package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.APIManager;
import net.cubespace.geSuit.managers.LoggingManager;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class APIMessageListener extends MessageListener {

    public APIMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.API_CHANNEL);
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
            case "UUIDToPlayerName":
                APIManager.doResolveIDs(server, in.readInt(), in.readUTF());
                break;
            case "PlayerNameToUUID":
                APIManager.doResolveNames(server, in.readInt(), in.readUTF());
                break;
            case "PlayerNameHistory":
                APIManager.doNameHistory(server, in.readInt(), in.readUTF());
                break;
            default:
                LoggingManager.log("WARNING: Unknown API command received: \"" + task + "\"!");
                break;
        }
    }
}
