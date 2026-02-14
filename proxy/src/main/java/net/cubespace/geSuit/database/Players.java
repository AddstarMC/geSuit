package net.cubespace.geSuit.database;

import com.google.common.collect.Maps;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.GSPlayer;

import java.sql.*;
import java.util.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Players implements IRepository {

    public boolean playerExists(String player) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement playerExists = DatabaseManager.connectionPool.getPreparedStatement("playerExists", con)
        ) {
            playerExists.setString(1, player);
            playerExists.setString(2, player);

            return playerExists.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    
    public List<UUID> getUUIDs(String start, String end) {
        List<UUID> results = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getUUIDs = DatabaseManager.connectionPool.getPreparedStatement("getUUIDS", con)
        ) {

            getUUIDs.setString(1, start);
            getUUIDs.setString(2, end);
            ResultSet res = getUUIDs.executeQuery();
            while (res.next()) {
                results.add(Utilities.makeUUID(res.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    public List<UUID> getAllUUIDs() {
        List<UUID> results = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getUUIDs = DatabaseManager.connectionPool.getPreparedStatement("getAllUUIDS", con)
        ) {
            ResultSet res = getUUIDs.executeQuery();
            while (res.next()) {
                results.add(Utilities.makeUUID(res.getString("uuid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getPlayerIP(String player) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getPlayerIP = DatabaseManager.connectionPool.getPreparedStatement("getPlayerIP", con)
        ) {
            getPlayerIP.setString(1, player);
            getPlayerIP.setString(2, player);

            String ip = null;
            ResultSet res = getPlayerIP.executeQuery();
            while (res.next()) {
                ip = res.getString("ipaddress");
            }
            res.close();
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getPlayerTPS(String player) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getPlayerTPS = DatabaseManager.connectionPool.getPreparedStatement("getPlayerTPS", con)
        ) {

            getPlayerTPS.setString(1, player);
            getPlayerTPS.setString(2, player);

            boolean tps = true;
            ResultSet res = getPlayerTPS.executeQuery();
            while (res.next()) {
                tps = res.getBoolean("tps");
            }
            res.close();
            getPlayerTPS.close();
            getPlayerTPS.getConnection().close();
            return tps;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public String[] getAltPlayer(String uuid, String ip, boolean ignoreSelf) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getAltPlayer = DatabaseManager.connectionPool.getPreparedStatement("getAltPlayer", con)
        ) {
            getAltPlayer.setString(1, ip);
            String altname = "";
            String altuuid = uuid;
            
            ResultSet res = getAltPlayer.executeQuery();

            if (ignoreSelf) {
            	// Skip the first result when a new player joins (because it's always themselves)
            	res.next();
            }

            if (res.next()) {
                altname = res.getString("playername");
                altuuid = res.getString("uuid");
            }
            res.close();

            if (!uuid.equals(altuuid)) {
            	return new String[]{altname, altuuid};
            } else {
            	return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void insertPlayer(GSPlayer player, String ip) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement insertPlayer = DatabaseManager.connectionPool.getPreparedStatement("insertPlayer", con)
        ) {
            insertPlayer.setString(1, player.getName());
            insertPlayer.setString(2, player.getUuid());
            insertPlayer.setString(3, ip);

            insertPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertPlayerConvert(String player, String uuid, Timestamp lastonline, String ip, boolean tps) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement insertPlayerConvert = DatabaseManager.connectionPool.getPreparedStatement("insertPlayerConvert", con)
        ) {
            insertPlayerConvert.setString(1, player);
            insertPlayerConvert.setString(2, uuid);
            insertPlayerConvert.setTimestamp(3, lastonline);
            insertPlayerConvert.setTimestamp(4, lastonline);
            insertPlayerConvert.setString(5, ip);
            insertPlayerConvert.setBoolean(6, tps);

            insertPlayerConvert.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePlayer(GSPlayer gsPlayer) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                Connection con1 = DatabaseManager.connectionPool.getConnection();
                PreparedStatement updatePlayer = DatabaseManager.connectionPool.getPreparedStatement("updatePlayerByUUID", con);
                PreparedStatement updatePlayerbyName = DatabaseManager.connectionPool.getPreparedStatement("updatePlayerByName", con1)
        ) {
            updatePlayer.setString(1, gsPlayer.getName());
            updatePlayer.setString(2, gsPlayer.getIp());
            updatePlayer.setBoolean(3, gsPlayer.acceptingTeleports());
            updatePlayer.setBoolean(4, gsPlayer.isNewSpawn());
            updatePlayer.setString(5, gsPlayer.getUuid());

            int result = updatePlayer.executeUpdate();
            updatePlayer.close();
            if (result > 1) {
                geSuit.getInstance().getLogger().warn("PLAYER HAS MULTIPLE UUID ENTRIES WHICH HAVE BEEN UPDATED: " + gsPlayer.getName());
            }
            if (result == 0) {
                geSuit.getInstance().getLogger().warn("PLAYER IS BEING UPDATED BY NAME: " + gsPlayer.getName());

                updatePlayerbyName.setString(1, gsPlayer.getUuid());
                updatePlayerbyName.setString(2, gsPlayer.getIp());
                updatePlayerbyName.setBoolean(3, gsPlayer.acceptingTeleports());
                updatePlayerbyName.setBoolean(4, gsPlayer.isNewSpawn());
                updatePlayerbyName.setString(5, gsPlayer.getName());
                if (updatePlayerbyName.executeUpdate() != 1) {
                    geSuit.getInstance().getLogger().warn("PLAYER COULD NOT BE UPDATED: " + gsPlayer.getName());

                }
                updatePlayer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GSPlayer loadPlayer(String player) {
        GSPlayer player1 = null;
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getPlayer = DatabaseManager.connectionPool.getPreparedStatement("getPlayer", con)
        ) {
            getPlayer.setString(1, player);
            getPlayer.setString(2, player);

            ResultSet res = getPlayer.executeQuery();
            while (res.next()) {
                player1 = new GSPlayer(res.getString("playername"), res.getString("uuid"), res.getBoolean("tps"), res.getBoolean("newspawn"), res.getString("ipaddress"), res.getTimestamp("lastonline"), res.getTimestamp("firstonline"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player1;
    }

    public List<String> matchPlayers(String player) {
        List<String> players = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement getPlayer = DatabaseManager.connectionPool.getPreparedStatement("matchPlayers", con)
        ) {
            getPlayer.setString(1, "%" + player + "%");     // Player Name
            getPlayer.setString(2, player);                 // UUID

            ResultSet res = getPlayer.executeQuery();
            while (res.next()) {
                players.add(res.getString("playername"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
    
    public Map<String, UUID> resolvePlayerNames(Collection<String> names) {
        Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(name);
                
                if (count >= maxBatch) {
                    getOldPlayerName("playername", "resolvePlayerName", builder, resolved);
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                getOldPlayerName("playername", "resolvePlayerName", builder, resolved);
            }
            
            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
    
    /**
     * Resolves a player name using the tracking table instead of the players table. This allows it to resolve old names for players
     * @param names the names
     * @return Map
     */
    public Map<String, UUID> resolvePlayerNamesHistoric(Collection<String> names) {

        Map<String, UUID> resolved = Maps.newHashMapWithExpectedSize(names.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (String name : names) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(name);
                
                if (count >= maxBatch) {
                    getOldPlayerName("player", "resolveOldPlayerName", builder, resolved);
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                getOldPlayerName("player", "resolveOldPlayerName", builder, resolved);
            }
            
            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private void getOldPlayerName(String nameColumn, String sql, StringBuilder search, Map<String, UUID> resolved) {
        try (Connection con = DatabaseManager.connectionPool.getConnection();
             PreparedStatement statement = DatabaseManager.connectionPool.getPreparedStatement(sql, con)) {
            statement.setString(1, search.toString());

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                resolved.put(results.getString(nameColumn), Utilities.makeUUID(results.getString("uuid")));
            }
            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, String> resolveUUIDs(Collection<UUID> ids) {
        Map<UUID, String> resolved = Maps.newHashMapWithExpectedSize(ids.size());
        try {
            int maxBatch = 40;
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (UUID id : ids) {
                ++count;
                if (builder.length() != 0) {
                    builder.append(",");
                }
                
                builder.append(id.toString().replace("-", ""));
                
                if (count >= maxBatch) {
                    getPlayerUUIDs("resolveUUID", builder, resolved);
                    builder.setLength(0);
                    count = 0;
                }
            }
            
            if (count > 0) {
                getPlayerUUIDs("resolveUUID", builder, resolved);
            }

            return resolved;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private void getPlayerUUIDs(String sql, StringBuilder builder, Map<UUID, String> resolved) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement statement = DatabaseManager.connectionPool.getPreparedStatement(sql, con)) {

            statement.setString(1, builder.toString());

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                resolved.put(Utilities.makeUUID(results.getString("uuid")), results.getString("playername"));
            }
            results.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Players, "playername VARCHAR(100), "
                + "uuid VARCHAR(100) NOT NULL,"
                + "firstonline DATETIME NOT NULL, "
                + "lastonline DATETIME NOT NULL, "
                + "ipaddress VARCHAR(100), "
                + "tps TINYINT(1) DEFAULT 1,"
                + "newspawn TINYINT(1) DEFAULT 0,"
                + "CONSTRAINT pk_uuid PRIMARY KEY (uuid)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool connection) {
        connection.addPreparedStatement("getPlayerIP", "SELECT ipaddress FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("playerExists", "SELECT playername FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayerTPS", "SELECT tps FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getPlayer", "SELECT * FROM "+ ConfigManager.main.Table_Players +" WHERE playername = ? OR uuid = ?");
        connection.addPreparedStatement("getAltPlayer", "SELECT playername, uuid FROM "+ ConfigManager.main.Table_Players +" WHERE ipaddress = ? ORDER BY lastonline DESC LIMIT 2");
        // Show the 20 most recent players whose name matches the search string
        connection.addPreparedStatement("matchPlayers", "SELECT playername,uuid FROM (SELECT playername,uuid,lastonline FROM "+ ConfigManager.main.Table_Players +" WHERE playername like ? OR uuid like ? ORDER BY lastonline desc LIMIT 20) AS FilterQ ORDER BY lastonline");
        connection.addPreparedStatement("insertPlayer", "INSERT INTO "+ ConfigManager.main.Table_Players +" (playername,uuid,firstonline,lastonline,ipaddress) VALUES (?, ?, NOW(), NOW(), ?)");
        connection.addPreparedStatement("insertPlayerConvert", "INSERT INTO "+ ConfigManager.main.Table_Players +" (playername,uuid,firstonline,lastonline,ipaddress,tps) VALUES (?, ?, ?, ?, ?, ?)");
        connection.addPreparedStatement("getPlayers", "SELECT * FROM "+ ConfigManager.main.Table_Players);
        connection.addPreparedStatement("setUUID", "UPDATE "+ ConfigManager.main.Table_Players +" SET uuid = ? WHERE playername = ?");
        connection.addPreparedStatement("updatePlayerByUUID", "UPDATE " + ConfigManager.main.Table_Players + " SET playername = ?, lastonline = NOW(), ipaddress = ?, tps = ?, newspawn = ? WHERE uuid = ?");
        connection.addPreparedStatement("updatePlayerByName", "UPDATE " + ConfigManager.main.Table_Players + " SET uuid = ?, lastonline = NOW(), ipaddress = ?, tps = ?, newspawn = ? WHERE playername = ?");
        connection.addPreparedStatement("resolvePlayerName", "SELECT playername,uuid FROM "+ ConfigManager.main.Table_Players +" WHERE FIND_IN_SET(playername, ?)");
        connection.addPreparedStatement("resolveOldPlayerName", "SELECT player,uuid FROM "+ ConfigManager.main.Table_Tracking +" WHERE FIND_IN_SET(player, ?) GROUP BY player");
        connection.addPreparedStatement("resolveUUID", "SELECT playername,uuid FROM "+ ConfigManager.main.Table_Players +" WHERE FIND_IN_SET(uuid, ?)");
        connection.addPreparedStatement("getAllUUIDS", "SELECT uuid FROM " + ConfigManager.main.Table_Players);
        connection.addPreparedStatement("getUUIDS", "SELECT uuid FROM " + ConfigManager.main.Table_Players + " WHERE " +
                "uuid BETWEEN ? and ?");
    }
    
    @Override
    public void checkUpdate() {
        int installedVersion = ConfigManager.main.Version_Database_Players;

        System.out.println("Current Version of the Players Database: " + installedVersion);

        if (installedVersion < 2) {
            // Version 2 adds UUIDs as Field
    
            try (Connection con = DatabaseManager.connectionPool.getConnection()) {
                con.createStatement().execute("ALTER TABLE `" + ConfigManager.main.Table_Players +
                        "` ADD `uuid` VARCHAR(100) NULL AFTER `playername`, ADD UNIQUE (`uuid`) ;");
            } catch (SQLException e) {
                System.out.println("Could not update the Player Database to version 2");
                e.printStackTrace();
                return;
            }
            // Convert all Names to UUIDs
    
            try (
                    Connection con = DatabaseManager.connectionPool.getConnection();
                    PreparedStatement getPlayers = DatabaseManager.connectionPool.getPreparedStatement("getPlayers", con)
            ) {
                ResultSet res = getPlayers.executeQuery();
                while (res.next()) {
                    String playername = res.getString("playername");
                    String uuid = Utilities.getUUID(playername);

                    if (uuid != null) {
                        try (
                                Connection con2 = DatabaseManager.connectionPool.getConnection();
                                PreparedStatement preparedStatement = DatabaseManager.connectionPool.getPreparedStatement("setUUID", con2)
                        ) {
                            preparedStatement.setString(1, uuid);
                            preparedStatement.setString(2, playername);
                            preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            System.out.println("Could not update Player for update to version 2");
                            e.printStackTrace();
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Could not get Players for update to version 2");
                e.printStackTrace();
                return;
            }
        }
        if (installedVersion < 3) {
            // Version 3 adds "firstonline" field
            try {
                System.out.println("Upgrading Player Database to version 3...");
                DatabaseManager.connectionPool.getConnection().createStatement().execute("ALTER TABLE `" + ConfigManager.main.Table_Players + "` ADD `firstonline` DATETIME NOT NULL AFTER `uuid`;");
            } catch (SQLException e) {
                System.out.println("Could not update the Player Database to version 3");
                e.printStackTrace();
                return;
            }

            // Convert any existing "firstonline" values to the current "lastonline" values
            Statement stmt = null;
            try {
                stmt = DatabaseManager.connectionPool.getConnection().createStatement();
            	stmt.executeUpdate("UPDATE `"+ ConfigManager.main.Table_Players +"` SET firstonline=lastonline");
            	stmt.close();
            } catch (SQLException e) {
                System.out.println("Could not upgrade firstonline values of existing players");
                e.printStackTrace();
                return;
            }
        }

        ConfigManager.main.Version_Database_Players = 3;
        try {
            ConfigManager.main.save();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

}
