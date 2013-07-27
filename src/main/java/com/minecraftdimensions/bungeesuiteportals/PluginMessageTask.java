package com.minecraftdimensions.bungeesuiteportals;

import java.io.ByteArrayOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class PluginMessageTask extends BukkitRunnable {
    
    private final BungeeSuitePortals plugin;
    private final ByteArrayOutputStream bytes;
    private final Player player;
    
    public PluginMessageTask(BungeeSuitePortals plugin, Player player, ByteArrayOutputStream bytes) {
        this.plugin = plugin;
        this.bytes = bytes;
        this.player = player;
    }

    public void run() {
        player.sendPluginMessage(plugin, BungeeSuitePortals.OUTGOING_PLUGIN_CHANNEL, bytes.toByteArray());
    }

}