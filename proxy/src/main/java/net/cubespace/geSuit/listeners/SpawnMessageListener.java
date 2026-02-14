package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.cubespace.geSuit.objects.Location;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SpawnMessageListener extends MessageListener {

    public SpawnMessageListener(boolean legacy) {
        super(legacy, net.cubespace.geSuit.geSuit.CHANNEL_NAMES.SPAWN_CHANNEL);
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
            case "SendToProxySpawn":
                SpawnManager.sendPlayerToProxySpawn(PlayerManager.getPlayer(in.readUTF(), true));
                break;
            case "GetSpawns":
                SpawnManager.sendSpawns(server);
                break;
            case "SetServerSpawn":
                SpawnManager.setServerSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(server.getServerInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean());
                break;
            case "SetWorldSpawn":
                SpawnManager.setWorldSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(server.getServerInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()), in.readBoolean());
                break;
            case "DelWorldSpawn":
                SpawnManager.delWorldSpawn(PlayerManager.getPlayer(in.readUTF(), true), server, in.readUTF());
                break;
            case "SetNewPlayerSpawn":
                SpawnManager.setNewPlayerSpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(server.getServerInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
            case "SetProxySpawn":
                SpawnManager.setProxySpawn(PlayerManager.getPlayer(in.readUTF(), true), new Location(server.getServerInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
            case "SendToArgSpawn":
                SpawnManager.sendPlayerToArgSpawn(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
        }
        in.close();
    }
}
