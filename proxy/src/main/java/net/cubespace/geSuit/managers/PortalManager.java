package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.objects.Warp;
import net.cubespace.geSuit.pluginmessages.DeletePortal;
import net.cubespace.geSuit.pluginmessages.SendPortal;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalManager {
    private static Map<String, List<Portal>> portals = new HashMap<>();

    public static void loadPortals() {
        portals = DatabaseManager.portals.getPortals();
    }

    public static void getPortals(com.velocitypowered.api.proxy.server.RegisteredServer s) {
        List<Portal> list = portals.get(s.getServerInfo().getName());
        if (list == null) return;
        for (Portal p : list) {
            SendPortal.execute(p);
        }
    }

    public static void setPortal(GSPlayer sender, String name, String type, String dest, String fillType, Location max, Location min) {
        if (!(type.equalsIgnoreCase("warp") || type.equalsIgnoreCase("server"))) {
            sender.sendMessage(ConfigManager.messages.INVALID_PORTAL_TYPE);
            return;
        }
        fillType = fillType.toUpperCase();
        if (!(fillType.equals("AIR") || fillType.equals("LAVA") || fillType.equals("WATER") || fillType.equals("WEB") || fillType.equals("SUGAR_CANE") || fillType.equals("PORTAL") || fillType.equals("END_PORTAL"))) {
            sender.sendMessage(ConfigManager.messages.PORTAL_FILLTYPE);
            return;
        }
        if (type.equalsIgnoreCase("warp")) {
            Warp w = WarpsManager.getWarp(dest.toLowerCase());
            if (w == null) {
                sender.sendMessage(ConfigManager.messages.PORTAL_DESTINATION_NOT_EXIST);
                return;
            }
        } else {
            if (geSuit.getInstance().getProxy().getServer(dest).isEmpty()) {
                sender.sendMessage(ConfigManager.messages.PORTAL_DESTINATION_NOT_EXIST);
                return;
            }
        }
        String serverName = max.getServerName();
        List<Portal> list = portals.computeIfAbsent(serverName, k -> new ArrayList<>());
        Portal p = new Portal(name, serverName, fillType, type, dest, max, min);
        if (doesPortalExist(name)) {
            Portal old = getPortal(name);
            removePortal(old);

            DatabaseManager.portals.updatePortal(p);

            sender.sendMessage(ConfigManager.messages.PORTAL_UPDATED);
        } else {
            DatabaseManager.portals.insertPortal(p);
            sender.sendMessage(ConfigManager.messages.PORTAL_CREATED);
        }


        SendPortal.execute(p);
        list.add(p);
    }

    public static void removePortal(Portal p) {
        List<Portal> list = portals.get(p.getServerName());
        if (list != null) list.remove(p);

        DatabaseManager.portals.deletePortal(p.getName());

        DeletePortal.execute(p);
    }

    public static void deletePortal(GSPlayer sender, String portal) {
        if (!doesPortalExist(portal)) {
            sender.sendMessage(ConfigManager.messages.PORTAL_DOES_NOT_EXIST);
            return;
        }

        Portal p = getPortal(portal);
        removePortal(p);

        sender.sendMessage(ConfigManager.messages.PORTAL_DELETED);
    }

    public static Portal getPortal(String name) {
        for (List<Portal> list : portals.values()) {
            for (Portal p : list) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
        }

        return null;
    }

    public static boolean doesPortalExist(String name) {
        for (List<Portal> list : portals.values()) {
            for (Portal p : list) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void teleportPlayer(GSPlayer p, String type, String dest, boolean perm) {
        if (!perm) {
            p.sendMessage(Utilities.colorize(ConfigManager.messages.PORTAL_NO_PERMISSION));
            return;
        }
        if (type.equalsIgnoreCase("warp")) {
            Warp w = WarpsManager.getWarp(dest.toLowerCase());
            if (w == null) {
                p.sendMessage(ConfigManager.messages.PORTAL_DESTINATION_NOT_EXIST);
            } else {
                TeleportToLocation.execute(p, w.getLocation());
            }
        } else {
            var serverOpt = geSuit.getInstance().getProxy().getServer(dest);
            if (serverOpt.isEmpty()) {
                p.sendMessage(ConfigManager.messages.PORTAL_DESTINATION_NOT_EXIST);
                return;
            }
            if (!dest.equals(p.getServer())) {
                p.connectTo(serverOpt.get());
            }
        }
    }

    public static void listPortals(GSPlayer p) {
        for (String serverName : portals.keySet()) {
            StringBuilder message = new StringBuilder();
            message.append("&6").append(serverName).append(": ").append("&r");
            List<Portal> list = portals.get(serverName);
            if (list != null) {
                for (Portal portal : list) {
                    message.append(portal.getName()).append(", ");
                }
            }
            p.sendMessage(Utilities.colorize(message.toString()));
        }
    }
}
