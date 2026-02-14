package net.cubespace.geSuit.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.configs.SubConfig.Database;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectionPool {
    private final HikariConfig dbConfig = new HikariConfig();
    private HikariDataSource dataSource;
    private boolean configured = false;
    private final Map<String, String> statementCache = new HashMap<>();
    private final ArrayList<IRepository> repositories = new ArrayList<>();

    public void addRepository(IRepository repository) {
        repositories.add(repository);
    }

    public boolean initialiseConnections(Database database) {
        if (configureDataSource(database)) {
            if (!ConfigManager.main.Inited) {
                for (IRepository repository : repositories) {
                    String[] tableInformation = repository.getTable();

                    if (!doesTableExist(tableInformation[0])) {
                        try {
                            standardQuery("CREATE TABLE IF NOT EXISTS `" + tableInformation[0] + "` (" + tableInformation[1] + ");");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            geSuit.getInstance().getLogger().error("Could not create Table " + tableInformation[0].substring(0, 20) + " ...");
                            throw new IllegalStateException(e.getMessage());
                        }
                    }
                }

                ConfigManager.main.Inited = true;
                try {
                    ConfigManager.main.save();
                } catch (InvalidConfigurationException ignored) {

                }
            } else {
                for (IRepository repository : repositories) {
                    repository.checkUpdate();
                }
            }
        }

        return true;
    }

    protected boolean configureDataSource(Database database) {
        if (configured) return true;
        dbConfig.setJdbcUrl("jdbc:mysql://" + database.Host + ":" + database.Port + "/" + database.Database + "?useSSL=" + database.useSSL);
        dbConfig.setUsername(database.Username);
        dbConfig.setPassword(database.Password);
        setDataSourceDefaults(database);
        dbConfig.setPoolName("geSuit-Pool");
        dataSource = new HikariDataSource(dbConfig);
        for (IRepository rep : repositories) {
            rep.registerPreparedStatements(this);
        }
        configured = true;
        return true;
    }

    private void setDataSourceDefaults(Database database) {
        dbConfig.addDataSourceProperty("cachePrepStmts", true);
        dbConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        dbConfig.addDataSourceProperty("useServerPrepStmts", true);
        dbConfig.addDataSourceProperty("useLocalSessionState", true);
        dbConfig.addDataSourceProperty("rewriteBatchedStatements", true);
        dbConfig.addDataSourceProperty("elideSetAutoCommits", true);
        dbConfig.addDataSourceProperty("maintainTimeStats", false);
        dbConfig.addDataSourceProperty("maximumPoolSize", 20);
        dbConfig.setInitializationFailTimeout(10000);
        dbConfig.setValidationTimeout(15000);
        //this allows the config to override the hardset defaults...but ensure those defaults are
        // set if non else are.
        for (Map.Entry<String, Object> property : database.properties.entrySet()) {
            dbConfig.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    private String getSQL(String name) {
        return statementCache.getOrDefault(name, null);
    }
    
    public void addSQL(String name, String sql) {
        statementCache.put(name, sql);
    }

    private PreparedStatement prepareSQL(String name) throws SQLException {
        Connection connection = dataSource.getConnection();
        return prepareSQL(name, connection, 0);
    }

    private PreparedStatement prepareSQL(String name, Connection connection, int mode) throws SQLException {
        String sql = getSQL(name);
        if (sql == null) {
            geSuit.getInstance().getLogger().warn("No SQL found for entry: " + name);
            return null;
        }
        if (mode == 0) return connection.prepareStatement(sql);
        else return connection.prepareStatement(sql, mode);
    }

    @Nullable
    public PreparedStatement getPreparedStatement(String name, Connection conn, int mode) throws SQLException {
        return prepareSQL(name, conn, mode);
    }


    @Nullable
    public PreparedStatement getPreparedStatement(String name, Connection connection) throws SQLException {
        return prepareSQL(name, connection, 0);
    }
    
    /**
     * @param name the key to a statement
     *
     * @return A PreparedStatement ready for execution
     *
     * @deprecated using this method does not allow correct resource mamangement and can lead to
     *         connections being held out of the pool ...until timeout.  if you use it you must
     *         close the connection explicitly.
     * @throws SQLException if it cannot get a connection or prepare the statement.
     */
    @Deprecated
    public PreparedStatement getPreparedStatement(String name) throws SQLException {
        return prepareSQL(name);
    }
    

    public void addPreparedStatement(String name, String sql, int mode) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql, mode);
            connection.close();
            statement.close();
            statementCache.put(name, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPreparedStatement(String name, String sql) {
        Connection connection = getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.close();
            connection.close();
            statementCache.put(name, sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Adds the given column to the given table if the column does not yet exist
     * The new column will be VARCHAR(length) NULL
     */
    public void AddStringColumnIfMissing(String table, String column, int length) {

        Boolean columnExists = doesTableHaveColumn(table, column);
        if (!columnExists) {
            addStringColumnToTable(table, column, length);
        }
    }

    private void addStringColumnToTable(String table, String column, int length) {
    
        try {
            geSuit.getInstance().getLogger().info("Adding column " + column + " to table " + table);

            String query = "ALTER TABLE `" + table + "` ADD `" + column + "` varchar(" + length + ") NULL";
            Connection con = dataSource.getConnection();
            Statement statement = con.createStatement();
            statement.executeUpdate(query);
            statement.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
            geSuit.getInstance().getLogger().error("Could not add column " + column + " to table " + table);
            throw new IllegalStateException();
        }
    }

    private void standardQuery(String query) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        statement.close();
        connection.close();
    }

    private boolean doesTableExist(String table) {
        return checkTable(table);
    }

    private Boolean doesTableHaveColumn(String table, String column) {

        try {

            String query =
                    "SELECT COUNT(*) as RowCount FROM information_schema.columns " +
                            "WHERE TABLE_SCHEMA = database() AND " +
                            "TABLE_NAME = '" + table + "' AND " +
                            "COLUMN_NAME = '" + column + "';";
            Connection con = dataSource.getConnection();
            Statement statement =con.createStatement();
            ResultSet res = statement.executeQuery(query);
            while (res.next()) {
                int colCount = res.getInt("RowCount");
                if (colCount > 0)
                    return true;
            }
            statement.close();
            con.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            geSuit.getInstance().getLogger().error("Could not validate column " + column + " on table " + table);
            throw new IllegalStateException();
        }
    }

    private boolean checkTable(String table) {
        DatabaseMetaData dbm;
        try {
            dbm = dataSource.getConnection().getMetaData();
        } catch (SQLException e2) {
            e2.printStackTrace();
            return false;
        }

        ResultSet tables;
        try {
            tables = dbm.getTables(null, null, table, null);
        } catch (SQLException e1) {
            e1.printStackTrace();
            return false;
        }

        boolean check;
        try {
            check = tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return check;
    }
}
