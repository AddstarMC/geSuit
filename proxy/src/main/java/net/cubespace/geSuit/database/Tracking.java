package net.cubespace.geSuit.database;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Track;
import net.cubespace.geSuit.profile.Profile;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Tracking implements IRepository {
    
    public void insertTracking(String player, String uuid, String ip) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection(); PreparedStatement insertPlayer = DatabaseManager.connectionPool.getPreparedStatement("insertTracking", con)) {
            insertPlayer.setString(1, player);
            insertPlayer.setString(2, uuid);
            insertPlayer.setString(3, ip);

            insertPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertHistoricTracking(String player, String uuid, String ip, Date changedDate, Date lastSeen) {

        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement insertPlayer = DatabaseManager.connectionPool.getPreparedStatement("insertHistoricTracking", con)
        ) {
            insertPlayer.setString(1, player);
            insertPlayer.setString(2, uuid);
            insertPlayer.setString(3, ip);
            insertPlayer.setDate(4, changedDate);
            insertPlayer.setDate(5, lastSeen);
            insertPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Track> getPlayerTracking(String search, String type) {
        List<Track> tracking = new ArrayList<>();

        try (Connection con = DatabaseManager.connectionPool.getConnection()) {
        	PreparedStatement trackInfo;

            switch (type) {
                case "ip":
                    // Lookup by IP
                    trackInfo = DatabaseManager.connectionPool.getPreparedStatement("getIPTracking", con);
                    trackInfo.setString(1, search);
                    break;
                case "uuid":
                    // Lookup by IP
                    trackInfo = DatabaseManager.connectionPool.getPreparedStatement("getUUIDTracking", con);
                    trackInfo.setString(1, search);
                    break;
                default:
                    // Lookup by player name
                    trackInfo = DatabaseManager.connectionPool.getPreparedStatement("getPlayerTracking", con);
                    trackInfo.setString(1, search);
                    break;
            }

            ResultSet res = trackInfo.executeQuery();
            while (res.next()) {
                tracking.add(new Track(
                		res.getString("player"),
                		res.getString("uuid"),
                		res.getString("ip"),
                		res.getTimestamp("firstseen"),
                		res.getTimestamp("lastseen"),
                		res.getString("type"),
                		res.getString("banned_playername"),
                		res.getString("banned_uuid"),
                		res.getString("banned_ip")
                ));
            }

            res.close();
            trackInfo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tracking;
    }

    public void insertNameHistory(GSPlayer player) {
        UUID id = Utilities.makeUUID(player.getUuid());
        String ip = player.getIp();
        Map<Timestamp, String> input = Profile.getMojangNameHistory(id);
        TreeMap<Timestamp, String> sorted = new TreeMap<>(input);
        Date firstSeen = new Date(0);
        for (Map.Entry<Timestamp, String> e : sorted.entrySet()) {
            String oldName = e.getValue();
            Timestamp time = e.getKey();
            Date changedAt = new Date(time.getTime());
            insertHistoricTracking(oldName, player.getUuid(), ip, firstSeen, changedAt);
            firstSeen = changedAt;
        }
    }

    public List<Track> getNameHistory(UUID id) {
        List<Track> tracking = new ArrayList<>();
        try (Connection con = DatabaseManager.connectionPool.getConnection()) {
            PreparedStatement statement = DatabaseManager.connectionPool.getPreparedStatement("getNameHistory", con);
            String uuid = Utilities.getStringFromUUID(id);
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            
            ResultSet res = statement.executeQuery();
            while (res.next()) {
                tracking.add(new Track(
                        res.getString("player"),
                        res.getString("uuid"),
                        res.getString("ip"),
                        res.getTimestamp("firstseen"),
                        res.getTimestamp("lastseen"),
                        null,
                        null,
                        null,
                        null
                ));
            }

            res.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tracking;
    }

    public void batchUpdateNameHistories(final List<UUID> uuids) {
        geSuit.getInstance().getProxy().getScheduler().buildTask(
                geSuit.getInstance(), nameHistoryUpdater(uuids)).schedule();
    }
    
    Runnable nameHistoryUpdater(final List<UUID> uuids) {
        return () -> {
            int updated = 0;
            LoggingManager.log("Starting Tracking Table update for " + uuids.size() + " UUIDs...");
            for (UUID uuid : uuids) {
                try {
                    // Fetch the list of names for this UUID from Mojang
                    Map<Timestamp, String> input = Profile.getMojangNameHistory(uuid);
                    if (input == null) {
                        LoggingManager.log("WARNING: Failed to fetch Tracking name history of \"" + uuid + "\" from Mojang");
                        continue;
                    }

                    Date changedAt = new Date(0);
                    TreeMap<Timestamp, String> sorted = new TreeMap<>(input);
                    while (sorted.size() > 0) {
                        Map.Entry<Timestamp, String> e = sorted.pollLastEntry();
                        String oldName = e.getValue();
                        Timestamp time = e.getKey();
                        Date firstSeen = new Date(time.getTime());
                        if (changedAt.before(firstSeen)) changedAt = firstSeen;
                        insertHistoricTracking(oldName, Utilities.getStringFromUUID(uuid), "", firstSeen, changedAt);
                        updated++;
                        changedAt = firstSeen;
                        if (updated % 100 == 0) {
                            LoggingManager.log("Processing Tracking Table... " + updated + " entries updated...");
                        }
                    }
                    Thread.sleep(1200);
                } catch (IllegalStateException | InterruptedException e) {
                    String mess = e.getMessage();
                    System.out.println("Interrupted at UUID: " + uuid + " Cause: " + mess);
                    e.printStackTrace();
                }
            }
            LoggingManager.log("Tracking Table update complete! Updated " + updated + " entries.");
        };
    }

    public Track checkNameChange(UUID id, String playername) {
        Track tracking = null;
        try (Connection con = DatabaseManager.connectionPool.getConnection()) {
            PreparedStatement statement = DatabaseManager.connectionPool.getPreparedStatement("checkNameChange", con);
            String uuid = Utilities.getStringFromUUID(id);
            statement.setString(1, uuid);
            statement.setString(2, playername);
            
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                tracking = new Track(
                        res.getString("player"),
                        res.getString("uuid"),
                        res.getString("ip"),
                        res.getTimestamp("firstseen"),
                        res.getTimestamp("lastseen"),
                        null,
                        null,
                        null,
                        null
                );
            }

            res.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tracking;
    }

    @Override
    public String[] getTable() {
    	return new String[]{ConfigManager.main.Table_Tracking, "player varchar(20) NOT NULL, "
    			  + "`uuid` varchar(32) NOT NULL,"
    			  + "`ip` varchar(15) NOT NULL,"
    			  + "`firstseen` datetime NOT NULL,"
    			  + "`lastseen` datetime NOT NULL,"
    			  + "UNIQUE KEY `player` (`player`,`uuid`,`ip`)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool connection) {
        connection.addPreparedStatement("insertHistoricTracking", "INSERT INTO " + ConfigManager.main.Table_Tracking + " (player,uuid,ip,firstseen,lastseen) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE player=player");
        connection.addPreparedStatement("insertTracking", "INSERT INTO "+ ConfigManager.main.Table_Tracking +" (player,uuid,ip,firstseen,lastseen) VALUES (?, ?, ?, NOW(), NOW()) ON DUPLICATE KEY UPDATE lastseen=NOW()");
        connection.addPreparedStatement("getPlayerTracking", "SELECT t2.ip, t2.player, t2.uuid, t2.firstseen, t2.lastseen, b.type, b.banned_playername, b.banned_uuid, b.banned_ip FROM "+ ConfigManager.main.Table_Tracking +" AS t1 JOIN "+ ConfigManager.main.Table_Tracking +" AS t2 ON (t1.ip != '' AND t2.ip != '' AND t1.ip=t2.ip) LEFT JOIN " + ConfigManager.main.Table_Bans + " AS b ON (t2.ip=b.banned_ip OR t2.player=b.banned_playername OR t2.uuid=b.banned_uuid) AND b.type != 'warn' AND b.active=1 WHERE t1.player=? GROUP BY t2.player,t2.uuid,t2.ip ORDER BY t2.lastseen;");
        connection.addPreparedStatement("getUUIDTracking", "SELECT t2.ip, t2.player, t2.uuid, t2.firstseen, t2.lastseen, b.type, b.banned_playername, b.banned_uuid, b.banned_ip FROM "+ ConfigManager.main.Table_Tracking +" AS t1 JOIN "+ ConfigManager.main.Table_Tracking +" AS t2 ON (t1.ip != '' AND t2.ip != '' AND t1.ip=t2.ip) LEFT JOIN " + ConfigManager.main.Table_Bans + " AS b ON (t2.ip=b.banned_ip OR t2.player=b.banned_playername OR t2.uuid=b.banned_uuid) AND b.type != 'warn' AND b.active=1 WHERE t1.uuid=? GROUP BY t2.player,t2.uuid,t2.ip ORDER BY t2.lastseen;");
        connection.addPreparedStatement("getIPTracking", "SELECT t.ip, t.player, t.uuid, t.firstseen, t.lastseen, b.type, b.banned_playername, b.banned_uuid, b.banned_ip FROM "+ ConfigManager.main.Table_Tracking +" AS t LEFT JOIN "+ ConfigManager.main.Table_Bans +" AS b ON (t.ip=b.banned_ip OR t.player=b.banned_playername OR t.uuid=b.banned_uuid) AND b.type != 'warn' AND b.active=1 WHERE t.ip=? GROUP BY t.player,t.uuid,t.ip ORDER BY t.lastseen;");
        connection.addPreparedStatement("getNameHistory", "SELECT p1.* FROM " + ConfigManager.main.Table_Tracking + " p1 INNER JOIN ( SELECT max(lastseen) LastSeen, player FROM " + ConfigManager.main.Table_Tracking + " WHERE uuid=? GROUP BY player) p2 ON p1.player = p2.player AND p1.lastseen = p2.LastSeen WHERE p1.uuid=? order by p1.lastseen;");
        connection.addPreparedStatement("checkNameChange", "SELECT * FROM " + ConfigManager.main.Table_Tracking + " WHERE uuid=? AND player!=? ORDER BY lastseen DESC;");
    }

	@Override
	public void checkUpdate() {

	}
}
