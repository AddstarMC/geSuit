package net.cubespace.geSuitPortals.managers;

import com.sk89q.worldedit.math.BlockVector3;
import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.DataManager;
import net.cubespace.geSuitPortals.objects.Portal;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PortalsManager extends DataManager {

    public static boolean RECEIVED = false;
    public static HashMap<World, ArrayList<Portal>> PORTALS = new HashMap<>();
    public static HashMap<String, Location> pendingTeleports = new HashMap<>();

    public PortalsManager(BukkitModule instance) {
        super(instance);
    }

    public void deletePortal(String name, String string) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("DeletePortal");
            out.writeUTF(name);
            out.writeUTF(string);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void removePortal(String name) {
        Portal p = getPortal( name );
        System.out.println( "removing portal " + name );
        if ( p != null ) {
            PORTALS.get( p.getWorld() ).remove( p );
            p.clearPortal();
        }
    }

    public Portal getPortal(String name) {
        for ( ArrayList<Portal> list : PORTALS.values() ) {
            for ( Portal p : list ) {
                if ( p.getName().equals( name ) ) {
                    return p;
                }
            }
        }
        return null;
    }

    public void getPortalsList(String name) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("ListPortals");
            out.writeUTF(name);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void teleportPlayer(Player p, Portal portal) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("TeleportPlayer");
            out.writeUTF(p.getName());
            out.writeUTF(portal.getType());
            out.writeUTF(portal.getDestination());
            out.writeBoolean(p.hasPermission("gesuit.portals.portal." + portal.getName()) || p.hasPermission("gesuit.portals.portal.*"));
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void setPortal(CommandSender sender, String name, String type, String dest,
                          String fill ) {

        Player p = ( Player ) sender;
        Region region;
        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
            region = session.getRegionSelector(session.getSelectionWorld()).getRegion();
        } catch (IncompleteRegionException exception) {
            exception.printStackTrace();
            return;
        }

        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetPortal");
            out.writeUTF(sender.getName());
            if (!(region instanceof CuboidRegion)) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeUTF(name);
                out.writeUTF(type);
                out.writeUTF(dest);
                out.writeUTF(fill);
                BlockVector3 max = region.getMaximumPoint();
                BlockVector3 min = region.getMinimumPoint();
                out.writeUTF(region.getWorld().getName());
                out.writeDouble(max.getX());
                out.writeDouble(max.getY());
                out.writeDouble(max.getZ());
                out.writeUTF(region.getWorld().getName());
                out.writeDouble(min.getX());
                out.writeDouble(min.getY());
                out.writeDouble(min.getZ());
            }
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void addPortal(String name, String type, String dest, String filltype,
                          Location max, Location min ) {
        if ( max.getWorld() == null ) {
            Bukkit.getConsoleSender().sendMessage( ChatColor.RED + "World does not exist portal " + name + " will not load :(" );
            return;
        }
        Portal portal = new Portal( name, type, dest, filltype, max, min );
        ArrayList<Portal> ps = PORTALS.computeIfAbsent(max.getWorld(), k -> new ArrayList<>());
        ps.add( portal );
        portal.fillPortal();
    }

    public void requestPortals() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("RequestPortals");
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    
    }

}
