package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.pluginmessages.TPAFinalise;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;
import net.cubespace.geSuit.pluginmessages.TeleportToPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TeleportManager {
    public static final HashMap<GSPlayer, GSPlayer> pendingTeleportsTPA = new HashMap<>(); // Player ----teleported---> player
    public static final HashMap<GSPlayer, GSPlayer> pendingTeleportsTPAHere = new HashMap<>(); // Player ----teleported---> player
    private static final int expireTime = ConfigManager.teleport.TeleportRequestExpireTime;

    public static void requestToTeleportToPlayer(String player, String target) {
        final GSPlayer bp = PlayerManager.getPlayer(player);
        final GSPlayer bt = PlayerManager.matchOnlinePlayer(target);
        if (playerHasPendingTeleport(bp)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING);
            return;
        }
        if (bt == null) {
            bp.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE.replace("{player}", bp.getName()));
            return;
        }
        if (bp.getName().equals(bt.getName())) {
            bp.sendMessage(ConfigManager.messages.TELEPORT_UNABLE.replace("{player}", bp.getName()));
            return;
        }
        if (!playerIsAcceptingTeleports(bt)) {
            bp.sendMessage(ConfigManager.messages.TELEPORT_UNABLE.replace("{player}", bt.getName()));
            return;
        }
        if (playerHasPendingTeleport(bt)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING_OTHER);
            return;
        }

        // Cross TPA server whitelist handling
        if (!bp.getServer().equals(bt.getServer())) {
	        ArrayList<String> wl = ConfigManager.teleport.TPAWhitelist;

	        if ((!wl.isEmpty()) &&
	        		(!wl.contains(bp.getServer() + ":" + bt.getServer())) &&            // SRC:DST
	        		(!wl.contains(bp.getServer() + ":ALL")) &&                          // SRC:ALL
	        		(!wl.contains("ALL:" + bt.getServer()))) {                          // ALL:DST
	            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_WRONG_SERVER);
	            return;
	        }
        }

        pendingTeleportsTPA.put(bt, bp);
        bp.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_SENT.replace("{player}", bt.getName()));
        bt.sendMessage(ConfigManager.messages.PLAYER_REQUESTS_TO_TELEPORT_TO_YOU.replace("{player}", bp.getName()));
        geSuit.getInstance().getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            if (pendingTeleportsTPA.containsKey(bt)) {
                if (!pendingTeleportsTPA.get(bt).equals(bp)) {
                    return;
                }
                if (bp != null) {
                    bp.sendMessage(ConfigManager.messages.TPA_REQUEST_TIMED_OUT.replace("{player}", bt.getName()));
                }
                pendingTeleportsTPA.remove(bt);
                if (bt != null) {
                    bt.sendMessage(ConfigManager.messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getName()));
                }
            }
        }).delay(expireTime, TimeUnit.SECONDS).schedule();
    }

    public static void requestPlayerTeleportToYou(String player, String target) {
        final GSPlayer bp = PlayerManager.getPlayer(player);
        final GSPlayer bt = PlayerManager.matchOnlinePlayer(target);
        if (playerHasPendingTeleport(bp)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING);
            return;
        }
        if (bt == null) {
            bp.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE.replace("{player}", bp.getName()));
            return;
        }
        if (!playerIsAcceptingTeleports(bt)) {
            bp.sendMessage(ConfigManager.messages.TELEPORT_UNABLE.replace("{player}", bt.getName()));
            return;
        }
        if (playerHasPendingTeleport(bt)) {
            bp.sendMessage(ConfigManager.messages.PLAYER_TELEPORT_PENDING_OTHER.replace("{player}", bt.getName()));
            return;
        }
        pendingTeleportsTPAHere.put(bt, bp);
        bp.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_SENT.replace("{player}", bp.getName()));
        bt.sendMessage(ConfigManager.messages.PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM.replace("{player}", bp.getName()));

        geSuit.getInstance().getProxy().getScheduler().buildTask(geSuit.getInstance(), () -> {
            if (pendingTeleportsTPAHere.containsKey(bt)) {
                if (!pendingTeleportsTPAHere.get(bt).equals(bp)) {
                    return;
                }
                if (bp != null) {
                    bp.sendMessage(ConfigManager.messages.TPAHERE_REQUEST_TIMED_OUT.replace("{player}", bt.getName()));
                }
                pendingTeleportsTPAHere.remove(bt);
                if (bt != null) {
                    bt.sendMessage(ConfigManager.messages.TP_REQUEST_OTHER_TIMED_OUT.replace("{player}", bp.getName()));
                }
            }
        }).delay(expireTime, TimeUnit.SECONDS).schedule();
    }

    public static void acceptTeleportRequest(GSPlayer player) {
        if (pendingTeleportsTPA.containsKey(player)) {
            GSPlayer target = pendingTeleportsTPA.get(player);
            player.sendMessage(ConfigManager.messages.TELEPORT_ACCEPTED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_ACCEPTED.replace("{player}", player.getName()));
            TPAFinalise.execute(target, player);
            pendingTeleportsTPA.remove(player);
        } else if (pendingTeleportsTPAHere.containsKey(player)) {
            GSPlayer target = pendingTeleportsTPAHere.get(player);
            player.sendMessage(ConfigManager.messages.TELEPORT_ACCEPTED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_ACCEPTED.replace("{player}", player.getName()));
            TPAFinalise.execute(player, target);
            pendingTeleportsTPAHere.remove(player);
        } else {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_TELEPORTS);
        }
    }

    public static void denyTeleportRequest(GSPlayer player) {
        if (pendingTeleportsTPA.containsKey(player)) {
            GSPlayer target = pendingTeleportsTPA.get(player);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.TELEPORT_DENIED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_DENIED.replace("{player}", player.getName()));
            pendingTeleportsTPA.remove(player);
        } else if (pendingTeleportsTPAHere.containsKey(player)) {
            GSPlayer target = pendingTeleportsTPAHere.get(player);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.TELEPORT_DENIED.replace("{player}", target.getName()));
            target.sendMessage(ConfigManager.messages.TELEPORT_REQUEST_DENIED.replace("{player}", player.getName()));
            pendingTeleportsTPAHere.remove(player);
        } else {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_TELEPORTS);
        }
    }

    public static boolean playerHasPendingTeleport(GSPlayer player) {
        return pendingTeleportsTPA.containsKey(player) || pendingTeleportsTPAHere.containsKey(player);
    }

    public static boolean playerIsAcceptingTeleports(GSPlayer player) {
        return player.acceptingTeleports();
    }

    public static void setPlayersDeathBackLocation(GSPlayer player, Location loc) {
        player.setDeathBackLocation(loc);
    }

    public static void setPlayersTeleportBackLocation(GSPlayer player, Location loc) {
        if (player != null) {
            player.setTeleportBackLocation(loc);
        }
    }

    public static void sendPlayerToLastBack(GSPlayer player, boolean death, boolean teleport) {
        if (player.hasDeathBackLocation() || player.hasTeleportBackLocation()) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SENT_BACK);
        } else {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_BACK_TP);
        }
        if (death && teleport) {
            if (player.hasDeathBackLocation() || player.hasTeleportBackLocation()) {
                TeleportToLocation.execute(player, player.getLastBackLocation());
            }
        } else if (death) {
            TeleportToLocation.execute(player, player.getDeathBackLocation());
        } else if (teleport) {
            TeleportToLocation.execute(player, player.getTeleportBackLocation());
        }
    }

    public static void togglePlayersTeleports(GSPlayer player) {
        if (player.acceptingTeleports()) {
            player.setAcceptingTeleports(false);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.TELEPORT_TOGGLE_OFF);
        } else {
            player.setAcceptingTeleports(true);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.TELEPORT_TOGGLE_ON);
        }
    }

    public static void tpAll(String sender, String target) {
        GSPlayer p = PlayerManager.getPlayer(sender);
        GSPlayer t = PlayerManager.matchOnlinePlayer(target);

        if (t == null) {
            p.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }

        for (GSPlayer player : PlayerManager.getPlayers()) {
            if (!player.equals(p)) {
                TeleportToPlayer.execute(p, t);
            }

            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.ALL_PLAYERS_TELEPORTED.replace("{player}", t.getName()));
        }
    }

    public static void teleportPlayerToPlayer(String sender, String player, String target, boolean silent, boolean bypass) {
        GSPlayer s = PlayerManager.getPlayer(sender);
        GSPlayer p = PlayerManager.matchOnlinePlayer(player);
        GSPlayer t = PlayerManager.matchOnlinePlayer(target);
        if (p == null || t == null) {
            s.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (!bypass) {
            if (!playerIsAcceptingTeleports(p) || !playerIsAcceptingTeleports(t)) {
                s.sendMessage(ConfigManager.messages.TELEPORT_UNABLE);
                return;
            }
        }

        if (!(sender.equals(player) || sender.equals(target))) {
            s.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED.replace("{player}", p.getName()).replace("{target}", t.getName()));
        }

        TeleportToPlayer.execute(p, t);

        if (!silent) {
            t.sendMessage(ConfigManager.messages.PLAYER_TELEPORTED_TO_YOU.replace("{player}", p.getName()));
        }

        p.sendMessage(ConfigManager.messages.TELEPORTED_TO_PLAYER.replace("{player}", t.getName()));
    }
    public static void sendPlayerToServer(String player, String server) {
        GSPlayer p = PlayerManager.matchOnlinePlayer(player);
        if (p == null || p.getServer().equals(server)) return;
        geSuit.getInstance().getProxy().getServer(server).ifPresent(p::connectTo);
    }
}