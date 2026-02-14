package net.cubespace.geSuit.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;

public class SpawnListener {

    @Subscribe
    public void onPostLogin(PostLoginEvent e) {
        if (ConfigManager.spawn.ForceAllPlayersToProxySpawn && !SpawnManager.newPlayers.contains(e.getPlayer())) {
            if (SpawnManager.doesProxySpawnExist()) {
                SpawnManager.sendPlayerToProxySpawn(PlayerManager.getPlayer(e.getPlayer().getUsername(), true));
            } else {
                geSuit.getInstance().getLogger().warn("Wanted to use ForceAllPlayersToProxySpawn without a Proxy Spawn set");
            }
        }
    }
}
