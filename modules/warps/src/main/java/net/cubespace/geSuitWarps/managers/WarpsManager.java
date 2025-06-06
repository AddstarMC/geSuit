package net.cubespace.geSuitWarps.managers;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class WarpsManager extends DataManager {

    public WarpsManager(BukkitModule instance) {
        super(instance);
    }

    public void warpPlayer(final CommandSender sender, final String senderName, final String warp) {
    	Player p = Bukkit.getPlayer(sender.getName());
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("WarpPlayer");
            out.writeUTF(sender.getName());
            out.writeUTF(senderName);
            out.writeUTF(warp);
            out.writeBoolean(sender.hasPermission("gesuit.warps.warp." + warp.toLowerCase()) || sender.hasPermission("gesuit.warps.warp.*"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
            instance.sendMessage(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWarp(CommandSender sender, String name, boolean hidden, boolean global) {
        Location l = ( ( Player ) sender ).getLocation();
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetWarp");
            out.writeUTF(sender.getName());
            out.writeUTF(name);
            out.writeUTF(l.getWorld().getName());
            out.writeDouble(l.getX());
            out.writeDouble(l.getY());
            out.writeDouble(l.getZ());
            out.writeFloat(l.getYaw());
            out.writeFloat(l.getPitch());
            out.writeBoolean(hidden);
            out.writeBoolean(global);
            instance.sendMessage(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWarpDesc(CommandSender sender, String warpName, String description) {
        Location l = ( ( Player ) sender ).getLocation();
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("SetWarpDesc");
            out.writeUTF(sender.getName());
            out.writeUTF(warpName);
            out.writeUTF(description);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void silentWarpPlayer(final CommandSender sender, final String senderName, final String warp) {
        Player p = Bukkit.getPlayer(sender.getName());
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {
            out.writeUTF("SilentWarpPlayer");
            out.writeUTF(sender.getName());
            out.writeUTF(senderName);
            out.writeUTF(warp);
            out.writeBoolean(sender.hasPermission("gesuit.warps.warp." + warp.toLowerCase()) || sender.hasPermission("gesuit.warps.warp.*"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
            instance.sendMessage(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteWarp(CommandSender sender, String warp) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("DeleteWarp");
            out.writeUTF(sender.getName());
            out.writeUTF(warp);
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }


    public void listWarps(CommandSender sender) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream( b )) {
            out.writeUTF("GetWarpsList");
            out.writeUTF(sender.getName());
            out.writeBoolean(sender.hasPermission("gesuit.warps.list.server"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.list.global"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.list.hidden"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
            instance.sendMessage(b);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}