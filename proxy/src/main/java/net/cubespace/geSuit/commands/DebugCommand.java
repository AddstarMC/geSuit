package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.EnableBukkitDebug;

import java.util.UUID;

public class DebugCommand implements SimpleCommand {

    private boolean ppvalid = false;
    private boolean gsvalid = false;
    private String sname = "";

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var sender = inv.source();
        if (!(sender.hasPermission("gesuit.debug") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        String[] args = inv.arguments();
        String action = args.length > 0 ? args[0] : "help";

        switch (action) {
            case "onlineplayers":
                PlayerManager.sendMessageToTarget(sender, "List of entries in onlinePlayers:");
                for (String player : PlayerManager.onlinePlayers.keySet()) {
                    GSPlayer gs = PlayerManager.onlinePlayers.get(player);
                    gsvalid = false;
                    ppvalid = false;
                    sname = "";
                    getPlayer(gs);
                    PlayerManager.sendMessageToTarget(sender, "  &b" + player + "&f -> GS:" + (gsvalid ? "&ayes" : "&cno") + "&f / PP:" + (ppvalid ? "&ayes" : "&cno") + "&f / SRV:" + (!sname.isEmpty() ? "&a" + sname : "&cnone"));
                }
                break;
            case "cachedplayers":
                PlayerManager.sendMessageToTarget(sender, "List of entries in cachedplayers:");
                for (UUID uuid : PlayerManager.cachedPlayers.keySet()) {
                    GSPlayer gs = PlayerManager.cachedPlayers.get(uuid);
                    gsvalid = false;
                    ppvalid = false;
                    sname = "";
                    getPlayer(gs);
                    PlayerManager.sendMessageToTarget(sender, "  &b" + uuid + "&f -> GS:" + (gsvalid ? "&ayes&b (" + gs.getName() + ")" : "&cno") + "&f / PP:" + (ppvalid ? "&ayes" : "&cno") + "&f / SRV:" + (!sname.isEmpty() ? "&a" + sname : "&cnone"));
                }
                break;
            case "bukkitplugins":
                if (args.length == 2) {
                    String server = args[1];
                    if (server.equalsIgnoreCase("all")) {
                        for (var rs : geSuit.getInstance().getProxy().getAllServers()) {
                            EnableBukkitDebug.execute(rs.getServerInfo().getName());
                        }
                    } else {
                        geSuit.getInstance().getProxy().getServer(server).ifPresentOrElse(
                            rs -> EnableBukkitDebug.execute(rs.getServerInfo().getName()),
                            () -> PlayerManager.sendMessageToTarget(sender, "ERROR: Server " + server + " not found"));
                    }
                } else {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: bukkitplugins requires parameter either all or servername");
                }
                break;
            case "pluginmsg":
                if (args.length == 2) {
                    String mode = args[1];
                    if (mode.equalsIgnoreCase("all")) {
                        geSuit.getInstance().setDebugEnabled(2);
                        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: ALL");
                    } else if (mode.equalsIgnoreCase("gesuit")) {
                        geSuit.getInstance().setDebugEnabled(1);
                        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: GESUIT");
                    } else {
                        geSuit.getInstance().setDebugEnabled(0);
                        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: OFF");
                    }
                } else {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: Usage /gsdebug pluginmsg <off|gesuit|all>");
                }
                break;
            case "sendspawns":
                if (args.length < 2) {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: Usage /gsdebug sendspawns <servername>");
                    return;
                }
                var serverOpt = geSuit.getInstance().getProxy().getServer(args[1]);
                if (serverOpt.isPresent()) {
                    PlayerManager.sendMessageToTarget(sender, "Sending spawn list to " + serverOpt.get().getServerInfo().getName());
                    SpawnManager.sendSpawns(serverOpt.get());
                } else {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: Server " + args[1] + " not found");
                }
                break;
            default:
            case "help":
                PlayerManager.sendMessageToTarget(sender, "&ageSuit Debug Commands:");
                PlayerManager.sendMessageToTarget(sender, "&e/gsdebug pluginmsg <off|gesuit|all>&f - Dump online player list");
                PlayerManager.sendMessageToTarget(sender, "&e/gsdebug sendspawns <servername>&f - Send spawn list to server");
                PlayerManager.sendMessageToTarget(sender, "&e/gsdebug onlineplayers&f - Dump online player list");
                PlayerManager.sendMessageToTarget(sender, "&e/gsdebug cachedplayers&f - Dump cached player list");
                PlayerManager.sendMessageToTarget(sender, "&e/gsdebug bukkitplugins <all|servername>&f - Enable debugging on all or named server for all gesuit modules");
                break;
        }
    }

    private void getPlayer(GSPlayer player) {
        if (player != null) {
            gsvalid = true;
            Player pp = player.getPlayer();
            if (pp != null) {
                ppvalid = true;
                sname = pp.getCurrentServer().map(sc -> sc.getServerInfo().getName()).orElse("");
            }
        }
    }
}
