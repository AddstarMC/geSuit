package net.cubespace.geSuit.managers;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.events.NewPlayerJoinEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Ban;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Track;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayerManager {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");

    public static final HashMap<UUID, GSPlayer> cachedPlayers = new HashMap<>();
    public static final HashMap<String, GSPlayer> onlinePlayers = new HashMap<>();
    public static final ArrayList<Player> kickedPlayers = new ArrayList<>();

    public static boolean playerExists(Player player) {
        return getPlayer(player.getUsername()) != null || playerExists(player.getUniqueId());
    }

    public static boolean playerExists(UUID player) {
        return DatabaseManager.players.playerExists(Utilities.getStringFromUUID(player));
    }

    /** @param onComplete called when init is done (or denied). @param denyWithReason call with reason to deny login (then onComplete will still be called). */
    public static void initPlayer(Player player, Runnable onComplete, Consumer<Component> denyWithReason) {
        ProxyServer proxy = geSuit.getInstance().getProxy();
        proxy.getScheduler().buildTask(geSuit.getInstance(), () -> {
            if (banCheck(player, onComplete, denyWithReason)) {
                return;
            }
            boolean playerExists = playerExists(player.getUniqueId());
            if (!playerExists && lockDownCheck(player, onComplete, denyWithReason)) {
                return;
            }
            GSPlayer gsPlayer;
            if (playerExists) {
                gsPlayer = getPlayer(player.getUsername());
                if (gsPlayer == null) {
                    gsPlayer = DatabaseManager.players.loadPlayer(Utilities.getStringFromUUID(player.getUniqueId()));
                    gsPlayer.setName(player.getUsername());
                    HomesManager.loadPlayersHomes(gsPlayer);
                    LoggingManager.log(ConfigManager.messages.PLAYER_LOAD.replace("{player}", gsPlayer.getName()).replace("{uuid}", player.getUniqueId().toString()));
                } else {
                    LoggingManager.log(ConfigManager.messages.PLAYER_LOAD_CACHED.replace("{player}", gsPlayer.getName()).replace("{uuid}", player.getUniqueId().toString()));
                }
            } else {
                gsPlayer = new GSPlayer(player.getUsername(), Utilities.getStringFromUUID(player.getUniqueId()), true);
                gsPlayer.setFirstJoin(true);
            }
            if (player.getRemoteAddress() != null && player.getRemoteAddress().getAddress() != null) {
                gsPlayer.setIp(player.getRemoteAddress().getHostString());
            } else {
                gsPlayer.setIp("unknown");
            }
            Track history = DatabaseManager.tracking.checkNameChange(player.getUniqueId(), player.getUsername());
            if (history != null) {
                gsPlayer.setLastName(history);
            }
            cachedPlayers.put(player.getUniqueId(), gsPlayer);
            onComplete.run();
        }).schedule();
    }

    public static boolean banCheck(Player player, Runnable onComplete, Consumer<Component> denyWithReason) {
        String ip = player.getRemoteAddress() != null ? player.getRemoteAddress().getHostString() : "";
        if (DatabaseManager.bans.isPlayerBanned(player.getUsername(), Utilities.getStringFromUUID(player.getUniqueId()), ip)) {
            Ban b = DatabaseManager.bans.getBanInfo(player.getUsername(), Utilities.getStringFromUUID(player.getUniqueId()), ip);
            if (b != null) {
                if (b.getType().equals("tempban") && BansManager.checkTempBan(b)) {
                    Date then = b.getBannedUntil();
                    long timeDiff = then.getTime() - System.currentTimeMillis();
                    Component reason = LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(
                        ConfigManager.messages.TEMP_BAN_MESSAGE
                            .replace("{sender}", b.getBannedBy())
                            .replace("{time}", sdf.format(then))
                            .replace("{left}", Utilities.buildTimeDiffString(timeDiff, 2))
                            .replace("{shortleft}", Utilities.buildShortTimeDiffString(timeDiff, 10))
                            .replace("{message}", b.getReason())));
                    denyWithReason.accept(reason);
                    onComplete.run();
                    return true;
                } else if (!b.getType().equals("tempban")) {
                    Component reason = LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(
                        ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{sender}", b.getBannedBy()).replace("{message}", b.getReason())));
                    denyWithReason.accept(reason);
                    onComplete.run();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean lockDownCheck(Player player, Runnable onComplete, Consumer<Component> denyWithReason) {
        if (!LockDownManager.checkExpiry()) {
            String timeRemaining = Utilities.buildShortTimeDiffString(LockDownManager.getExpiryTime() - System.currentTimeMillis(), 2);
            Component reason = LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(
                ConfigManager.messages.LOCKDOWN_MESSAGE.replace("{message}", LockDownManager.getOptionalMessage())));
            denyWithReason.accept(reason);
            LoggingManager.log(player.getUsername() + " refused due to server lockdown. Remaining: " + timeRemaining);
            onComplete.run();
            return true;
        }
        return false;
    }

    public static GSPlayer confirmJoin(Player player) {
        final GSPlayer gsPlayer = cachedPlayers.get(player.getUniqueId());
        if (gsPlayer.firstConnect()) {
            if (gsPlayer.isFirstJoin()) {
                String ip = player.getRemoteAddress() != null ? player.getRemoteAddress().getHostString() : "";
                DatabaseManager.players.insertPlayer(gsPlayer, ip);
                LoggingManager.log(ConfigManager.messages.PLAYER_CREATE.replace("{player}", player.getUsername()).replace("{uuid}", player.getUniqueId().toString()));
                if (ConfigManager.main.NewPlayerBroadcast) {
                    String welcomeMsg = ConfigManager.messages.NEW_PLAYER_BROADCAST.replace("{player}", player.getUsername());
                    sendBroadcast(welcomeMsg, player.getUsername());
                    geSuit.getInstance().getProxy().getEventManager().fire(new NewPlayerJoinEvent(player.getUsername(), welcomeMsg));
                }
                if (ConfigManager.spawn.SpawnNewPlayerAtNewspawn && SpawnManager.NewPlayerSpawn != null) {
                    SpawnManager.newPlayers.add(player);
                    geSuit.getInstance().getProxy().getScheduler()
                        .buildTask(geSuit.getInstance(), () -> {
                            SpawnManager.sendPlayerToNewPlayerSpawn(gsPlayer);
                            SpawnManager.newPlayers.remove(player);
                        })
                        .delay(300, TimeUnit.MILLISECONDS)
                        .schedule();
                }
            }
            onlinePlayers.put(player.getUsername().toLowerCase(), gsPlayer);
        }
        return gsPlayer;
    }

    public static void unloadPlayer(String player) {
        if (onlinePlayers.containsKey(player.toLowerCase())) {
            onlinePlayers.remove(player.toLowerCase());
            LoggingManager.log(ConfigManager.messages.PLAYER_UNLOAD.replace("{player}", player));
        }
    }

    public static void sendMessageToTarget(CommandSource target, String message) {
        if (target == null) return;
        for (String line : Utilities.colorize(message).split("\n")) {
            if (geSuit.getInstance().isDebugEnabled()) {
                geSuit.getInstance().getLogger().info("DEBUG: [SendMessage] " + target.getClass().getSimpleName() + ": " + Utilities.colorize(line));
            }
            target.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(line)));
        }
    }

    public static void sendMessageToTarget(GSPlayer target, String message) {
        if (target == null) return;
        Player p = target.getPlayer();
        if (p != null) sendMessageToTarget(p, message);
    }

    public static void sendMessageToTarget(String target, String message) {
        if (target == null || target.isEmpty()) return;
        GSPlayer p = getPlayer(target);
        CommandSource dest = (p != null && p.getPlayer() != null) ? p.getPlayer() : geSuit.getInstance().getProxy().getConsoleCommandSource();
        sendMessageToTarget(dest, message);
    }

    public static void sendBroadcast(String message) {
        sendBroadcast(message, null);
    }

    public static void sendBroadcast(String message, String excludedPlayer) {
        for (Player p : geSuit.getInstance().getProxy().getAllPlayers()) {
            if (excludedPlayer != null && excludedPlayer.equals(p.getUsername())) continue;
            sendMessageToTarget(p.getUsername(), message);
        }
        LoggingManager.log(message);
    }

    public static String getLastSeeninfos(String player, boolean full, boolean seeVanished) {
        GSPlayer p = getPlayer(player);
        LinkedHashMap<String, String> items = new LinkedHashMap<>();
        boolean online = (p != null && p.getPlayer() != null);
        if (p == null) {
            p = DatabaseManager.players.loadPlayer(player);
        }
        if (p == null) {
            return ConfigManager.messages.PLAYER_DOES_NOT_EXIST;
        }
        Ban b = DatabaseManager.bans.getBanInfo(p.getName(), p.getUuid(), null);
        if (b != null) {
            if (b.getType().equals("tempban")) {
                if (b.getBannedUntil().getTime() > System.currentTimeMillis()) {
                    items.put("Temp Banned", Utilities.buildShortTimeDiffString(b.getBannedUntil().getTime() - System.currentTimeMillis(), 3) + " remaining");
                    items.put("Ban Reason", b.getReason());
                    if (full) items.put("Banned By", b.getBannedBy());
                }
            } else {
                items.put("Banned", b.getReason());
                if (full) items.put("Banned By", b.getBannedBy());
            }
        }
        if (full) {
            if (online && p.getPlayer().getCurrentServer().isPresent()) {
                items.put("Server", p.getPlayer().getCurrentServer().get().getServerInfo().getName());
            }
            items.put("IP", p.getIp());
            try {
                String location = GeoIPManager.lookup(InetAddress.getByName(p.getIp()));
                if (location != null) items.put("Location", location);
            } catch (UnknownHostException ignored) {}
        }
        String message = (online ? ConfigManager.messages.PLAYER_SEEN_ONLINE : ConfigManager.messages.PLAYER_SEEN_OFFLINE).replace("{player}", p.getName());
        if (online) {
            String fullDate = String.format("%s @ %s",
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(p.getLoginTime()),
                DateFormat.getTimeInstance(DateFormat.MEDIUM).format(p.getLoginTime()));
            message = message.replace("{timediff}", Utilities.buildTimeDiffString(System.currentTimeMillis() - p.getLoginTime(), 2)).replace("{date}", fullDate);
        } else {
            if (p.getLastOnline() != null) {
                String fullDate = String.format("%s @ %s",
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(p.getLastOnline()),
                    DateFormat.getTimeInstance(DateFormat.MEDIUM).format(p.getLastOnline()));
                message = message.replace("{timediff}", Utilities.buildTimeDiffString(System.currentTimeMillis() - p.getLastOnline().getTime(), 2)).replace("{date}", fullDate);
            } else {
                message = message.replace("{timediff}", "Never").replace("{date}", "Never");
            }
        }
        StringBuilder builder = new StringBuilder(message);
        for (Entry<String, String> item : items.entrySet()) {
            builder.append('\n').append(ConfigManager.messages.PLAYER_SEEN_ITEM_FORMAT
                .replace("{name}", item.getKey()).replace("{value}", item.getValue()));
        }
        return builder.toString();
    }

    public static GSPlayer matchOnlinePlayer(String player) {
        GSPlayer match = getPlayer(player);
        if (match != null) return match;
        GSPlayer fuzzymatch = null;
        for (GSPlayer p : onlinePlayers.values()) {
            Player pp = p.getPlayer();
            if (pp != null && pp.getUsername().equalsIgnoreCase(player)) return p;
            if (p.getUuid() != null && p.getUuid().equals(player)) return p;
            if (p.getName().toLowerCase().startsWith(player.toLowerCase())) match = p;
            if (p.getName().toLowerCase().contains(player.toLowerCase())) fuzzymatch = p;
        }
        return match != null ? match : fuzzymatch;
    }

    public static List<GSPlayer> getPlayersByIP(String ip) {
        List<GSPlayer> matchingPlayers = new ArrayList<>();
        if (ip == null) return matchingPlayers;
        for (GSPlayer p : onlinePlayers.values()) {
            Player pp = p.getPlayer();
            if (pp != null && pp.getRemoteAddress() != null && pp.getRemoteAddress().getHostString().equalsIgnoreCase(ip)) {
                matchingPlayers.add(p);
            }
        }
        return matchingPlayers;
    }

    public static Collection<GSPlayer> getPlayers() {
        return onlinePlayers.values();
    }

    public static Collection<GSPlayer> cachedPlayers() {
        return cachedPlayers.values();
    }

    public static GSPlayer getPlayer(String player) {
        return onlinePlayers.get(player.toLowerCase());
    }

    public static GSPlayer getPlayer(String player, boolean expectOnline) {
        GSPlayer p = getPlayer(player);
        if (p == null && expectOnline) {
            geSuit.getInstance().getLogger().warn("Unable to find player named \"" + player + "\" in onlinePlayers list!");
        }
        return p;
    }

    public static GSPlayer getPlayer(UUID id) {
        return cachedPlayers.get(id);
    }

    public static GSPlayer getPlayer(Player player) {
        return cachedPlayers.get(player.getUniqueId());
    }

    public static void updateTracking(GSPlayer player) {
        DatabaseManager.tracking.insertTracking(player.getName(), player.getUuid(), player.getIp());
    }

    public static String retrieveOldNames(CommandSource sender, String playername) {
        GSPlayer p = getPlayer(playername);
        if (p == null) p = DatabaseManager.players.loadPlayer(playername);
        if (p == null) return ConfigManager.messages.PLAYER_DOES_NOT_EXIST;
        DatabaseManager.tracking.insertNameHistory(p);
        return "Name History for " + p.getName() + " updated";
    }

    public static void batchUpdatePlayerNames(CommandSource sender, boolean all, String start, String end) {
        if (all) {
            DatabaseManager.tracking.batchUpdateNameHistories(DatabaseManager.players.getAllUUIDs());
        } else {
            DatabaseManager.tracking.batchUpdateNameHistories(DatabaseManager.players.getUUIDs(start, end));
        }
    }

    public static void sendtoNewSpawn(GSPlayer p) {
        p.setNewSpawn(true);
        DatabaseManager.players.updatePlayer(p);
    }
}
