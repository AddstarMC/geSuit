package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;

public class AdminMessageListener extends MessageListener {

    public AdminMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.ADMIN_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) {
        if (geSuit.getInstance().isDebugEnabled()) {
            String tag = event.getIdentifier().getId();
            if (geSuit.getInstance().getDebugLevel() == 2) {
                Utilities.dumpPacket(tag, "RECV", event.getData(), true);
            } else {
                if (tag.startsWith("gesuit:")) {
                    Utilities.dumpPacket(tag, "RECV", event.getData(), false);
                }
            }
        }
        eventMatched(event);
    }
}
