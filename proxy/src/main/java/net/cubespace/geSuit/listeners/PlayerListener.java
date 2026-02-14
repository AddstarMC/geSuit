package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.GeoIPManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PlayerListener {

    @Subscribe
    public EventTask onLogin(LoginEvent event) {
        return EventTask.withContinuation(cont -> {
            PlayerManager.initPlayer(
                event.getPlayer(),
                cont::resume,
                reason -> event.setResult(com.velocitypowered.api.event.ResultedEvent.ComponentResult.denied(reason))
            );
        });
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent e) {
        Player player = e.getPlayer();
        String ip = player.getRemoteAddress() != null ? player.getRemoteAddress().toString() : "unknown";
        LoggingManager.log("Player " + player.getUsername() + " (" + player.getUniqueId() + ") connected from " + ip);
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent e) {
        // Only run on first server connection (not server switch)
        if (e.getPreviousServer() != null) return;
        Player player = e.getPlayer();
        if (PlayerManager.getPlayer(player.getUsername()) == null) {
            if (geSuit.getInstance().getProxy().getPlayer(player.getUniqueId()).isEmpty()) {
                LoggingManager.log(LegacyComponentSerializer.legacySection().serialize(net.kyori.adventure.text.Component.text("Warning: ServerConnectedEvent called but player is not online any more.")));
                return;
            }
            final GSPlayer p = PlayerManager.confirmJoin(player);
            p.setServer(e.getPlayer().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(""));

            if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_CONNECT_PROXY.replace("{player}", p.getName()));
            }
            final boolean newspawn = p.isNewSpawn();

            if ((!p.isFirstJoin()) && newspawn) {
                SpawnManager.sendPlayerToNewPlayerSpawn(p);
                p.setNewSpawn(false);
            }

            String[] alt = null;
            if (ConfigManager.bans.ShowAltAccounts) {
                alt = DatabaseManager.players.getAltPlayer(p.getUuid(), p.getIp(), p.isFirstJoin());
            }

            DatabaseManager.players.updatePlayer(p);

            if (ConfigManager.main.MOTD_Enabled && (p.firstConnect() || newspawn)) {
                geSuit.getInstance().getProxy().getScheduler()
                    .buildTask(geSuit.getInstance(), () -> {
                        if (geSuit.getInstance().getProxy().getPlayer(player.getUniqueId()).isPresent()) {
                            String motd = (p.isFirstJoin() || newspawn) ? ConfigManager.motdNew.getMOTD() : ConfigManager.motd.getMOTD();
                            PlayerManager.sendMessageToTarget(player.getUsername(), motd.replace("{player}", p.getName()));
                        }
                    })
                    .delay(500, TimeUnit.MILLISECONDS)
                    .schedule();
            }

            p.connected();

            final String[] fAlt = alt;
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), () -> {
                    if (ConfigManager.bans.ShowAltAccounts && fAlt != null) {
                        boolean bannedAlt = ConfigManager.bans.ShowBannedAltAccounts && DatabaseManager.bans.isPlayerBanned(fAlt[0], fAlt[1], null);
                        String msg = bannedAlt
                            ? ConfigManager.messages.PLAYER_BANNED_ALT_JOIN.replace("{player}", p.getName()).replace("{alt}", fAlt[0]).replace("{ip}", p.getIp())
                            : ConfigManager.messages.PLAYER_ALT_JOIN.replace("{player}", p.getName()).replace("{alt}", fAlt[0]).replace("{ip}", p.getIp());
                        Utilities.sendOnChatChannel(ConfigManager.main.ChatControlChannel, msg);
                    }
                    if (ConfigManager.bans.GeoIP.ShowOnLogin && player.getRemoteAddress() != null && player.getRemoteAddress().getAddress() != null) {
                        String location = GeoIPManager.lookup(player.getRemoteAddress().getAddress());
                        String msg = location != null
                            ? ConfigManager.messages.PLAYER_GEOIP.replace("{player}", p.getName()).replace("{location}", location)
                            : ConfigManager.messages.PLAYER_NOGEOIP.replace("{player}", p.getName());
                        Utilities.sendOnChatChannel(ConfigManager.main.ChatControlChannel, msg);
                    }
                    PlayerManager.updateTracking(p);
                })
                .delay(100, TimeUnit.MILLISECONDS)
                .schedule();
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        int dcTime = ConfigManager.main.PlayerDisconnectDelay;
        Player player = e.getPlayer();
        final GSPlayer p = PlayerManager.cachedPlayers.remove(player.getUniqueId());
        if (p == null) return;

        if (dcTime > 0) {
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), () -> {
                    if (!PlayerManager.kickedPlayers.contains(player)) {
                        if (ConfigManager.main.BroadcastProxyConnectionMessages) {
                            PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getName()));
                        }
                    } else {
                        PlayerManager.kickedPlayers.remove(player);
                    }
                    PlayerManager.unloadPlayer(player.getUsername());
                    DatabaseManager.players.updatePlayer(p);
                    if (ConfigManager.bans.TrackOnTime) {
                        DatabaseManager.ontime.updatePlayerOnTime(p.getName(), p.getUuid(), p.getLoginTime(), new Date().getTime());
                    }
                })
                .delay(dcTime, TimeUnit.SECONDS)
                .schedule();
        } else {
            if (!PlayerManager.kickedPlayers.contains(player)) {
                if (ConfigManager.main.BroadcastProxyConnectionMessages && PlayerManager.getPlayer(player.getUsername()) != null) {
                    PlayerManager.sendBroadcast(ConfigManager.messages.PLAYER_DISCONNECT_PROXY.replace("{player}", p.getName()));
                }
            } else {
                PlayerManager.kickedPlayers.remove(player);
            }
            PlayerManager.unloadPlayer(player.getUsername());
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), () -> {
                    DatabaseManager.players.updatePlayer(p);
                    if (ConfigManager.bans.TrackOnTime) {
                        DatabaseManager.ontime.updatePlayerOnTime(p.getName(), p.getUuid(), p.getLoginTime(), new Date().getTime());
                    }
                })
                .delay(1, TimeUnit.MILLISECONDS)
                .schedule();
        }
    }
}
