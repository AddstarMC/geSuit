package net.cubespace.geSuitPortals.listeners;

import net.cubespace.geSuitPortals.geSuitPortals;
import net.cubespace.geSuitPortals.managers.PortalsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PortalsMessageListener implements PluginMessageListener {
    private final PortalsManager manager;
    private final geSuitPortals instance;

    public PortalsMessageListener(PortalsManager manager, geSuitPortals instance) {
        this.manager = manager;
        this.instance = instance;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String task = in.readUTF();
            switch (task) {
                case "SendPortal":
                    manager.addPortal(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()));
                    break;
                case "DeletePortal":
                    manager.removePortal(in.readUTF());
                    break;
                case "GetVersion": {
                    String name = null;
                    try {
                        name = in.readUTF();
                    } catch (IOException ignored) {

                    }
                    if (name != null) {
                        @SuppressWarnings("deprecation") Player p = Bukkit.getPlayer(name);
                        p.sendMessage(ChatColor.RED + "Portals - " + ChatColor.GOLD + instance.getDescription().getVersion());
                    }
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Portals - " + ChatColor.GOLD + instance.getDescription().getVersion());
                    manager.sendVersion();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
