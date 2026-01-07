package net.cubespace.geSuiteSpawn.listeners;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuiteSpawn.geSuitSpawn;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class SpawnListener implements Listener {

    private final SpawnManager manager;
    private final geSuitSpawn instance;

    public SpawnListener(SpawnManager manager, geSuitSpawn instance) {
        this.manager = manager;
        this.instance = instance;
    }

    @EventHandler( priority = EventPriority.LOWEST )
    public void playerJoin( PlayerJoinEvent e ) {
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
        if ( !SpawnManager.HAS_SPAWNS ) {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Spawns are empty, requesting from proxy");
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                if (!SpawnManager.HAS_SPAWNS) {
                    manager.getSpawns();
                }
            }, 30L );
        }

        // Handle new player spawns
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            if ( SpawnManager.hasWorldSpawn( p.getWorld() ) && p.hasPermission( "gesuit.spawns.new.world" ) ) {
                manager.sendPlayerToWorldSpawn(p);
            } else if ( SpawnManager.hasServerSpawn() && p.hasPermission( "gesuit.spawns.new.server" ) ) {
                manager.sendPlayerToServerSpawn(p);
            } else if ( p.hasPermission( "gesuit.spawns.new.global" ) ) {
                manager.sendPlayerToProxySpawn(p, true);
            }
        }
    }

    // If force spawn perm is set, spawn players at world spawn by default; regardless of their last location
    // We need a separate event handler for this so "new spawn" can still override this
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
    public void playerForcedSpawn( PlayerSpawnLocationEvent e ) {
        if (e.getPlayer().hasPermission( "gesuit.spawns.force.spawn")) {
            if (SpawnManager.hasWorldSpawn(e.getPlayer().getWorld())) {
                if (BukkitModule.isDebug()) instance.getLogger().info("Forcing world spawn for " + e.getPlayer().getName() + " in world " + e.getPlayer().getWorld().getName());
                e.setSpawnLocation(SpawnManager.getWorldSpawn(e.getPlayer().getWorld()));
            } else if (SpawnManager.hasServerSpawn()) {
                if (BukkitModule.isDebug()) instance.getLogger().info("Forcing server spawn for " + e.getPlayer().getName() + " in world " + e.getPlayer().getWorld().getName());
                e.setSpawnLocation(SpawnManager.getServerSpawn());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled=true)
    public void playerSpawn(PlayerSpawnLocationEvent e) {
        if (e.getSpawnLocation().getWorld() == null) {
            instance.getLogger().warning("Spawn location is invalid! Sending player to spawn!");
            Location loc = manager.getSpawnLocation(e.getPlayer());
            if (loc != null) {
                e.setSpawnLocation(loc);
            } else {
                manager.sendPlayerToProxySpawn(e.getPlayer(), true);
            }
        }
    }

    // Handle respawn locations (eg . after death)
    // This is set to HIGH because MyWorlds sets respawn locations at NORMAL priority
    // so this needs to run after that to override it to ensure geSuit spawn locations are used
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled=true)
    public void playerRespawn(PlayerRespawnEvent e) {
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
        Location loc = manager.getSpawnLocation(e.getPlayer());
        if (loc != null) {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Teleporting player " + e.getPlayer().getName() + " to spawn at " + loc);
            e.setRespawnLocation(loc);
        } else {
            instance.getLogger().warning("Error: No location returned by getSpawnLocation() for player " + e.getPlayer().getName() + "!");
            manager.sendPlayerToProxySpawn(e.getPlayer(), true);
        }
    }
}
