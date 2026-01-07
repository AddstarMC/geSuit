package net.cubespace.geSuiteSpawn.managers;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.DataManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class SpawnManager extends DataManager {
    public static boolean HAS_SPAWNS = false;
    public static HashMap<String, Location> SPAWNS = new HashMap<>();

    public SpawnManager(BukkitModule instance) {
        super(instance);
    }

    public void sendPlayerToProxySpawn(CommandSender sender, boolean silent) {

        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SendToProxySpawn");
            out.writeUTF(sender.getName());
            out.writeBoolean(silent);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void setNewPlayerSpawn(CommandSender sender) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();

        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetNewPlayerSpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(l.getWorld().getName());
            out.writeDouble(l.getX());
            out.writeDouble(l.getY());
            out.writeDouble(l.getZ());
            out.writeFloat(l.getYaw());
            out.writeFloat(l.getPitch());
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void setProxySpawn(CommandSender sender) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();

        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetProxySpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(l.getWorld().getName());
            out.writeDouble(l.getX());
            out.writeDouble(l.getY());
            out.writeDouble(l.getZ());
            out.writeFloat(l.getYaw());
            out.writeFloat(l.getPitch());
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void setServerSpawn(CommandSender sender) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();
        p.getWorld().setSpawnLocation( l.getBlockX(), l.getBlockY(), l.getBlockZ() );
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetServerSpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(l.getWorld().getName());
            out.writeDouble(l.getX());
            out.writeDouble(l.getY());
            out.writeDouble(l.getZ());
            out.writeFloat(l.getYaw());
            out.writeFloat(l.getPitch());
            out.writeBoolean(hasServerSpawn());
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void setWorldSpawn(CommandSender sender) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();
        p.getWorld().setSpawnLocation( l.getBlockX(), l.getBlockY(), l.getBlockZ() );
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetWorldSpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(l.getWorld().getName());
            out.writeDouble(l.getX());
            out.writeDouble(l.getY());
            out.writeDouble(l.getZ());
            out.writeFloat(l.getYaw());
            out.writeFloat(l.getPitch());
            out.writeBoolean(hasWorldSpawn(p.getWorld()));
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }

    }

    public void sendPlayerToServerSpawn(CommandSender sender) {
        Player p = (Player) sender;
        if (hasServerSpawn()) {
            p.teleport(getServerSpawn());
        } else {
            sender.sendMessage(ChatColor.RED + "Error: No server spawn set.");
            instance.getLogger().warning("Error: No server spawn set (player " + p.getName() + ")");
        }
        p.teleport(getServerSpawn());
    }

    public void sendPlayerToWorldSpawn(CommandSender sender) {
        Player p = (Player) sender;
        if (hasWorldSpawn(p.getWorld())) {
            p.teleport(getWorldSpawn(p.getWorld()));
        } else {
            sender.sendMessage(ChatColor.RED + "Error: No world spawn set for this world.");
            instance.getLogger().warning("Error: No world spawn set for world: " + p.getWorld().getName() + " (player " + p.getName() + ")");
        }
    }

    public static Location getPlayerWorldSpawn(Player p) {
        Location spawn = getWorldSpawn(p.getWorld());
        if (spawn == null) {
            return p.getWorld().getSpawnLocation();
        } else {
            return spawn;
        }
    }

    public void delWorldSpawn(CommandSender sender) {
        Player p = ( Player ) sender;
        Location l = p.getLocation();
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("DelWorldSpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(l.getWorld().getName());
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void getSpawns() {
        if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Message \"GetSpawns\" sent to proxy");

        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("GetSpawns");
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public static boolean hasWorldSpawn( World w ) {
        return SPAWNS.containsKey( w.getName() );
    }

    public static Location getWorldSpawn( World w ) {
        return SPAWNS.get( w.getName() );
    }

    public static boolean hasServerSpawn() {
        return SPAWNS.containsKey( "server" );
    }

    public static Location getServerSpawn() {
        return SPAWNS.get( "server" );
    }

    public void sendPlayerToSpawn(CommandSender sender) {
        Player p = (Player) sender;
        Location loc = getSpawnLocation(p);
        if (loc == null) {
            instance.getLogger().warning("Error: No location returned by getSpawnLocation() for player " + p.getName() + "!");
            sendPlayerToProxySpawn(p, true);
        } else {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Teleporting player " + p.getName() + " to spawn at " + loc);
            p.teleport(loc);
        }
    }

    public Location getSpawnLocation(Player p) {
        if (p.hasPermission("gesuit.spawns.spawn.bed") && p.getBedSpawnLocation() != null) {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Using bed spawn for player " + p.getName());
            return p.getBedSpawnLocation();
        } else if (p.hasPermission("gesuit.spawns.spawn.world") && SpawnManager.hasWorldSpawn(p.getWorld())) {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Using world spawn for player " + p.getName());
            return SpawnManager.getWorldSpawn( p.getWorld() );
        } else if (p.hasPermission("gesuit.spawns.spawn.server") && SpawnManager.hasServerSpawn()) {
            if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Using server spawn for player " + p.getName());
            return SpawnManager.getServerSpawn();
        } else if (p.hasPermission("gesuit.spawns.spawn.global")) {
            if (SpawnManager.hasWorldSpawn(p.getWorld())) {
                if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Using global world spawn for player " + p.getName());
                return SpawnManager.getWorldSpawn(p.getWorld());
            } else if (SpawnManager.hasServerSpawn()) {
                if (BukkitModule.isDebug()) instance.getLogger().info("geSuit DEBUG: Using global server spawn for player " + p.getName());
                return SpawnManager.getServerSpawn();
            }
        }
        return null;
    }

    public static void addSpawn( String name, String world, double x, double y, double z, float yaw, float pitch ) {
        SPAWNS.put( name, new Location( Bukkit.getWorld( world ), x, y, z, yaw, pitch ) );

    }

    public static void delWorldSpawn( String worldName) {
        SPAWNS.remove(worldName);

    }

    public void sendPlayerToArgSpawn(CommandSender sender, String spawn, String server) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SendToArgSpawn");
            out.writeUTF(sender.getName());
            out.writeUTF(spawn);
            out.writeUTF(server);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void sendVersion() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SendVersion");
            out.writeUTF(ChatColor.RED + "Spawns - " + ChatColor.GOLD + instance.getDescription().getVersion());
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
