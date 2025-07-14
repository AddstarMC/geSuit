package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.EnableBukkitDebug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.protocol.packet.Chat;

import java.util.UUID;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class DebugCommand extends Command
{

    public DebugCommand()
    {
        super("gsdebug");
    }

    private boolean ppvalid = false;
    private boolean gsvalid = false;
    private String sname = "";

    @Override
    public void execute(CommandSender sender, String[] args)
    {
    	if (!(sender.hasPermission("gesuit.debug") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);
            return;
        }

        String action = "help";
        if (args.length > 0) {
            action = args[0];
        }
        switch (action) {
            case "onlineplayers":
                // Useful for troubleshooting issues with the onlinePlayers map
                PlayerManager.sendMessageToTarget(sender, "List of entries in onlinePlayers:");
                for (String player : PlayerManager.onlinePlayers.keySet()) {
                    GSPlayer gs = PlayerManager.onlinePlayers.get(player);
                    gsvalid = false;
                    ppvalid = false;
                    sname = "";
                    getPlayer(gs);
                    PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + player +
                            ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
                            ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
                            ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
                }
                break;
            case "cachedplayers":
                // Useful for troubleshooting issues with the onlinePlayers map
                PlayerManager.sendMessageToTarget(sender, "List of entries in cachedplayers:");
                for (UUID uuid : PlayerManager.cachedPlayers.keySet()) {
                    GSPlayer gs = PlayerManager.cachedPlayers.get(uuid);
                    gsvalid = false;
                    ppvalid = false;
                    sname = "";
                    getPlayer(gs);
                    PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + uuid +
                            ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" + ChatColor.AQUA + " (" + gs.getName() + ")" : ChatColor.RED + "no") +
                            ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
                            ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
                }
                break;
            case "bukkitplugins":
                if (args.length == 2) {
                    String server = args[1];
                    if (server.equalsIgnoreCase("all")) {
                        for (ServerInfo serverInfo :
                                ProxyServer.getInstance().getConfigurationAdapter().getServers().values()) {
                            EnableBukkitDebug.execute(serverInfo);
                        }
                    } else {
                        ServerInfo s = ProxyServer.getInstance().getServerInfo(server);
                        if (s != null) {
                            EnableBukkitDebug.execute(s);
                        } else {
                            PlayerManager.sendMessageToTarget(sender, "ERROR: Server " + server + " not found");
                        }
                    }
                } else {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: bukkitplugins requires " +
                            "parameter either all or servername");
                }
                break;
            case "pluginmsg":
                if (args.length == 2) {
                    String server = args[1];
                    if (server.equalsIgnoreCase("all")) {
                        geSuit.getInstance().setDebugEnabled(2);
                        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: ALL");
                    } else if (server.equalsIgnoreCase("gesuit")) {
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
                // Send the list of spawns to the specified server
                // This is useful for troubleshooting issues with the spawn system
                // and for ensuring that the spawn list is up-to-date on all servers
                if (args.length < 2) {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: Usage /gsdebug sendspawns <servername>");
                    return;
                }
                ServerInfo s = ProxyServer.getInstance().getServerInfo(args[1]);
                if (s == null) {
                    PlayerManager.sendMessageToTarget(sender, "ERROR: Server " + args[1] + " not found");
                    return;
                }
                PlayerManager.sendMessageToTarget(sender, "Sending spawn list to " + s.getName());
                SpawnManager.sendSpawns(s);
                break;
            default:
            case "help":
                PlayerManager.sendMessageToTarget(sender, ChatColor.GREEN + "geSuit Debug Commands:");
                PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug pluginmsg <off|gesuit|all>" + ChatColor.WHITE + " - Dump online player list");
                PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug sendspawns <servername>" + ChatColor.WHITE + " - Send spawn list to server");
                PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug onlineplayers" + ChatColor.WHITE + " - Dump online player list");
                PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug cachedplayers" + ChatColor.WHITE + " - Dump cached player list");
                PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug bukkitplugins <all|servername>"
                        + ChatColor.WHITE + " - Enable debugging on all or named server for all gesuit " + "modules");
                break;
		}
    }

    private void getPlayer(GSPlayer player) {
        if (player != null) {
            gsvalid = true;
            ProxiedPlayer pp = player.getProxiedPlayer();
            if (pp != null) {
                ppvalid = true;
                Server s = pp.getServer();
                if ((s != null) && (s.getInfo() != null)) {
                    sname = s.getInfo().getName();
                }
            }
        }
    }
}
