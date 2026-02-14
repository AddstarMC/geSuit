package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Warp;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;
import net.cubespace.geSuit.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WarpsManager {
    private static HashMap<String, Warp> warps = new HashMap<>();

    public static void loadWarpLocations() {
        List<Warp> warps1 = DatabaseManager.warps.getWarps();

        for(Warp warp : warps1) {
            warps.put(warp.getName().toLowerCase(), warp);
        }
    }

	public static void setWarp(GSPlayer sender, String name, Location loc, boolean hidden, boolean global) {
		setWarp(sender, name, loc, hidden, global, "");
	}

    public static void setWarp(GSPlayer sender, String name, Location loc, boolean hidden, boolean global, String description) {
        Warp w;
        if (doesWarpExist(name)) {
            w = warps.get(name.toLowerCase());
            w.setLocation(loc);
            w.setGlobal(global);
            w.setHidden(hidden);
			w.setDescription(description);
            DatabaseManager.warps.updateWarp(w);
            sender.sendMessage(ConfigManager.messages.WARP_UPDATED.replace("{warp}", name));
        } else {
            w = new Warp(name, loc, hidden, global, description);
            warps.put(name.toLowerCase(), w);
            DatabaseManager.warps.insertWarp(w);
            sender.sendMessage(ConfigManager.messages.WARP_CREATED.replace("{warp}", name));
        }
    }

	public static void setWarpDesc(GSPlayer sender, String warpName, String description) {
		Warp w;
		if (doesWarpExist(warpName)) {
			w = warps.get(warpName.toLowerCase());
			w.setDescription(description);
			DatabaseManager.warps.updateWarp(w);
			sender.sendMessage(ConfigManager.messages.WARP_DESCRIPTION_UPDATED.replace("{warp}", warpName));
		} else {
			sender.sendMessage(ConfigManager.messages.WARP_DOES_NOT_EXIST.replace("{warp}", warpName));
		}
	}

	public static void deleteWarp(GSPlayer sender, String warp) {
        Warp w = getWarp(warp);
        warps.remove(w.getName().toLowerCase());
        DatabaseManager.warps.deleteWarp(w.getName());
        sender.sendMessage(ConfigManager.messages.WARP_DELETED.replace("{warp}", warp));
    }

    public static Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public static boolean doesWarpExist(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public static void getWarpsList(String sender, boolean server, boolean global, boolean hidden, boolean bypass) {
        GSPlayer s = PlayerManager.getPlayer(sender);
        if (!(server || global || hidden)) {
            s.sendMessage("&c" + "No warps to display");
        }
        StringBuilder serverString = new StringBuilder("&6" + "Server warps: \n");
        StringBuilder globalString = new StringBuilder("&6" + "Global warps: \n");
        StringBuilder hiddenString = new StringBuilder("&6" + "Hidden warps: \n");

        Map<String, Warp> sorted = new TreeMap<>(warps);
        for (Warp w : sorted.values()) {
            if (w.isGlobal()) {
                globalString.append(w.getName()).append(", ");
            } else if (w.isHidden()) {
                hiddenString.append(w.getName()).append(", ");
            } else if (s.getServer().equals(w.getLocation().getServerName())) {
                serverString.append(w.getName()).append(", ");
            } else if (bypass) {
                globalString.append(w.getName()).append(", ");
            }
        }
        if (server) {
            if (serverString.length() == 17) {
                serverString.append("&c" + " none  ");
            }
            s.sendMessage(serverString.substring(0, serverString.length() - 2));
        }
        if (global) {
            if (globalString.length() == 17) {
                globalString.append("&c" + " none  ");
            }

            s.sendMessage(globalString.substring(0, globalString.length() - 2));
        }
        if (hidden) {
            if (hiddenString.length() == 17) {
                hiddenString.append("&c" + " none  ");
            }
            s.sendMessage(hiddenString.substring(0, hiddenString.length() - 2));
        }
    }

    public static void sendPlayerToWarp(String sender, String player, String warp, boolean permission, boolean bypass) {
        sendPlayerToWarp(sender, player, warp, permission, bypass, true);
    }

    public static void sendPlayerToWarp(String sender, String player, String warp, boolean permission, boolean bypass,
                                        boolean showPlayerWarpedMessage) {
        GSPlayer s = PlayerManager.getPlayer(sender);
        GSPlayer p = PlayerManager.getPlayer(player);
        if (p == null) {
            s.sendMessage(ConfigManager.messages.PLAYER_NOT_ONLINE);
            return;
        }
        if (s == null) {
            s = p;    // If sending from console, pretend the player executed the command
        }

        Warp w = warps.get(warp.toLowerCase());
        if (w == null) {
            s.sendMessage(ConfigManager.messages.WARP_DOES_NOT_EXIST.replace("{warp}", warp));
            return;
        }

        if (!permission) {
            s.sendMessage(Utilities.colorize( ConfigManager.messages.WARP_NO_PERMISSION));
            return;
        }

        if (!w.isGlobal() && !w.isHidden()) {
            if (!w.getLocation().getServerName().equals(p.getServer()) && !bypass) {
                s.sendMessage(ConfigManager.messages.WARP_SERVER);
                return;
            }
        }

        Location l = w.getLocation();

        if (showPlayerWarpedMessage) {
            p.sendMessage(Utilities.colorize( ConfigManager.messages.PLAYER_WARPED.replace("{warp}", w.getDescriptionOrName())));
        }

        if ((!p.equals(s))) {
            s.sendMessage(Utilities.colorize( ConfigManager.messages.PLAYER_WARPED_OTHER.replace("{player}", p.getName()).replace("{warp}", w.getDescriptionOrName())));
        }

        TeleportToLocation.execute(p, l);
    }
}

