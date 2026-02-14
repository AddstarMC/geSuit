package net.cubespace.geSuit.managers;

import com.google.common.collect.Iterables;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.events.BanPlayerEvent;
import net.cubespace.geSuit.events.UnbanPlayerEvent;
import net.cubespace.geSuit.events.WarnPlayerEvent;
import net.cubespace.geSuit.events.WarnPlayerEvent.ActionType;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BansManager {

    private static final List<Kick> kicks = new ArrayList<>();

    private static ProxyServer getProxy() {
        return geSuit.getInstance().getProxy();
    }

    private static String getSenderName(CommandSource sender) {
        return sender instanceof Player ? ((Player) sender).getUsername() : "Console";
    }

    public static void banPlayer(String bannedBy, String player, String reason) {
    	banPlayer(bannedBy, player, reason, false);
    }

    public static void banPlayer(String bannedBy, String player, String reason, Boolean auto) {
        GSPlayer s = PlayerManager.getPlayer(bannedBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());
        banPlayer(sender, player, reason, auto);

    }

    public static void banPlayer(CommandSource sender, String player, String reason, Boolean auto) {
        BanTarget t = getBanTarget(player);
        if (t.gsp == null)
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.UNKNOWN_PLAYER_STILL_BANNING);
        Ban b = DatabaseManager.bans.getBanInfo(t.name, t.uuid, null);
        if (b != null) {
            if (b.getType().equals("tempban")) {
                // We don't want tempbans AND bans in place.. it could cause issues!
                DatabaseManager.bans.unbanPlayer(b.getId());
            } else {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_ALREADY_BANNED);
                return;
            }
        }
        String bannedBy = getSenderName(sender);
        if (reason == null || reason.equals("")) {
            // Do not allow a ban without a reason since people accidentally do /db instead of /dst or /dtb
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BAN_REASON_REQUIRED);
            return;
        }

        DatabaseManager.bans.banPlayer(t.name, t.uuid, null, bannedBy, reason, "ban");

        callEvent(new BanPlayerEvent(new Ban(-1, t.name, t.uuid, null, bannedBy, reason, "ban", 1, null, null), auto));

        // Player is online so kick them
        if ((t.gsp != null) && (t.gsp.getPlayer() != null)) {
            disconnectPlayer(t.gsp.getPlayer(), Utilities.colorize(ConfigManager.messages.BAN_PLAYER_MESSAGE.replace("{message}", reason).replace("{sender}", bannedBy)));
        }

        if (ConfigManager.bans.BroadcastBans) {
            if (auto) {
                PlayerManager.sendBroadcast(Utilities.colorize(ConfigManager.messages.BAN_PLAYER_AUTO_BROADCAST.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender))), t.name);
            } else {
                PlayerManager.sendBroadcast(ConfigManager.messages.BAN_PLAYER_BROADCAST.replace("{player}", t.dispname).replace("{message}", reason).replace("{sender}", bannedBy), t.name);
            }
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BAN_PLAYER_BROADCAST.replace("{player}", t.dispname).replace("{message}", reason).replace("{sender}", bannedBy));
        }

    }


    public static void unbanPlayer(String sentBy, String player) {
        GSPlayer s = PlayerManager.getPlayer(sentBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());
        unbanPlayer(sender, player);
    }

    public static void unbanPlayer(CommandSource sender, String player) {
        BanTarget t = getBanTarget(player);
        if (!DatabaseManager.bans.isPlayerBanned(t.name, t.uuid, player)) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_NOT_BANNED);
            return;
        }

        Ban b = DatabaseManager.bans.getBanInfo(t.name, t.uuid, player);

        DatabaseManager.bans.unbanPlayer(b.getId());
        callEvent(new UnbanPlayerEvent(b, getSenderName(sender)));

        if (ConfigManager.bans.BroadcastUnbans) {
            PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_UNBANNED.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)));
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_UNBANNED.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)));
        }
    }


    public static void banIP(String bannedBy, String target, String reason) {
        GSPlayer s = PlayerManager.getPlayer(bannedBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());
        banIP(sender, target, reason);

    }

    public static void banIP(CommandSource sender, String target, String reason) {
        if (reason == null || reason.equals("")) {
            reason = Utilities.colorize(ConfigManager.messages.DEFAULT_BAN_REASON);
        }

        String ip = null;
        String uuid = null;
        String player = null;
        if (Utilities.isIPAddress(target)) {
            // Target is just an IP address.. we don't know which player/uuid so keep them null
            ip = target;
        } else {
            // Target is a player name or uuid.. grab the player details and record it all
            GSPlayer gs = DatabaseManager.players.loadPlayer(target);
            if (gs != null) {
                ip = gs.getIp();
                uuid = gs.getUuid();
                player = gs.getName();
            }
        }

        if ((ip == null) || (ip.isEmpty())) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }
        String bannedBy = getSenderName(sender);
        if (!DatabaseManager.bans.isPlayerBanned(ip)) {
            if (DatabaseManager.bans.banPlayer(player, uuid, ip, bannedBy, reason, "ipban") > 0) {
                callEvent(new BanPlayerEvent(new Ban(-1, player, uuid, ip, bannedBy, reason, "ipban", 1, null, null), false));
            } else {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.BAN_FAILED_UNKNOWN_REASON);
                return;
            }
        }

        for (GSPlayer p : PlayerManager.getPlayersByIP(ip)) {
            if (p.getPlayer() != null) {
                disconnectPlayer(p.getPlayer(), ConfigManager.messages.IPBAN_PLAYER.replace("{message}", reason).replace("{sender}", bannedBy));
            }
        }

        if (ConfigManager.bans.BroadcastBans) {
            // Notify players of the ban (use the regular Ban message)
            PlayerManager.sendBroadcast(ConfigManager.messages.BAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
            Utilities.sendOnChatChannel(ConfigManager.main.ChatControlChannel, ConfigManager.messages.IPBAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.IPBAN_PLAYER_BROADCAST.replace("{player}", player).replace("{message}", reason).replace("{sender}", bannedBy));
        }
    }

    public static void kickAll(String sender, String message) {
        if (message.equals("")) {
            message = ConfigManager.messages.DEFAULT_KICK_MESSAGE;
        }

        message = Utilities.colorize(ConfigManager.messages.KICK_PLAYER_MESSAGE.replace("{message}", message).replace("{sender}", sender));

        for (Player p : getProxy().getAllPlayers()) {
            if ((!p.hasPermission("gesuit.bypass.kickall")) && (!p.getUsername().equals(sender))) {
                disconnectPlayer(p, message);
            }
        }
    }


    public static void checkPlayersBan(String sentBy, String player) {
        GSPlayer s = PlayerManager.getPlayer(sentBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        BanTarget t = getBanTarget(player);
        Ban b = DatabaseManager.bans.getBanInfo(t.name);

        if (b == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_NOT_BANNED);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
            PlayerManager.sendMessageToTarget(sender, "&3-------- &cBan Info&3 --------");
            PlayerManager.sendMessageToTarget(sender, "&cPlayer: &b" + b.getPlayer());
            if (b.getUuid() != null) {
                PlayerManager.sendMessageToTarget(sender, "&cUUID: &b" + b.getUuid());
            }
            PlayerManager.sendMessageToTarget(sender, "&cType: &b" + b.getType());
            PlayerManager.sendMessageToTarget(sender, "&cBy: &b" + b.getBannedBy());
            PlayerManager.sendMessageToTarget(sender, "&cReason: &b" + b.getReason());
            PlayerManager.sendMessageToTarget(sender, "&cDate: &b" + sdf.format(b.getBannedOn()));

            if (b.getBannedUntil() == null) {
                PlayerManager.sendMessageToTarget(sender, "&cUntil: &b-Forever-");
            } else {
                PlayerManager.sendMessageToTarget(sender, "&cUntil: &b" + sdf.format(b.getBannedUntil()));
            }
        }
    }

    public static void displayPlayerBanHistory(String sentBy, String player) {
        GSPlayer s = PlayerManager.getPlayer(sentBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        BanTarget t = getBanTarget(player);
        List<Ban> bans = DatabaseManager.bans.getBanHistory(t.name);

        if (bans == null || bans.isEmpty()) {
            PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_BANNED.replace("{player}", t.dispname)));
            return;
        }
        PlayerManager.sendMessageToTarget(sender, "&3-------- &e" + player + "'s Ban History&3 --------");
        boolean first = true;
        for (Ban b : bans) {
            if (first) {
                first = false;
            } else {
                PlayerManager.sendMessageToTarget(sender, "");
            }
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("dd MMM yyyy HH:mm");

            PlayerManager.sendMessageToTarget(sender,
            		(b.getBannedUntil() != null ? "&6" : "&c") + "Date: " +
            		"&a" + sdf.format(b.getBannedOn()) +
            		(b.getBannedUntil() != null ?
            				"&6 > " + sdf.format(b.getBannedUntil()) :
            				"&c > permban"));

            PlayerManager.sendMessageToTarget(sender,
            		(b.getBannedUntil() != null ? "&6" : "&c") + "By: " +
            		"&b" + b.getBannedBy() +
            		"&3 (&7" + b.getReason() + "&3)");
        }
    }

    public static void kickPlayer(String kickedBy, String player, String reason) {
    	kickPlayer(kickedBy, player, reason, false);
    }

    public static void kickPlayer(String kickedBy, String player, String reason, Boolean auto) {
        GSPlayer s = PlayerManager.getPlayer(kickedBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        BanTarget t = getBanTarget(player);
        if ((t.gsp == null) || (t.gsp.getPlayer() == null)) {
        	PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_NOT_ONLINE);
        	return;
        }

        if (reason.isEmpty()) {
            reason = ConfigManager.messages.DEFAULT_KICK_MESSAGE;
        }

        disconnectPlayer(t.gsp.getPlayer(), Utilities.colorize(ConfigManager.messages.KICK_PLAYER_MESSAGE.replace("{message}", reason).replace("{sender}", getSenderName(sender))));
        int kickLimit = ConfigManager.bans.KickLimit;
        if (kickLimit > 0) {
            checkKickTempBan(kickLimit, t, kickedBy, reason);
        }
        if (ConfigManager.bans.RecordKicks) {
            DatabaseManager.bans.kickPlayer(t.name, t.uuid, kickedBy, reason);
        }

        if (ConfigManager.bans.BroadcastKicks) {
            // Only broadcast if the kick reason is not in the silent list
            if (!kickReasonInList(reason, ConfigManager.bans.KickReasonSilent)) {
                if (auto) {
                    PlayerManager.sendBroadcast(Utilities.colorize(ConfigManager.messages.KICK_PLAYER_AUTO_BROADCAST.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender))), t.name);
                } else {
                    PlayerManager.sendBroadcast(Utilities.colorize(ConfigManager.messages.KICK_PLAYER_BROADCAST.replace("{message}", reason).replace("{player}", t.dispname).replace("{sender}", getSenderName(sender))), t.name);
                }
            }
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.KICK_PLAYER_BROADCAST.replace("{message}", reason).replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)));
        }
    }

    public static void disconnectPlayer(Player player, String message) {
        player.disconnect(LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(message)));
    }

    public static void reloadBans(String sender) {
        PlayerManager.sendMessageToTarget(sender, "Bans Reloaded");

        try {
            ConfigManager.bans.reload();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void tempBanPlayer(String bannedBy, String player, int seconds, String message) {
    	tempBanPlayer(bannedBy, player, seconds, message, false);
    }

    public static void tempBanPlayer(String bannedBy, String player, long seconds, String message, Boolean auto) {
        BanTarget t = getBanTarget(player);
        tempBanPlayer(bannedBy, t, seconds, message, auto);
    }

    public static void tempBanPlayer(String bannedBy, BanTarget t, long seconds, String message, Boolean auto) {
        GSPlayer s = PlayerManager.getPlayer(bannedBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());
        if (t.gsp == null)
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.UNKNOWN_PLAYER_STILL_BANNING);

        Ban b = DatabaseManager.bans.getBanInfo(t.name, t.uuid, null);
        if (b != null) {
            if (b.getType().equals("tempban")) {
                // We don't want tempbans AND bans in place.. it could cause issues!
                DatabaseManager.bans.unbanPlayer(b.getId());
            } else {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_ALREADY_BANNED);
                return;
            }
        }

        if (message == null || message.equals("")) {
            // Do not allow a temp ban without a reason since people accidentally do /dtb instead of /dst
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.TEMP_BAN_REASON_REQUIRED);
            return;
        }

        Date sqlToday = new Date(System.currentTimeMillis() + (seconds * 1000L));
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("dd MMM yyyy HH:mm:ss");
        String time = sdf.format(sqlToday);
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        String timeDiff = Utilities.buildTimeDiffString(seconds * 1000L, 2);
        String shortTimeDiff = Utilities.buildShortTimeDiffString(seconds * 1000L, 10);

        DatabaseManager.bans.tempBanPlayer(t.name, t.uuid, bannedBy, message, sdf.format(sqlToday));
        PlayerManager.sendtoNewSpawn(t.gsp);
        callEvent(new BanPlayerEvent(new Ban(-1, t.name, t.uuid, null, bannedBy, message, "tempban", 1, null, new Timestamp(System.currentTimeMillis() + (seconds * 1000L))), auto));

        if ((t.gsp != null) && (t.gsp.getPlayer() != null)) {
            disconnectPlayer(t.gsp.getPlayer(), ConfigManager.messages.TEMP_BAN_MESSAGE.replace("{sender}", t.dispname).replace("{time}", time).replace("{left}", timeDiff).replace("{shortleft}", shortTimeDiff).replace("{message}", message));
        }

        if (ConfigManager.bans.BroadcastBans) {
            if (auto) {
                PlayerManager.sendBroadcast(Utilities.colorize(ConfigManager.messages.TEMP_BAN_AUTO_BROADCAST.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)).replace("{time}", time).replace("{left}", timeDiff).replace("{shortleft}", shortTimeDiff)));
            } else {
                PlayerManager.sendBroadcast(ConfigManager.messages.TEMP_BAN_BROADCAST.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)).replace("{message}", message).replace("{time}", time).replace("{left}", timeDiff).replace("{shortleft}", shortTimeDiff));
            }
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.TEMP_BAN_BROADCAST.replace("{player}", t.dispname).replace("{sender}", getSenderName(sender)).replace("{message}", message).replace("{time}", time).replace("{left}", timeDiff).replace("{shortleft}", shortTimeDiff));
        }
    }

    public static boolean checkTempBan(Ban b) {
        java.util.Date today = new java.util.Date(Calendar.getInstance().getTimeInMillis());
        java.util.Date banned = b.getBannedUntil();

        if (today.compareTo(banned) >= 0) {
            DatabaseManager.bans.unbanPlayer(b.getId());
            return false;
        }

        return true;
    }

    public static void warnPlayer(String warnedBy, String player, String reason) {
        GSPlayer s = PlayerManager.getPlayer(warnedBy);
        CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        BanTarget t = getBanTarget(player);
        if (t.gsp == null) {
        	PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.UNKNOWN_PLAYER_NOT_WARNING);
        	return;
    	}
        
        if (reason == null || reason.isEmpty()) {
            // Do not allow a warning without a reason since people accidentally do /dw instead of /dst
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.WARN_REASON_REQUIRED);
            return;
        }

        DatabaseManager.bans.warnPlayer(t.name, t.uuid, warnedBy, reason);

        if (ConfigManager.bans.BroadcastWarns) {
            PlayerManager.sendBroadcast(ConfigManager.messages.WARN_PLAYER_BROADCAST.replace("{player}", t.dispname).replace("{message}", reason).replace("{sender}", warnedBy));
        } else {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.WARN_PLAYER_BROADCAST.replace("{player}", t.dispname).replace("{message}", reason).replace("{sender}", warnedBy));
        }

        int warncount = 0;
        ActionType actionType = ActionType.None;
        String actionExtra = "";
        // Check if we have warning actions defined
        if (ConfigManager.bans.Actions != null) {
        	List<Ban> warnings = DatabaseManager.bans.getWarnHistory(t.name, t.uuid);
        	warncount = 0;
            for (Ban w : warnings) {
            	// Only count warnings that have not expired
            	Date now = new Date(); 
	            int age = (int) ((now.getTime() - w.getBannedOn().getTime()) / 1000 / 86400);
	        	if (age < ConfigManager.bans.WarningExpiryDays) {
	        		warncount++;
	        	}
            }

        	if (ConfigManager.bans.Actions.containsKey(warncount)) {
        		String fullaction = ConfigManager.bans.Actions.get(warncount);
        		String[] parts = fullaction.split(" ");

        		String action = parts[0];
                switch (action) {
                    case "kick":
                        actionType = ActionType.Kick;
                        if ((t.gsp != null) && (t.gsp.getPlayer() != null)) {
                            kickPlayer(warnedBy, t.name, reason, true);
                        }
                        break;
                    case "tempban":
                        actionType = ActionType.TempBan;
                        int seconds = TimeParser.parseString(parts[1]);
                        tempBanPlayer(warnedBy, t.name, seconds, reason, true);
                        actionExtra = "for " + parts[1];
                        break;
                    case "ban":
                        actionType = ActionType.Ban;
                        banPlayer(warnedBy, t.name, reason, true);
                        break;
                    default:
                        PlayerManager.sendMessageToTarget(sender, "&c" + "Warning action of \"" + fullaction + "\" is invalid!");
                        LoggingManager.log("&c" + "Warning action of \"" + fullaction + "\" is invalid!");
                        break;
                }
            }
        }
        
        callEvent(new WarnPlayerEvent(t.name, t.uuid, warnedBy, reason, actionType, actionExtra, warncount));
    }

    public static void displayPlayerWarnHistory(final String sentBy, final String player, final boolean showStaffNames) {
        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);

            CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            // Resolve the target player
            GSPlayer target = PlayerManager.getPlayer(player);
            String targetId;
            if (target == null) {
                Map<String, UUID> ids = DatabaseManager.players.resolvePlayerNamesHistoric(Collections.singletonList(player));
                UUID id = Iterables.getFirst(ids.values(), null);
                if (id == null) {
                    PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED.replace("{player}", player)));
                    return;
                }
                targetId = id.toString().replace("-", "");
            } else {
                targetId = target.getUuid();
            }

            List<Ban> warns = DatabaseManager.bans.getWarnHistory(player, targetId);
            if (warns == null || warns.isEmpty()) {
                PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED.replace("{player}", player)));
                return;
            }
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + player + "'s Warning History" + "&3" + " --------");

            int count = 0;
            for (Ban b : warns) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern("dd MMM yyyy HH:mm");

                Date now = new Date();
                int age = (int) ((now.getTime() - b.getBannedOn().getTime()) / 1000 / 86400);

                String warnedBy = " ";

                if (age >= ConfigManager.bans.WarningExpiryDays) {
                    if (showStaffNames)
                        warnedBy = "&8" + " (" + "&8" + b.getBannedBy() + "&8" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&7" + "- " +
                                    "&8" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&8" + b.getReason());
                } else {
                    count++;
                    if (showStaffNames)
                        warnedBy = "&e" + " (" + "&7" + b.getBannedBy() + "&e" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&e" + String.valueOf(count) + ": " +
                                    "&a" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&b" + b.getReason());
                }
            }
        }).schedule();
    }

    public static void displayIPWarnBanHistory(final String sentBy, final String ip) {

        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);
            CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            List<Track> tracking = DatabaseManager.tracking.getPlayerTracking(ip, "ip");
            if (tracking.isEmpty()) {
                PlayerManager.sendMessageToTarget(sender,
                        "&c" + "[Tracker] No known accounts match or contain \"" + ip + "\"");
                return;
            }

            // Construct a list of UUIDs and player names
            // (we only want to lookup warnings for the player's current name)
            HashMap<String, String> uuidNameMap = new HashMap<>();
            for (Track t : tracking) {
                uuidNameMap.put(t.getUuid(), t.getPlayer());
            }

            PlayerManager.sendMessageToTarget(sender,
                    "&a" + "[Tracker] IP address \"" + ip + "\" has " + uuidNameMap.size() + " accounts:");

            // Copy the names into a list so that we can sort
            List<String> sortedNames = new ArrayList<>(uuidNameMap.values());

            // Show warnings for each player, sorting alphabetically by name
            sortedNames.sort(String.CASE_INSENSITIVE_ORDER);
            for (String playerName : sortedNames) {
                displayPlayerWarnBanHistory(sender, playerName);
            }
        });

    }

    public static void displayPlayerWarnBanHistory(final String sentBy, final String player) {
        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);

            CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            displayPlayerWarnBanHistory(sender, player);
        }).schedule();
    }

    private static void displayPlayerWarnBanHistory(CommandSource sender, final String player) {

        // Resolve the target player
        GSPlayer target = PlayerManager.getPlayer(player);
        String targetId;
        if (target == null) {
            Map<String, UUID> ids = DatabaseManager.players.resolvePlayerNamesHistoric(Collections.singletonList(player));
            UUID id = Iterables.getFirst(ids.values(), null);
            if (id == null) {
                PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED_OR_BANNED.replace("{player}", player)));
                return;
            }
            targetId = id.toString().replace("-", "");
        } else {
            targetId = target.getUuid();
        }

        // Retrieve warnings
        List<Ban> warns = DatabaseManager.bans.getWarnHistory(player, targetId);

        // Retrieve active bans
        BanTarget t = getBanTarget(player);
        Ban activeBan = DatabaseManager.bans.getBanInfo(t.name);

        if (activeBan == null && (warns == null || warns.isEmpty())) {
            PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED_OR_BANNED.replace("{player}", player)));
            return;
        }

        if (warns == null || warns.isEmpty())
        {
            PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_WARNED.replace("{player}", player)));
        } else {
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + player + "'s Warning History" + "&3" + " --------");

            int count = 0;
            for (Ban b : warns) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern("dd MMM yyyy HH:mm");

                Date now = new Date();
                int age = (int) ((now.getTime() - b.getBannedOn().getTime()) / 1000 / 86400);

                String warnedBy = " ";

                if (age >= ConfigManager.bans.WarningExpiryDays) {
                    warnedBy = "&8" + " (" + "&8" + b.getBannedBy() + "&8" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&7" + "- " +
                                    "&8" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&8" + b.getReason());
                } else {
                    count++;
                    warnedBy = "&e" + " (" + "&7" + b.getBannedBy() + "&e" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&e" + String.valueOf(count) + ": " +
                                    "&a" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&b" + b.getReason());
                }
            }
        }

        if (activeBan != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
            String banType = activeBan.getType();

            PlayerManager.sendMessageToTarget(sender, "");
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&c" + "Ban Info" + "&3" + " --------");
            PlayerManager.sendMessageToTarget(sender,
                    "&b" + activeBan.getPlayer() +
                            "&f" + " was banned on " +
                            "&b" + sdf.format(activeBan.getBannedOn()) +
                            "&f" + " by " +
                            "&b" + activeBan.getBannedBy());

            if (activeBan.getBannedUntil() == null) {
                String banDescription = banType;

                if (banType.equalsIgnoreCase("ban"))
                    banDescription = "permanent ban";
                else if (banType.equalsIgnoreCase("ipban"))
                    banDescription = "IP ban";

                PlayerManager.sendMessageToTarget(sender, "&c" + "Type: " + "&b" + banDescription);

            } else {
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                if (currentTime.after(activeBan.getBannedUntil()))
                {
                    PlayerManager.sendMessageToTarget(sender,
                            "&c" + "Type: " +
                                    "&b" + banType +
                                    "&f" + ", expired " +
                                    "&a" + sdf.format(activeBan.getBannedUntil()));
                } else {
                    PlayerManager.sendMessageToTarget(sender,
                            "&c" + "Type: " +
                                    "&b" + banType +
                                    "&f" + ", until " +
                                    "&a" + sdf.format(activeBan.getBannedUntil()));
                }
            }
            PlayerManager.sendMessageToTarget(sender, "&c" + "Reason: " + "&b" + activeBan.getReason());
        }
    }

    public static void displayPlayerKickHistory(final String sentBy, final String player, final boolean showStaffNames) {
        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);

            CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            // Resolve the target player
            GSPlayer target = PlayerManager.getPlayer(player);
            String targetId;
            if (target == null) {
                Map<String, UUID> ids = DatabaseManager.players.resolvePlayerNamesHistoric(Collections.singletonList(player));
                UUID id = Iterables.getFirst(ids.values(), null);
                if (id == null) {
                    PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_KICKED.replace("{player}", player)));
                    return;
                }
                targetId = id.toString().replace("-", "");
            } else {
                targetId = target.getUuid();
            }

            List<Ban> warns = DatabaseManager.bans.getKickHistory(player, targetId);
            if (warns == null || warns.isEmpty()) {
                PlayerManager.sendMessageToTarget(sender, Utilities.colorize(ConfigManager.messages.PLAYER_NEVER_KICKED.replace("{player}", player)));
                return;
            }
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + player + "'s Kick History" + "&3" + " --------");

            int count = 0;
            for (Ban b : warns) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern("dd MMM yyyy HH:mm");

                Date now = new Date();
                int age = (int) ((now.getTime() - b.getBannedOn().getTime()) / 1000 / 86400);

                String warnedBy = " ";

                if (age >= ConfigManager.bans.KickExpiryDays) {
                    if (showStaffNames)
                        warnedBy = "&8" + " (" + "&8" + b.getBannedBy() + "&8" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&7" + "- " +
                                    "&8" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&8" + b.getReason());
                } else {
                    count++;
                    if (showStaffNames)
                        warnedBy = "&e" + " (" + "&7" + b.getBannedBy() + "&e" + ") ";

                    PlayerManager.sendMessageToTarget(sender,
                            "&e" + String.valueOf(count) + ": " +
                                    "&a" + sdf.format(b.getBannedOn()) +
                                    warnedBy +
                                    "&b" + b.getReason());
                }
            }
        }).schedule();
    }

    public static void displayWhereHistory(final String sentBy, final String options, final String search) {
        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);
            final CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            List<Track> tracking;
            if (search.contains(".")) {
                tracking = DatabaseManager.tracking.getPlayerTracking(search, "ip");
                if (tracking.isEmpty()) {
                    PlayerManager.sendMessageToTarget(sender,
                            "&c" + "[Tracker] No known accounts match or contain \"" + search + "\"");
                    return;
                } else {
                    PlayerManager.sendMessageToTarget(sender,
                            "&a" + "[Tracker] IP address \"" + search + "\" matches " + tracking.size() + " accounts/IPs:");
                }
            } else {
                String type;
                String searchString = search;
                if (searchString.length() > 20) {
                    type = "uuid";
                    searchString = searchString.replace("-", "");
                } else {
                    type = "name";
                }

                if (!DatabaseManager.players.playerExists(searchString)) {
                    // No exact match... do a partial match
                    PlayerManager.sendMessageToTarget(sender,
                            "&b" + "[Tracker] No accounts matched exactly \"" + searchString + "\", trying wildcard search..");

                    List<String> matches = DatabaseManager.players.matchPlayers(searchString);
                    if (matches.isEmpty()) {
                        PlayerManager.sendMessageToTarget(sender,
                                "&c" + "[Tracker] No known accounts match or contain \"" + searchString + "\"");
                        return;
                    } else if (matches.size() == 1) {
                        if (searchString.length() < 20) {
                            // Searched for a player name
                            searchString = matches.get(0);
                        }
                    } else {
                        // Matched too many names, show list of names instead
                        // (showing the 20 most recent matches)
                        PlayerManager.sendMessageToTarget(sender,
                                "&c" + "[Tracker] More than one player matched \"" + searchString + "\":");
                        for (String m : matches) {
                            PlayerManager.sendMessageToTarget(sender,
                                    "&b" + " - " + m);
                        }
                        return;
                    }
                }

                tracking = DatabaseManager.tracking.getPlayerTracking(searchString, type);
                if (tracking.isEmpty()) {
                    PlayerManager.sendMessageToTarget(sender,
                            "&a" + "[Tracker] No known accounts match or contain \"" + searchString + "\"");
                    return;
                } else {
                    PlayerManager.sendMessageToTarget(sender,
                          "&a" + "[Tracker] Player \"" + searchString + "\" is associated with " + tracking.size() + " accounts/IPs:");
                    if (options.contains("d")) {
                        for (Track t : tracking) {
                            try {
                                InetAddress a = InetAddress.getByName(t.getIp());
                                String msg = ConfigManager.messages.GEOIP_REPORT
                                      .replace("{ip]", t.getIp())
                                      .replace("{location}", GeoIPManager.lookup(a));
                                PlayerManager.sendMessageToTarget(sender, msg);
                            } catch (UnknownHostException ignore) {
                                //suppress
                            }
                        }
                    }
                    if (getProxy().getPlayer(searchString).isPresent()) {
                        final Player player = getProxy().getPlayer(searchString).orElseThrow();
                        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
                            if (player.getRemoteAddress() != null && player.getRemoteAddress().getAddress() != null) {
                                List<String> location = GeoIPManager.detailLookup(player.getRemoteAddress().getAddress());
                                if (location.size() > 0) {
                                    PlayerManager.sendMessageToTarget(sender, "&a" + "[Tracker] Player " + player.getUsername() + "'s IP resolves: ");
                                    location.forEach(s1 -> PlayerManager.sendMessageToTarget(sender, "&a" + "[Tracker] " + s1));
                                }
                            }
                        }).schedule();
                    }
                }
            }

            // Construct a mapping betweeen UUID and the player's most recent username
            // Also keep track of the number of names associated with each uuid
            HashMap<String, String> uuidNameMap = new HashMap<>();
            HashMap<String, Integer> uuidNameCount = new HashMap<>();

            for (Track t : tracking) {
                String currentUuid = t.getUuid();

                uuidNameMap.put(currentUuid, t.getPlayer());
                uuidNameCount.merge(currentUuid, 1, Integer::sum);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Track t : tracking) {
                StringBuilder builder = new StringBuilder();
                builder.append("&2");
                builder.append(" - ");

                String playerName = t.getPlayer();
                if (uuidNameCount.get(t.getUuid()) > 1) {
                    String latestName = uuidNameMap.get(t.getUuid());
                    if (!latestName.equalsIgnoreCase(playerName)) {
                        playerName = latestName + " (" + playerName + ")";
                    }
                }

                if (t.isNameBanned()) {
                    builder.append("&3");
                    builder.append(playerName);
                    builder.append("&a");
                    if (t.getBanType().equals("ban")) {
                        builder.append("[Ban]");
                    } else {
                        builder.append("[Tempban]");
                    }
                } else {
                    builder.append(playerName);
                }

                builder.append(' ');

                if (t.isIpBanned()) {
                    builder.append("&3");
                    builder.append(t.getIp());
                    builder.append("&a");
                    builder.append("[IPBan]");
                } else {
                    builder.append("&e");
                    builder.append(t.getIp());
                }

                builder.append("&7");
                builder.append(" (");
                builder.append(sdf.format(t.getLastSeen()));
                builder.append(')');

                PlayerManager.sendMessageToTarget(sender, builder.toString());
            }
        }).schedule();
    }

    public static void displayPlayerOnTime(final String sentBy, final String player) {
        final GSPlayer s = PlayerManager.getPlayer(sentBy);
        final CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            BanTarget bt = getBanTarget(player);
            if ((bt == null) || (bt.gsp == null)) {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
                return;
            }

            // Get time records and set online time (if player is online)
            TimeRecord tr = DatabaseManager.ontime.getPlayerOnTime(bt.uuid);
            boolean online = (bt.gsp.getPlayer() != null);
            if (online) {
                tr.setTimeSession(System.currentTimeMillis() - bt.gsp.getLoginTime());
            } else {
                tr.setTimeSession(-1);
            }

            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + bt.dispname + "'s OnTime Statistics" + "&3" + " --------");

            // Player join date/time + number of days
            Timestamp firstTime = bt.gsp.getFirstOnline();
            String firstJoin;
            if (firstTime != null) {
                firstJoin = String.format("%s %s",
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(firstTime),
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(firstTime));
            } else {
                firstJoin = "Uknown - check with admins";
            }
            String days = Integer.toString((int) Math.floor((System.currentTimeMillis() - bt.gsp.getFirstOnline().getTime()) / TimeUnit.DAYS.toMillis(1)));
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_FIRST_JOINED
                    .replace("{date}", firstJoin)
                    .replace("{days}", days));

            // Current session length
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_SESSION
                    .replace("{diff}", (tr.getTimeSession() == -1) ? "&c" + "Offline" : Utilities.buildTimeDiffString(tr.getTimeSession(), 3)));

            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_TODAY.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeToday() + tr.getTimeSession(), 3)));
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_WEEK.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeWeek() + tr.getTimeSession(), 3)));
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_MONTH.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeMonth() + tr.getTimeSession(), 3)));
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_YEAR.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeYear() + tr.getTimeSession(), 3)));
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.ONTIME_TIME_TOTAL.replace("{diff}", Utilities.buildTimeDiffString(tr.getTimeTotal() + tr.getTimeSession(), 3)));
        }).schedule();
    }

    public static void displayOnTimeTop(final String sentBy, final int page) {
        final GSPlayer s = PlayerManager.getPlayer(sentBy);
        final CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            int pagenum;
            pagenum = page;
            if (pagenum > 20) {
                PlayerManager.sendMessageToTarget(sender, "&c" + "Sorry, maximum page number is 20.");
                return;
            }
            // Get time records and set online time (if player is online)
            Map<String, Long> results = DatabaseManager.ontime.getOnTimeTop(pagenum);
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + "OnTime Top Statistics" + "&3" + " (page " + page + ") --------");
            int offset = (pagenum < 1) ? 0 : (pagenum - 1) * 10;    // Offset = Page number x 10 (but starts at 0 and no less than 0
            for (String name : results.keySet()) {
                offset++;
                String line = ConfigManager.messages.ONTIME_TIME_TOP
                        .replace("{num}", String.format("%1$2s", offset))
                        .replace("{time}", Utilities.buildTimeDiffString(results.get(name) * 1000, 2))
                        .replace("{player}", name);
                PlayerManager.sendMessageToTarget(sender, line);
            }
        }).schedule();
    }
    public static void displayLastLogins(final String sentBy, final String player, final int num){
        final GSPlayer s = PlayerManager.getPlayer(sentBy);
        final CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            BanTarget bt = getBanTarget(player);
            if ((bt == null) || (bt.gsp == null)) {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
                return;
            }
            Map<Timestamp, Long> results = DatabaseManager.ontime.getLastLogins(bt.uuid, num);
            PlayerManager.sendMessageToTarget(sender, "&3" + "-------- " + "&e" + bt.dispname + "'s Last Login History" + "&3" + " --------");
            for (Map.Entry<Timestamp, Long> pair : results.entrySet()) {
                String line = ConfigManager.messages.LASTLOGINS_FORMAT
                        .replace("{date}", DateFormat.getDateInstance(DateFormat.MEDIUM).format(pair.getKey()))
                        .replace("{ontime}", Utilities.buildTimeDiffString(pair.getValue() * 1000, 2));
                PlayerManager.sendMessageToTarget(sender, line);
            }
        }).schedule();
    }

    
    public static void displayNameHistory(final String sentBy, final String nameOrId) {
        getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            GSPlayer s = PlayerManager.getPlayer(sentBy);
            final CommandSource sender = (s == null ? getProxy().getConsoleCommandSource() : s.getPlayer());

            UUID id;
            try {
                id = Utilities.makeUUID(nameOrId);
            } catch (IllegalArgumentException e) {
                Map<String, UUID> result = DatabaseManager.players.resolvePlayerNamesHistoric(Collections.singletonList(nameOrId));
                if (result.isEmpty()) {
                    PlayerManager.sendMessageToTarget(sender,
                            "&c" + "Unknown player " + nameOrId);
                    return;
                } else {
                    id = Iterables.getFirst(result.values(), null);
                }
            }

            List<Track> names = DatabaseManager.tracking.getNameHistory(id);

            PlayerManager.sendMessageToTarget(sender,
                    "&a" + "Player " + nameOrId + " has had " + names.size() + " different names:");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Track t : names) {

                String builder = "&2" +
                        " - " +
                        t.getPlayer() +
                        ' ' +
                        "&7" +
                        sdf.format(t.getLastSeen());
                PlayerManager.sendMessageToTarget(sender, builder);
            }
        }).schedule();
    }
    
    private static void callEvent(final Object event) {
        getProxy().getEventManager().fire(event);
    }

    private static void checkKickTempBan(int kickLimit, BanTarget t, String kickedBy, String reason) {
        long kickBanTime = TimeUnit.MILLISECONDS.toSeconds(ConfigManager.bans.TempBanTime);
        int kickCount = 0;

        if (kickReasonInList(reason, ConfigManager.bans.KickReasonIgnoreList)) {
            return;
        }

        if (kicks.size() > 0) {
            clearKicks();
            if (kicks.size() > 0) {
                for (Kick kick : kicks) { //find active kicks.
                    if (t.gsp.getUuid().equals(kick.getUuid())) {
                        kickCount++;
                    }
                }
                kickCount++; //add 1 for the current kick
                if (kickCount >= kickLimit) {
                    tempBanPlayer(kickedBy, t, kickBanTime, reason, false);
                    //clear this players kicks
                    kicks.removeIf(kick -> kick.getUuid().equals(t.uuid));
                } else {
                    kicks.add(new Kick(t.gsp.getUuid(), t.dispname, kickedBy, reason, System.currentTimeMillis()));
                }
            } else {
                kicks.add(new Kick(t.gsp.getUuid(), t.dispname, kickedBy, reason, System.currentTimeMillis()));
            }
        } else {
            kicks.add(new Kick(t.gsp.getUuid(), t.dispname, kickedBy, reason, System.currentTimeMillis()));
        }

    }

    public static void clearKicks() {
        long kickTimeOut = ConfigManager.bans.KicksTimeOut;
        //remove kicks that would have expired first
        kicks.removeIf(kick -> kick.getBannedOn() + kickTimeOut < System.currentTimeMillis());

    }


    public static List<Kick> getKicks() {
        return kicks;
    }


    private static class BanTarget {
    	String name = null;
    	String dispname = null;
		String uuid = null;
    	GSPlayer gsp = null;
    }
    
    private static BanTarget getBanTarget(String player) {
    	BanTarget target = new BanTarget();

    	// Try to find the player online
    	GSPlayer t = PlayerManager.matchOnlinePlayer(player);

        // If they are not online, try to find them as an offline player
        if (t == null) {
        	t = DatabaseManager.players.loadPlayer(player);
        }

        // Set up the target + display name we should use
        if (t == null) {
        	// Can't find this player so just use whatever player string was given to us
    		target.name = player;
    		target.dispname = player;
        } else {
        	// Get their real name, UUID and display name (alias)
        	if ((t.getUuid() != null) && (!t.getUuid().isEmpty())) {
        		target.uuid = t.getUuid();
        	}
        	target.name = t.getName();

        	// Get their display name (used for broadcasts and messages)
        	if (t.getPlayer() != null) {
        		target.dispname = t.getPlayer().getUsername();
        	} else {
        		target.dispname = t.getName();
        	}
        	target.gsp = t;
        }
    	return target;
    }

    private static boolean kickReasonInList(String reason, List<String> reasons) {
        // Check if this kick reason contains any message in the list (for ignoring)
        if ((reasons != null) && (reasons.size() > 0)) {
            for (String ignore : reasons) {
                if (reason.contains(ignore)) {
                    // This kick reason is in the list
                    return true;
                }
            }
        }
        return false;
    }
}
