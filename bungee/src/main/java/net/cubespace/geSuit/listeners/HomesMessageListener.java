package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.HomesManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class HomesMessageListener extends MessageListener {

    public HomesMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.HOME_CHANNEL);
    }

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;
        DataInputStream in = new DataInputStream( new ByteArrayInputStream( event.getData() ) );

        String task = in.readUTF();

        // TODO: Add input validation! Don't assume all inputs are valid player names (or online)
        switch (task) {
            case "DeleteHome":
                HomesManager.deleteHome(in.readUTF(), in.readUTF());
                break;
            case "DeleteOtherPlayerHome":
                HomesManager.deleteOtherHome(PlayerManager.getPlayer(in.readUTF()), in.readUTF(), in.readUTF());
                break;
            case "SendPlayerHome":  //SendOtherPlayerHome sendPlayerToOtherHome
                HomesManager.sendPlayerToHome(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF());
                break;
            case "SendOtherPlayerHome":
                HomesManager.sendPlayerToOtherHome(PlayerManager.getPlayer(in.readUTF(), true), in.readUTF(), in.readUTF());
                break;
            case "SetPlayersHome":
                String player = in.readUTF();
                GSPlayer gsPlayer = PlayerManager.getPlayer(player, true);

                if (gsPlayer == null) {
                    gsPlayer = DatabaseManager.players.loadPlayer(player);

                    if (gsPlayer == null) {
                        DatabaseManager.players.insertPlayer(new GSPlayer(player, Utilities.getUUID(player), true), "0.0.0.0");
                        gsPlayer = DatabaseManager.players.loadPlayer(player);
                        gsPlayer.setServer(((Server) event.getSender()).getInfo().getName());
                    } else {
                        gsPlayer.setServer(((Server) event.getSender()).getInfo().getName());
                    }
                }

                HomesManager.createNewHome(gsPlayer, in.readInt(), in.readInt(), in.readUTF(), new Location(((Server) event.getSender()).getInfo().getName(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
                break;
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