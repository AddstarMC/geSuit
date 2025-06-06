package net.cubespace.geSuitTeleports.listeners;

import net.cubespace.geSuitTeleports.geSuitTeleports;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TeleportsMessageListener implements PluginMessageListener {
    private TeleportsManager manager;
    private geSuitTeleports instance;

    public TeleportsMessageListener(TeleportsManager manager, geSuitTeleports pl) {
        this.manager = manager;
        instance = pl;
    }

    @Override
    public void onPluginMessageReceived( String channel, Player player, byte[] message ) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String task = in.readUTF();
            if (task.equals("TeleportToPlayer")) {
                // Player1 Player2
                manager.teleportPlayerToPlayer(in.readUTF(), in.readUTF());
            }

            if (task.equals("TeleportToLocation")) {
                // Player World X Y Z Yaw Pitch
                manager.teleportPlayerToLocation(in.readUTF(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat());
            }

            if (task.equals("TeleportAccept")) {
                //noinspection deprecation
                manager.finishTPA(Bukkit.getPlayerExact(in.readUTF()), in.readUTF());
            }

            if (task.equals("LeavingServer")) {
                //noinspection deprecation
                manager.doLeaveServer(Bukkit.getPlayerExact(in.readUTF()));
            }

            if (task.equals("GetVersion")) {
                String name = null;
                try {
                    name = in.readUTF();
                } catch (IOException ignored) {

                }
                if (name != null) {
                    //noinspection deprecation
                    Player p = Bukkit.getPlayer(name);
                    p.sendMessage(ChatColor.RED + "Teleports - " + ChatColor.GOLD + instance.getDescription().getVersion());
                }
                manager.sendVersion();
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Teleports - " + ChatColor.GOLD + instance.getDescription().getVersion());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
