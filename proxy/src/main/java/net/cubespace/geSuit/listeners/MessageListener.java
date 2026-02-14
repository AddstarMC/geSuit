package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import net.cubespace.geSuit.geSuit;

/**
 * Base for Velocity plugin message listeners. Only handles messages from backend servers
 * (ServerConnection). Channel names match those used by Bukkit modules for compatibility.
 */
public abstract class MessageListener {

    private final geSuit.CHANNEL_NAMES channelName;
    private final boolean legacy;

    public MessageListener(boolean legacy, geSuit.CHANNEL_NAMES channel) {
        this.legacy = legacy;
        this.channelName = channel;
    }

    /**
     * Returns true if this listener should handle the event (channel matches and source is a server).
     * Caller must set result to handled when true.
     */
    protected boolean eventMatched(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) {
            return false;
        }
        String id = event.getIdentifier().getId();
        if (id.equalsIgnoreCase(channelName.toString())
                || id.equalsIgnoreCase(channelName.getLegacy())
                || (legacy && id.equalsIgnoreCase(channelName.getLegacy().toLowerCase()))) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            return true;
        }
        return false;
    }

    protected static ServerConnection getServerConnection(PluginMessageEvent event) {
        return event.getSource() instanceof ServerConnection ? (ServerConnection) event.getSource() : null;
    }
}
