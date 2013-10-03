package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.minecraftdimensions.bungeesuiteportals.managers.PermissionsManager;
import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;


public class PlayerLoginListener implements Listener{
	
	 @EventHandler( priority = EventPriority.LOWEST )
	    public void playerConnect( PlayerJoinEvent e ) {
		 if(!PortalsManager.RECEIVED){
			 PortalsManager.RECEIVED = true;
			 PortalsManager.requestPortals();
		 }
	        if ( PortalsManager.pendingTeleports.containsKey( e.getPlayer().getName() ) ) {
	            Location l = PortalsManager.pendingTeleports.get( e.getPlayer().getName() );
	            e.getPlayer().teleport( l );
	        }
	    }

	    @EventHandler( priority = EventPriority.NORMAL )
	    public void setPermissionGroup( final PlayerLoginEvent e ) {
	        if ( e.getPlayer().hasPermission( "bungeesuite.*" ) ) {
	            PermissionsManager.addAllPermissions( e.getPlayer() );
	        } else if ( e.getPlayer().hasPermission( "bungeesuite.admin" ) ) {
	            PermissionsManager.addAdminPermissions( e.getPlayer() );
	        } else if ( e.getPlayer().hasPermission( "bungeesuite.user" ) ) {
	            PermissionsManager.addUserPermissions( e.getPlayer() );
	        }
	    }

}
