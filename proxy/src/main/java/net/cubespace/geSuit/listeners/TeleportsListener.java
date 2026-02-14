package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.LeavingServer;

public class TeleportsListener {

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent e) {
        if (e.getPlayer().getCurrentServer().isEmpty()) return;
        GSPlayer player = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
        if (player == null) player = PlayerManager.getPlayer(e.getPlayer().getUsername());
        if (player == null) {
            geSuit.getInstance().getLogger().warn("Player: " + e.getPlayer() + " could not be found by the Gesuit PlayerManager. Could not execute leaving Server.");
            return;
        }
        LeavingServer.execute(player);
    }
}
