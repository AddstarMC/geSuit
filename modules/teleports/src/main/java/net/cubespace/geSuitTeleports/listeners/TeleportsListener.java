package net.cubespace.geSuitTeleports.listeners;

import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuitTeleports.geSuitTeleports;
import net.cubespace.geSuitTeleports.managers.TeleportsManager;
import net.cubespace.geSuiteSpawn.managers.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class TeleportsListener implements Listener {

    private geSuitTeleports instance;
    private TeleportsManager manager;
    private SpawnManager spawnsManager;

    public TeleportsListener(TeleportsManager manager, SpawnManager spawnManager, geSuitTeleports pl) {
		super();
        this.manager = manager;
        instance = pl;
        spawnsManager = spawnManager;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void playerConnect(PlayerSpawnLocationEvent e){
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs

		// Check if there's any pending teleports for the player
		Location loc = manager.getPendingTeleportLocation(e.getPlayer(), e.getSpawnLocation().getWorld().getSpawnLocation());
		if (loc != null) {
			e.setSpawnLocation(loc);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent e){
        if (!manager.getUtil().worldGuardTpAllowed(e.getTo(), e.getPlayer())) { //cancel the event if the location is blocked
			e.setCancelled(true);
			e.setTo(e.getFrom());
			return;
		}
		if(e.getCause() != TeleportCause.PLUGIN && e.getCause() != TeleportCause.COMMAND){
			return;
		}
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
		
		if(TeleportsManager.ignoreTeleport.contains(e.getPlayer())){
			TeleportsManager.ignoreTeleport.remove(e.getPlayer());
			return;
		}
        manager.sendTeleportBackLocation(e.getPlayer(), false);
	}
	
	@EventHandler
	public void playerLeave(PlayerQuitEvent e){
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs
		TeleportsManager.RemovePlayer(e.getPlayer());
		boolean empty = false;
		if(Bukkit.getOnlinePlayers().size() == 1){
			empty = true;
		}
        manager.sendTeleportBackLocation(e.getPlayer(), empty);
	}
	
        @EventHandler(ignoreCancelled = true)
        public void playerDeath(PlayerDeathEvent e){
                if (e.getEntity().hasMetadata("NPC")) return; // Ignore NPCs
        manager.sendDeathBackLocation(e.getEntity());
        TeleportsManager.ignoreTeleport.add(e.getEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void playerRespawn(PlayerRespawnEvent e){
                TeleportsManager.ignoreTeleport.remove(e.getPlayer());
        }

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void playerJoin(final PlayerJoinEvent e) {
		if (e.getPlayer().hasMetadata("NPC")) return; // Ignore NPCs

		// Check if there's any pending teleports for the player
		Player p = e.getPlayer();
		Location loc = manager.getPendingTeleportLocation(p, e.getPlayer().getWorld().getSpawnLocation());
		if (loc != null) {
			p.teleport(loc);
		} else {
			// This is to prevent recording the back location when teleporting across servers, on the destination server
			TeleportsManager.ignoreTeleport.add(p);
		}
		Bukkit.getScheduler().runTaskLater(instance, () -> TeleportsManager.ignoreTeleport.remove(p), 20);
        }
}
