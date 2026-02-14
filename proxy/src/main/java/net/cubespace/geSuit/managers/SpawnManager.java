package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Spawn;
import net.cubespace.geSuit.pluginmessages.DelWorldSpawn;
import net.cubespace.geSuit.pluginmessages.SendSpawn;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.ArrayList;
import java.util.List;

public class SpawnManager {
    public static Location NewPlayerSpawn;
    public static Location ProxySpawn;

    public static ArrayList<Player> newPlayers = new ArrayList<>();

    public static void loadSpawns() {
        ProxySpawn = DatabaseManager.spawns.getSpawn("ProxySpawn");
        NewPlayerSpawn = DatabaseManager.spawns.getSpawn("NewPlayerSpawn");
    }

    public static void delWorldSpawn(GSPlayer p, RegisteredServer server, String world) {
        DatabaseManager.spawns.deleteWorldSpawn(server.getServerInfo().getName(), world);
        PlayerManager.sendMessageToTarget(p, ConfigManager.messages.SPAWN_DELETED);
        DelWorldSpawn.execute(server, world);
    }

    public static boolean doesProxySpawnExist() {
        return ProxySpawn != null;
    }

    public static boolean doesNewPlayerSpawnExist() {
        return NewPlayerSpawn != null;
    }

    public static void sendPlayerToProxySpawn(GSPlayer player) {
        if (!doesProxySpawnExist()) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SPAWN_DOES_NOT_EXIST);
            return;
        }

        TeleportToLocation.execute(player, ProxySpawn);
    }


    public static void sendPlayerToNewPlayerSpawn(GSPlayer player) {
        if (!doesNewPlayerSpawnExist()) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SPAWN_DOES_NOT_EXIST);
            return;
        }

        TeleportToLocation.execute(player, NewPlayerSpawn);
    }


    public static void sendSpawns(RegisteredServer s) {
        List<Spawn> spawnList = DatabaseManager.spawns.getSpawnsForServer(s.getServerInfo().getName());

        for (Spawn spawn : spawnList) {
            SendSpawn.execute(spawn);
        }
    }

    private static void setSpawn(GSPlayer player, Spawn spawn, boolean exists) {
        if (exists) {
            DatabaseManager.spawns.updateSpawn(spawn);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SPAWN_UPDATED);
        } else {
            DatabaseManager.spawns.insertSpawn(spawn);
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SPAWN_SET);
        }

        SendSpawn.execute(spawn);
    }

    public static void setServerSpawn(GSPlayer p, Location l, boolean exists) {
        Spawn spawn = new Spawn("server", l);

        setSpawn(p, spawn, exists);
    }

    public static void setWorldSpawn(GSPlayer p, Location l, boolean exists) {
        // Spawn name is the world name
        Spawn spawn = new Spawn(l.getWorld(), l);

        setSpawn(p, spawn, exists);
    }

    public static void setNewPlayerSpawn(GSPlayer p, Location l) {
        Spawn spawn = new Spawn("NewPlayerSpawn", l);

        if (NewPlayerSpawn != null) {
            DatabaseManager.spawns.updateSpawn(spawn);
            p.sendMessage(ConfigManager.messages.SPAWN_UPDATED);
        } else {
            DatabaseManager.spawns.insertSpawn(spawn);
            p.sendMessage(ConfigManager.messages.SPAWN_SET);
        }

        NewPlayerSpawn = l;
    }

    public static void setProxySpawn(GSPlayer p, Location l) {
        Spawn spawn = new Spawn("ProxySpawn", l);

        if (ProxySpawn != null) {
            DatabaseManager.spawns.updateSpawn(spawn);
            p.sendMessage(ConfigManager.messages.SPAWN_UPDATED);
        } else {
            DatabaseManager.spawns.insertSpawn(spawn);
            p.sendMessage(ConfigManager.messages.SPAWN_SET);
        }

        ProxySpawn = l;
    }

    public static void sendPlayerToArgSpawn(GSPlayer player, String spawn, String server) {
        Location targetSpawn;
        if (server.isEmpty()) {
            targetSpawn = DatabaseManager.spawns.getSpawn(spawn);
        } else {
            targetSpawn = DatabaseManager.spawns.getServerSpawn(spawn, server);
        }

        if (targetSpawn == null) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SPAWN_DOES_NOT_EXIST);
            return;
        }

        TeleportToLocation.execute(player, targetSpawn);
    }

    public static void sendPlayerToNewPlayerSpawn(String sentBy, String player) {
        GSPlayer sender = PlayerManager.getPlayer(sentBy);
        GSPlayer target = PlayerManager.matchOnlinePlayer(player);
        if (target == null) { //player is not ONLINE
            target = DatabaseManager.players.loadPlayer(player);
            if (target == null) { //Player NOT Found offline either
                sender.sendMessage("We could not find a player matching: " + player);
            } else { //Set OFFLINE to logon at new spawn
                PlayerManager.sendtoNewSpawn(target);
                sender.sendMessage("Offline player: " + target.getName() + " will spawn in at the new player spawn next logon");
            }
        } else {
            sendPlayerToNewPlayerSpawn(target);
            sender.sendMessage(target.getName() + " has been sent to the New Player Spawn");
            target.sendMessage("You have been sent back to the new player spawn.  You will need to follow the process for a new player again.  Agreeing to any server rules or requirements to be allowed to play.");
        }
    }

    public static void sendPlayerToNewPlayerSpawn(com.velocitypowered.api.command.CommandSource sender, String player) {
        GSPlayer target = PlayerManager.matchOnlinePlayer(player);
        if (target == null) {
            target = DatabaseManager.players.loadPlayer(player);
            if (target == null) {
                PlayerManager.sendMessageToTarget(sender, "We could not find a player matching: " + player);
            } else {
                PlayerManager.sendtoNewSpawn(target);
                PlayerManager.sendMessageToTarget(sender, target.getName() + " has been sent to the New Player Spawn");
            }
        } else {
            sendPlayerToNewPlayerSpawn(target);
            PlayerManager.sendMessageToTarget(sender, target.getName() + " has been sent to the New Player Spawn");
            target.sendMessage("You have been sent back to the new player spawn.  You will need to follow the process for a new player again.  Agreeing to any server rules or requirements to be allowed to play.");
        }
    }
}
