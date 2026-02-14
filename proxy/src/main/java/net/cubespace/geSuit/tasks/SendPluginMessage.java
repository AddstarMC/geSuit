package net.cubespace.geSuit.tasks;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Sends a plugin message to a Velocity backend server. Uses the same channel names
 * as before so Bukkit modules continue to work.
 */
public class SendPluginMessage implements Runnable {

    private final ChannelIdentifier identifier;
    private final ByteArrayOutputStream bytes;
    private final RegisteredServer server;
    private int sendAttempts = 0;
    private final int maxAttempts = 70;
    private final int sendDelay = 75;

    public SendPluginMessage(geSuit.CHANNEL_NAMES channel, RegisteredServer server, ByteArrayOutputStream bytes) {
        this.identifier = ConfigManager.main.enableLegacy ? channel.getLegacyIdentifier() : channel.getIdentifier();
        this.bytes = bytes;
        this.server = server;
    }

    @Override
    public void run() {
        if (server.getPlayersConnected().size() == 0) {
            sendAttempts++;
            if (sendAttempts < maxAttempts) {
                geSuit.getInstance().getProxy().getScheduler()
                    .buildTask(geSuit.getInstance(), this)
                    .delay(sendDelay, TimeUnit.MILLISECONDS)
                    .schedule();
                return;
            }
        }

        if (geSuit.getInstance().isDebugEnabled()) {
            Utilities.dumpPacket(identifier.getId(), "SEND", bytes.toByteArray(), true);
            if (sendAttempts > 0) {
                geSuit.getInstance().DebugMsg("Message waited " + (sendAttempts * sendDelay) + "ms (and " + sendAttempts + " attempts) for a player to be present on " + server.getServerInfo().getName() + " server");
            }
        }

        server.sendPluginMessage(identifier, bytes.toByteArray());
    }
}
