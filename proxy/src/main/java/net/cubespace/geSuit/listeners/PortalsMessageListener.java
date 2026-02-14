package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.PortalManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PortalsMessageListener extends MessageListener {

    public PortalsMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.PORTAL_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        var conn = getServerConnection(event);
        if (conn == null) return;
        var server = conn.getServer();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String task = in.readUTF();

        switch (task) {
            case "TeleportPlayer":
                PortalManager.teleportPlayer(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF(), in.readBoolean());
                break;
            case "ListPortals":
                PortalManager.listPortals(PlayerManager.getPlayer(in.readUTF(), true));
                break;
            case "DeletePortal":
                PortalManager.deletePortal(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
                break;
            case "SetPortal": {
                GSPlayer sender = PlayerManager.getPlayer(in.readUTF(), true);
                boolean selection = in.readBoolean();
                if (!selection) {
                    PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_SELECTION_MADE);
                } else {
                    PortalManager.setPortal(sender, in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(),
                        new Location(server, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble()),
                        new Location(server, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble()));
                }
                break;
            }
            case "RequestPortals":
                PortalManager.getPortals(server);
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
        }
        in.close();
    }
}
