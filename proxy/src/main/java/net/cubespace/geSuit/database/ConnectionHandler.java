package net.cubespace.geSuit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class ConnectionHandler {
    private final Connection connection;
    private boolean used;
    private long lastUsed;
    private final LinkedHashMap<String, PreparedStatement> preparedStatements = new LinkedHashMap<>();

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        this.lastUsed = System.currentTimeMillis();
        this.used = true;
        return connection;
    }

    public boolean isOldConnection() {
        return (System.currentTimeMillis() - lastUsed) > 30000;
    }

    public void addPreparedStatement(String name, String query, int mode) {
        try {
            preparedStatements.put(name, connection.prepareStatement(query, mode));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPreparedStatement(String name, String query) {
        try {
            preparedStatements.put(name, connection.prepareStatement(query));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement getPreparedStatement(String name) {
        this.used = true;
        return preparedStatements.get(name);
    }

    public void release() {
        this.used = false;
    }

    public boolean isUsed() {
        return used;
    }

    public void closeConnection() {
        this.used = true;
        if (connection != null) {
            try {
                for (PreparedStatement ps : preparedStatements.values()) {
                    try {
                        ps.close();
                    } catch (SQLException ignore) {
                    }
                }
                preparedStatements.clear();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
