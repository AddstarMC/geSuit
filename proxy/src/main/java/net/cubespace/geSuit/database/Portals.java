package net.cubespace.geSuit.database;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Portal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Portals implements IRepository {
    public Map<String, List<Portal>> getPortals() {
        Map<String, List<Portal>> portalMap = new HashMap<>();

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getPortals = DatabaseManager.connectionPool.getPreparedStatement("getPortals", con)
        ) {
            if (getPortals == null) return portalMap;
            ResultSet res = getPortals.executeQuery();
            while (res.next()) {
                String name = res.getString("portalname");
                String server = res.getString("server");
                String type = res.getString("type");
                String dest = res.getString("destination");
                String world = res.getString("world");
                String fill = res.getString("filltype");
                double xmax = res.getDouble("xmax");
                double xmin = res.getDouble("xmin");
                double ymax = res.getDouble("ymax");
                double ymin = res.getDouble("ymin");
                double zmax = res.getDouble("zmax");
                double zmin = res.getDouble("zmin");

                Portal p = new Portal(name, server, fill, type, dest, new Location(server, world, xmax, ymax, zmax), new Location(server, world, xmin, ymin, zmin));
                List<Portal> list = portalMap.computeIfAbsent(server, k -> new ArrayList<>());

                list.add(p);
            }
            res.close();

            return portalMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deletePortal(String portalName) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement deletePortal = DatabaseManager.connectionPool.getPreparedStatement("deletePortal", con)) {
            deletePortal.setString(1, portalName);
            deletePortal.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertPortal(Portal portal) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement insertPortal = DatabaseManager.connectionPool.getPreparedStatement("insertPortal", con)) {
            insertPortal.setString(1, portal.getName());
            insertPortal.setString(2, portal.getServerName());
            insertPortal.setString(3, portal.getType());
            insertPortal.setString(4, portal.getDest());
            insertPortal.setString(5, portal.getMax().getWorld());
            insertPortal.setString(6, portal.getFillType());
            insertPortal.setInt(7, (int) portal.getMax().getX());
            insertPortal.setInt(8, (int) portal.getMin().getX());
            insertPortal.setInt(9, (int) portal.getMax().getY());
            insertPortal.setInt(10, (int) portal.getMin().getY());
            insertPortal.setInt(11, (int) portal.getMax().getZ());
            insertPortal.setInt(12, (int) portal.getMin().getZ());
            insertPortal.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePortal(Portal portal) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement updatePortal = DatabaseManager.connectionPool.getPreparedStatement("updatePortal", con)
        ) {
            updatePortal.setString(1, portal.getServerName());
            updatePortal.setString(2, portal.getMax().getWorld());
            updatePortal.setString(3, portal.getType());
            updatePortal.setString(4, portal.getFillType());
            updatePortal.setString(5, portal.getDest());
            updatePortal.setInt(6, (int) portal.getMax().getX());
            updatePortal.setInt(7, (int) portal.getMax().getY());
            updatePortal.setInt(8, (int) portal.getMax().getZ());
            updatePortal.setInt(9, (int) portal.getMin().getX());
            updatePortal.setInt(10, (int) portal.getMin().getY());
            updatePortal.setInt(11, (int) portal.getMin().getZ());
            updatePortal.setString(12, portal.getName());

            updatePortal.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Portals, "portalname VARCHAR(100), " +
                "server VARCHAR(100)," +
                "type VARCHAR(20), " +
                "destination VARCHAR(100), " +
                "world VARCHAR(100), " +
                "filltype VARCHAR(100) DEFAULT 'AIR', " +
                "xmax INT(11), " +
                "xmin INT(11), " +
                "ymax INT(11), " +
                "ymin INT(11), " +
                "zmax INT(11), " +
                "zmin INT(11), " +
                "CONSTRAINT pk_portalname PRIMARY KEY (portalname)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool connection) {
        connection.addPreparedStatement("getPortals", "SELECT * FROM " + ConfigManager.main.Table_Portals);
        connection.addPreparedStatement("deletePortal", "DELETE FROM "+ ConfigManager.main.Table_Portals +" WHERE portalname = ?");
        connection.addPreparedStatement("insertPortal", "INSERT INTO "+ ConfigManager.main.Table_Portals +" (portalname,server,type,destination,world,filltype,xmax,xmin,ymax,ymin,zmax,zmin) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
        connection.addPreparedStatement("updatePortal", "UPDATE "+ ConfigManager.main.Table_Portals +" SET server=?, world=?, type =?, filltype = ?, destination = ?, xmax=?, ymax=?, zmax=?, xmin = ?, ymin = ?, zmin = ? WHERE portalname=?");
    }

    @Override
    public void checkUpdate() {

    }
}
