package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.WarpsManager;
import net.cubespace.geSuit.objects.Location;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class WarpsMessageListener extends MessageListener {

    public WarpsMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.WARP_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        var conn = getServerConnection(event);
        if (conn == null) return;
        String serverName = conn.getServer().getServerInfo().getName();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String task = in.readUTF();

        if (task.equals("WarpPlayer")) {
            WarpsManager.sendPlayerToWarp(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean());
            return;
        }
        if (task.equals("GetWarpsList")) {
            WarpsManager.getWarpsList(in.readUTF(), in.readBoolean(), in.readBoolean(), in.readBoolean(), in.readBoolean());
            return;
        }
        if (task.equals("SetWarp")) {
            WarpsManager.setWarp(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), new Location(serverName, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean(), in.readBoolean());
            return;
        }
        if (task.equals("SetWarpDesc")) {
            WarpsManager.setWarpDesc(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
            return;
        }
        if (task.equals("SilentWarpPlayer")) {
            WarpsManager.sendPlayerToWarp(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean(), false);
            return;
        }
        if (task.equals("DeleteWarp")) {
            WarpsManager.deleteWarp(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
            return;
        }
        if (task.equals("SendVersion")) {
            LoggingManager.log(in.readUTF());
        }
    }
}
