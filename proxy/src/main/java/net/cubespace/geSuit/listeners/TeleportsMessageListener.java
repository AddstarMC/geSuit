package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.TeleportManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TeleportsMessageListener extends MessageListener {

    public TeleportsMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.TELEPORT_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        var conn = getServerConnection(event);
        if (conn == null) return;
        String serverName = conn.getServer().getServerInfo().getName();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String task = in.readUTF();

        if (task.equals("TpAccept")) {
            TeleportManager.acceptTeleportRequest(PlayerManager.getPlayer(in.readUTF(), true));
            return;
        }

        if (task.equals("TeleportToLocation")) {
            GSPlayer player = PlayerManager.getPlayer(in.readUTF(), true);
            String server = in.readUTF();
            String name = (!server.equals("")) ? server : serverName;
            TeleportToLocation.execute(player, new Location(name, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), readFloat(in), readFloat(in)));
            return;
        }

        if (task.equals("PlayersTeleportBackLocation")) {
            GSPlayer player = PlayerManager.getPlayer(in.readUTF());
            if (player != null) {
                var server = conn.getServer();
                TeleportManager.setPlayersTeleportBackLocation(player, new Location(server, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
            }
            return;
        }

        if (task.equals("PlayersDeathBackLocation")) {
            var server = conn.getServer();
            TeleportManager.setPlayersDeathBackLocation(PlayerManager.getPlayer(in.readUTF(), true), new Location(server, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
            return;
        }

        if (task.equals("TeleportToPlayer")) {
            TeleportManager.teleportPlayerToPlayer(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), in.readBoolean());
            return;
        }

        if (task.equals("TpaHereRequest")) {
            TeleportManager.requestPlayerTeleportToYou(in.readUTF(), in.readUTF());
            return;
        }

        if (task.equals("TpaRequest")) {
            TeleportManager.requestToTeleportToPlayer(in.readUTF(), in.readUTF());
            return;
        }

        if (task.equals("TpDeny")) {
            TeleportManager.denyTeleportRequest(PlayerManager.getPlayer(in.readUTF(), true));
            return;
        }

        if (task.equals("TpAll")) {
            TeleportManager.tpAll(in.readUTF(), in.readUTF());
            return;
        }

        if (task.equals("SendPlayerBack")) {
            TeleportManager.sendPlayerToLastBack(PlayerManager.getPlayer(in.readUTF(), true), in.readBoolean(), in.readBoolean());
            return;
        }

        if (task.equals("ToggleTeleports")) {
            TeleportManager.togglePlayersTeleports(PlayerManager.getPlayer(in.readUTF(), true));
            return;
        }

        if (task.equals("SendToServer")) {
            TeleportManager.sendPlayerToServer(in.readUTF(), in.readUTF());
            return;
        }

        if (task.equals("SendVersion")) {
            LoggingManager.log(in.readUTF());
        }
    }

    private static float readFloat(DataInputStream in) {
        try {
            return in.readFloat();
        } catch (IOException e) {
            return 0;
        }
    }
}
