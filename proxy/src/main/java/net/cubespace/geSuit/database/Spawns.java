package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Spawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Spawns implements IRepository {
    public Location getSpawn(String spawnName) {
        Location location = null;

        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement getSpawn = DatabaseManager.connectionPool.getPreparedStatement("getSpawn", con)) {
            if (getSpawn == null) return null;
            getSpawn.setString(1, spawnName);
            ResultSet res = getSpawn.executeQuery();
            while (res.next()) {
                location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public Location getServerSpawn(String spawnName, String serverName) {
        Location location = null;

        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement getServerSpawn = DatabaseManager.connectionPool.getPreparedStatement("getServerSpawn", con)) {
            getServerSpawn.setString(1, spawnName);
            getServerSpawn.setString(2, serverName);

            ResultSet res = getServerSpawn.executeQuery();
            while (res.next()) {
                location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
            }
            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void deleteWorldSpawn(String server, String world) {

        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement deleteSpawn = DatabaseManager.connectionPool.getPreparedStatement("deleteWorldSpawn", con)) {
            deleteSpawn.setString(1, server);
            deleteSpawn.setString(2, world);

            deleteSpawn.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Spawn> getSpawnsForServer(String server) {
        List<Spawn> spawns = new ArrayList<>();

        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement getSpawnsForServer = DatabaseManager.connectionPool.getPreparedStatement("getSpawnsForServer", con)
        ) {

            getSpawnsForServer.setString(1, server);
            ResultSet res = getSpawnsForServer.executeQuery();
            while (res.next()) {
                Location location = new Location(res.getString("server"), res.getString("world"), res.getDouble("x"), res.getDouble("y"), res.getDouble("z"), res.getFloat("yaw"), res.getFloat("pitch"));
                spawns.add(new Spawn(res.getString("spawnname"), location));
            }
            res.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return spawns;
    }

    public void insertSpawn(Spawn spawn) {

        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement insertSpawn = DatabaseManager.connectionPool.getPreparedStatement("insertSpawn", con)
        ) {
            insertSpawn.setString(1, spawn.getName());
            insertSpawn.setString(2, spawn.getLocation().getServerName());
            insertSpawn.setString(3, spawn.getLocation().getWorld());
            insertSpawn.setDouble(4, spawn.getLocation().getX());
            insertSpawn.setDouble(5, spawn.getLocation().getY());
            insertSpawn.setDouble(6, spawn.getLocation().getZ());
            insertSpawn.setFloat(7, spawn.getLocation().getYaw());
            insertSpawn.setFloat(8, spawn.getLocation().getPitch());

            insertSpawn.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSpawn(Spawn spawn) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement updateSpawn = DatabaseManager.connectionPool.getPreparedStatement("updateSpawn", con)) {
            updateSpawn.setString(1, spawn.getLocation().getWorld());
            updateSpawn.setDouble(2, spawn.getLocation().getX());
            updateSpawn.setDouble(3, spawn.getLocation().getY());
            updateSpawn.setDouble(4, spawn.getLocation().getZ());
            updateSpawn.setFloat(5, spawn.getLocation().getYaw());
            updateSpawn.setFloat(6, spawn.getLocation().getPitch());
            updateSpawn.setString(7, spawn.getName());
            updateSpawn.setString(8, spawn.getLocation().getServerName());

            updateSpawn.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Spawns, "spawnname VARCHAR(100), " +
                "server VARCHAR(100), " +
                "world VARCHAR(100), " +
                "x DOUBLE, " +
                "y DOUBLE, " +
                "z DOUBLE, " +
                "yaw FLOAT, " +
                "pitch FLOAT, " +
                "CONSTRAINT pk_spawnname PRIMARY KEY (spawnname, server)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool connection) {
        connection.addPreparedStatement("getSpawn", "SELECT * FROM "+ ConfigManager.main.Table_Spawns +" WHERE spawnname=?");
        connection.addPreparedStatement("getServerSpawn", "SELECT * FROM "+ ConfigManager.main.Table_Spawns +" WHERE spawnname=? AND server=?");
        connection.addPreparedStatement("getSpawnsForServer", "SELECT * FROM "+ ConfigManager.main.Table_Spawns +" WHERE server=? AND NOT (spawnname = 'NewPlayerSpawn' OR spawnname = 'ProxySpawn')");
        connection.addPreparedStatement("insertSpawn", "INSERT INTO "+ ConfigManager.main.Table_Spawns +" (spawnname, server, world, x, y, z, yaw, pitch) VALUES(?,?,?,?,?,?,?,?)");
        connection.addPreparedStatement("updateSpawn", "UPDATE "+ ConfigManager.main.Table_Spawns +" SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE spawnname = ? AND server = ?");
        connection.addPreparedStatement("deleteWorldSpawn", "DELETE FROM " + ConfigManager.main.Table_Spawns + " WHERE server=? AND world=? AND spawnname = world");


    }

    @Override
    public void checkUpdate() {

    }
}
