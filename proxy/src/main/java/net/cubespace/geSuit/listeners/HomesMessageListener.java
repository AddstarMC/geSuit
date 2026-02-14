package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.HomesManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HomesMessageListener extends MessageListener {

    public HomesMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.HOME_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        var conn = getServerConnection(event);
        if (conn == null) return;
        String serverName = conn.getServer().getServerInfo().getName();

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String task = in.readUTF();

        switch (task) {
            case "DeleteHome":
                HomesManager.deleteHome(in.readUTF(), in.readUTF());
                break;
            case "DeleteOtherPlayerHome":
                HomesManager.deleteOtherHome(PlayerManager.getPlayer(in.readUTF()), in.readUTF(), in.readUTF());
                break;
            case "SendPlayerHome":
                HomesManager.sendPlayerToHome(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
                break;
            case "SendOtherPlayerHome":
                HomesManager.sendPlayerToOtherHome(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
                break;
            case "SetPlayersHome": {
                String player = in.readUTF();
                GSPlayer gsPlayer = PlayerManager.getPlayer(player, true);
                if (gsPlayer == null) {
                    gsPlayer = DatabaseManager.players.loadPlayer(player);
                    if (gsPlayer == null) {
                        DatabaseManager.players.insertPlayer(new GSPlayer(player, Utilities.getUUID(player), true), "0.0.0.0");
                        gsPlayer = DatabaseManager.players.loadPlayer(player);
                    }
                    gsPlayer.setServer(serverName);
                } else {
                    gsPlayer.setServer(serverName);
                }
                HomesManager.createNewHome(gsPlayer, in.readInt(), in.readInt(), in.readUTF(), new Location(serverName, in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
            }
            case "GetHomesList":
                HomesManager.listPlayersHomes(PlayerManager.getPlayer(in.readUTF(), true), in.readInt());
                break;
            case "GetOtherHomesList":
                HomesManager.listOtherPlayersHomes(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
        }
        in.close();
    }
}
