package net.cubespace.geSuit.database;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.objects.Ban;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Bans implements IRepository {

    private Map<String, PreparedStatement> statements = new HashMap<>();

    public boolean isPlayerBanned(String player) {
        return isPlayerBanned(player, null, null);
    }

    public boolean isPlayerBanned(String player, String uuid, String ip) {
    
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement isPlayerBanned = DatabaseManager.connectionPool.getPreparedStatement(
                        "isPlayerBanned", con)) {
            Objects.requireNonNull(isPlayerBanned).setString(1, player);
            isPlayerBanned.setString(2, uuid);
            isPlayerBanned.setString(3, ip);

            return isPlayerBanned.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int banPlayer(String banned_playername, String banned_uuid, String banned_ip, String bannedBy, String reason, String type) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banPlayer =
                        DatabaseManager.connectionPool.getPreparedStatement("banPlayer", con, Statement.RETURN_GENERATED_KEYS)
        ) {
            Objects.requireNonNull(banPlayer).setString(1, banned_playername);
            banPlayer.setString(2, banned_uuid);
            banPlayer.setString(3, banned_ip);
            banPlayer.setString(4, bannedBy);
            banPlayer.setString(5, reason);
            banPlayer.setString(6, type);

            banPlayer.executeUpdate();
            ResultSet rs = banPlayer.getGeneratedKeys();
            if ( rs != null && rs.next() ) {
                return rs.getInt( 1 );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int warnPlayer(String banned_playername, String banned_uuid, String bannedBy, String reason) {
    
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banPlayer =
                        DatabaseManager.connectionPool.getPreparedStatement("warnPlayer", con, Statement.RETURN_GENERATED_KEYS)
        ) {
            Objects.requireNonNull(banPlayer).setString(1, banned_playername);
            banPlayer.setString(2, banned_uuid);
            banPlayer.setString(3, bannedBy);
            banPlayer.setString(4, reason);

            banPlayer.executeUpdate();
            ResultSet rs = banPlayer.getGeneratedKeys();
            if ( rs != null && rs.next() ) {
                return rs.getInt( 1 );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int kickPlayer(String banned_playername, String banned_uuid, String bannedBy, String reason) {
    
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banPlayer =
                        DatabaseManager.connectionPool.getPreparedStatement("kickPlayer", con, Statement.RETURN_GENERATED_KEYS)
        ) {
            Objects.requireNonNull(banPlayer).setString(1, banned_playername);
            banPlayer.setString(2, banned_uuid);
            banPlayer.setString(3, bannedBy);
            banPlayer.setString(4, reason);

            banPlayer.executeUpdate();
            ResultSet rs = banPlayer.getGeneratedKeys();
            if (rs != null && rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void tempBanPlayer(String banned_playername, String banned_uuid, String banned_by, String reason, String till) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement tempBanPlayer =
                        DatabaseManager.connectionPool.getPreparedStatement("tempBanPlayer", con)
        ) {
            Objects.requireNonNull(tempBanPlayer).setString(1, banned_playername);
            tempBanPlayer.setString(2, banned_uuid);
            tempBanPlayer.setString(3, banned_by);
            tempBanPlayer.setString(4, reason);
            tempBanPlayer.setString(5, till);

            tempBanPlayer.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Ban> getBanHistory(String lookup) {
        List<Ban> bans = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banInfo =
                        DatabaseManager.connectionPool.getPreparedStatement("banHistory", con)
        ) {
            Objects.requireNonNull(banInfo).setString(1, lookup);
            banInfo.setString(2, lookup);
            banInfo.setString(3, lookup);

            ResultSet res = banInfo.executeQuery();
            while (res.next()) {
                bans.add(new Ban(res.getInt("id"), res.getString("banned_playername"), res.getString("banned_uuid"), res.getString("banned_ip"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getInt("active"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until")));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bans;
    }

    public List<Ban> getWarnHistory(String player, String uuid) {
        List<Ban> bans = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banInfo =
                        DatabaseManager.connectionPool.getPreparedStatement("warnHistory", con)
        ) {
            Objects.requireNonNull(banInfo).setString(1, player);
            banInfo.setString(2, uuid);

            ResultSet res = banInfo.executeQuery();
            while (res.next()) {
                bans.add(new Ban(res.getInt("id"), res.getString("banned_playername"), res.getString("banned_uuid"), res.getString("banned_ip"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getInt("active"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until")));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bans;
    }

    public List<Ban> getKickHistory(String player, String uuid) {
        List<Ban> bans = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banInfo =
                        DatabaseManager.connectionPool.getPreparedStatement("kickHistory", con)
        ) {
            Objects.requireNonNull(banInfo).setString(1, player);
            banInfo.setString(2, uuid);
            ResultSet res = banInfo.executeQuery();
            while (res.next()) {
                bans.add(new Ban(res.getInt("id"), res.getString("banned_playername"), res.getString("banned_uuid"), res.getString("banned_ip"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getInt("active"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until")));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bans;
    }

    public List<Ban> getKickWarnHistory(String player, String uuid) {
        List<Ban> bans = new ArrayList<>();
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banInfo =
                        DatabaseManager.connectionPool.getPreparedStatement("kickwarnHistory", con)
        ) {
            Objects.requireNonNull(banInfo).setString(1, player);
            banInfo.setString(2, uuid);

            ResultSet res = banInfo.executeQuery();
            while (res.next()) {
                bans.add(new Ban(res.getInt("id"), res.getString("banned_playername"), res.getString("banned_uuid"), res.getString("banned_ip"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getInt("active"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until")));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bans;
    }

    public Ban getBanInfo(String player) {
        return getBanInfo(player, player, player);
    }

    public Ban getBanInfo(String player, String uuid, String ip) {
        Ban b = null;
    
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement banInfo =
                        DatabaseManager.connectionPool.getPreparedStatement("banInfo", con)
        ) {
            Objects.requireNonNull(banInfo).setString(1, player);
            banInfo.setString(2, uuid);
            banInfo.setString(3, ip);

            ResultSet res = banInfo.executeQuery();
            if (res.next()) {
                b = new Ban(res.getInt("id"), res.getString("banned_playername"), res.getString("banned_uuid"), res.getString("banned_ip"), res.getString("banned_by"), res.getString("reason"), res.getString("type"), res.getInt("active"), res.getTimestamp("banned_on"), res.getTimestamp("banned_until"));
            }

            res.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b;
    }

    public void unbanPlayer(int id) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement statement =
                        DatabaseManager.connectionPool.getPreparedStatement("unbanPlayer", con)
        ) {
            Objects.requireNonNull(statement).setInt(1, id);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertBanConvert(String bannedBy, String player, String uuid, String ip, String reason, String type, int active, Date bannedOn, Date bannedUntil) {
        try (
                Connection con = DatabaseManager.connectionPool.getConnection();
                PreparedStatement statement =
                        DatabaseManager.connectionPool.getPreparedStatement("insertBanConvert", con)
        ) {
            Objects.requireNonNull(statement).setString(1, player); //playerName
            statement.setString(2, uuid); //UUID
            statement.setString(3, ip); //IP
            statement.setString(4, bannedBy);
            statement.setString(5, reason);
            statement.setString(6, type);
            statement.setInt(7, active);
            statement.setDate(8, new java.sql.Date(bannedOn.getTime()));
            statement.setDate(9, new java.sql.Date(bannedUntil.getTime()));
        
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getTable() {
        return new String[]{ConfigManager.main.Table_Bans, "id INT(11) NOT NULL AUTO_INCREMENT,"
                + "banned_playername VARCHAR(100), "
                + "banned_uuid VARCHAR(100), "
                + "banned_ip VARCHAR(15), "
                + "banned_by VARCHAR(100), "
                + "reason VARCHAR(255), "
                + "type VARCHAR(100), "
                + "active TINYINT(1), "
                + "banned_on DATETIME NOT NULL,"
                + "banned_until DATETIME, "
                + "CONSTRAINT pk_banid PRIMARY KEY (id)"};
    }

    @Override
    public void registerPreparedStatements(ConnectionPool pool) {

        pool.addPreparedStatement("isPlayerBanned", "SELECT id FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? OR banned_ip = ?) AND type in ('ban', 'ipban', 'tempban') AND active = 1");
        pool.addPreparedStatement("banPlayer", "INSERT INTO " + ConfigManager.main.Table_Bans + " (banned_playername,banned_uuid,banned_ip,banned_by,reason,type,active,banned_on) VALUES (?,?,?,?,?,?,1,NOW());", PreparedStatement.RETURN_GENERATED_KEYS);
        pool.addPreparedStatement("warnPlayer", "INSERT INTO " + ConfigManager.main.Table_Bans + " (banned_playername,banned_uuid,banned_by,reason,type,active,banned_on) VALUES (?,?,?,?,'warn',0,NOW());", PreparedStatement.RETURN_GENERATED_KEYS);
        pool.addPreparedStatement("kickPlayer", "INSERT INTO " + ConfigManager.main.Table_Bans + " (banned_playername,banned_uuid,banned_by,reason,type,active,banned_on) VALUES (?,?,?,?,'kick',0,NOW());", PreparedStatement.RETURN_GENERATED_KEYS);
        pool.addPreparedStatement("unbanPlayer", "UPDATE " + ConfigManager.main.Table_Bans + " SET active = 0 WHERE id = ?");
        pool.addPreparedStatement("banInfo", "SELECT * FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? OR banned_ip = ?) AND type in ('ban', 'ipban', 'tempban') AND active = 1 ORDER BY type");
        pool.addPreparedStatement("banHistory", "SELECT * FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? OR banned_ip = ?) AND type in ('ban', 'ipban', 'tempban') ORDER BY id ASC");
        pool.addPreparedStatement("warnHistory", "SELECT * FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? ) AND type = 'warn' ORDER BY id ASC");
        pool.addPreparedStatement("kickHistory", "SELECT * FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? ) AND type = 'kick' ORDER BY id ASC");
        pool.addPreparedStatement("kickwarnHistory", "SELECT * FROM " + ConfigManager.main.Table_Bans + " WHERE (banned_playername = ? OR banned_uuid = ? ) AND type in ('kick','warn') ORDER BY id ASC");

        pool.addPreparedStatement("tempBanPlayer", "INSERT INTO " + ConfigManager.main.Table_Bans + " (banned_playername,banned_uuid,banned_by,reason,type,active,banned_on,banned_until) VALUES(?,?,?,?,'tempban',1,NOW(),?)", PreparedStatement.RETURN_GENERATED_KEYS);
        pool.addPreparedStatement("insertBanConvert", "INSERT INTO " + ConfigManager.main.Table_Bans + " (banned_playername,banned_uuid,banned_ip,banned_by,reason,type,active,banned_on,banned_until) VALUES(?,?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        pool.addPreparedStatement("getBans", "SELECT * FROM " + ConfigManager.main.Table_Bans);
        pool.addPreparedStatement("updateRowUUID", "UPDATE " + ConfigManager.main.Table_Bans + " SET banned_uuid = ? WHERE id = ?");
        pool.addPreparedStatement("updateToUUID", "UPDATE " + ConfigManager.main.Table_Bans + " SET banned_uuid = ? WHERE id = ?");
        pool.addPreparedStatement("updateToVersion3-part1", "ALTER TABLE `" + ConfigManager.main.Table_Bans + "` CHANGE `display` `banned_playername` VARCHAR( 100 );  ");
        pool.addPreparedStatement("updateToVersion3-part2", "ALTER TABLE `" + ConfigManager.main.Table_Bans + "` CHANGE `banned_entity` `banned_uuid` VARCHAR( 100 );  ");
        pool.addPreparedStatement("updateToVersion3-part3", "ALTER TABLE `" + ConfigManager.main.Table_Bans + "` ADD `banned_ip` VARCHAR( 15 ) NULL AFTER `banned_uuid`  ");
    }

    @Override
    public void checkUpdate() {
        //What current Version of the Database is this ?
        int installedVersion = ConfigManager.main.Version_Database_Ban;

        System.out.println("Current Version of the Ban Database: " + installedVersion);

        if (installedVersion < 2) {
            // Version 2 adds UUIDs as Field
            // Convert all Names to UUIDs
            try (
                    Connection con = DatabaseManager.connectionPool.getConnection();
                    PreparedStatement statement =
                            DatabaseManager.connectionPool.getPreparedStatement("getBans", con)
            ) {
                ResultSet res = Objects.requireNonNull(statement).executeQuery();
                while (res.next()) {
                    String bannedEntity = res.getString("banned_uuid");

                    if (!Utilities.isIPAddress(bannedEntity)) {
                        String uuid = Utilities.getUUID(bannedEntity);

                        if (uuid != null) {
                            try (
                                    Connection con2 =
                                            DatabaseManager.connectionPool.getConnection();
                                    PreparedStatement statement2 =
                                            DatabaseManager.connectionPool.getPreparedStatement(
                                                    "updateToUUID", con2)
                            ) {
                                Objects.requireNonNull(statement2).setString(1, uuid);
                                statement2.setInt(2, res.getInt("id"));
                                statement2.executeUpdate();
                            } catch (SQLException e) {
                                System.out.println("Could not update Ban for update to version 2");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Could not get Bans for update to version 2");
                e.printStackTrace();
                return;
            }
        }

        if (installedVersion < 3) { //dimensionZ aggressive+freedom-of-ban update
            boolean updateCompleted = false;
            try (
                    Connection con = DatabaseManager.connectionPool.getConnection();
                    PreparedStatement statement =
                            DatabaseManager.connectionPool.getPreparedStatement("updateToVersion3-part1", con);
                    PreparedStatement statement2 = DatabaseManager.connectionPool.getPreparedStatement("updateToVersion3-part2", con);
                    PreparedStatement statement3 = DatabaseManager.connectionPool.getPreparedStatement("updateToVersion3-part3", con)

            ) {
                Objects.requireNonNull(statement).executeUpdate();
                Objects.requireNonNull(statement2).executeUpdate();
                Objects.requireNonNull(statement3).executeUpdate();
                System.out.println("Updated Bans to version 3!");
                updateCompleted = true;
            } catch (SQLException ex) {
                System.out.println("Could not get Bans for update to version 3");
                Logger.getLogger(Bans.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!updateCompleted) {
                return;
            }
            ConfigManager.main.Version_Database_Ban = 3;
            try {
                ConfigManager.main.save();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

    }
}
