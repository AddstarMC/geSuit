package net.cubespace.geSuit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;

public class ConnectionPool {
    private String connectionString;
    private String username;
    private String password;
    
    private long maxIdleTime;
    
    private List<ConnectionHandler> connections;

    public ConnectionPool(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        
        connections = Collections.synchronizedList(Lists.<ConnectionHandler>newArrayList());
        maxIdleTime = TimeUnit.SECONDS.toMillis(30);
    }
    
    public void setMaxIdleTime(long maxTime) {
        maxIdleTime = maxTime;
    }
    
    public long getMaxIdleTime() {
        return maxIdleTime;
    }
    
    public void removeExpired() {
        synchronized(connections) {
            Iterator<ConnectionHandler> it = connections.iterator();
            while(it.hasNext()) {
                ConnectionHandler handler = it.next();
                
                if (!handler.isInUse()) {
                    if (System.currentTimeMillis() - handler.getCloseTime() > maxIdleTime) {
                        // Timeout
                        handler.closeConnection();
                        it.remove();
                    }
                }
            }
        }
    }
    
    /**
     * @return Returns a free connection from the pool of connections. Creates a new connection if there are none available
     */
    public ConnectionHandler getConnection() throws SQLException {
        synchronized(connections) {
            for (int i = 0; i < connections.size(); ++i) {
                ConnectionHandler con = connections.get(i);
                
                if (con.lease()) {
                    // Check connection
                    boolean healthy = true;
                    
                    try {
                        if (con.getConnection().isClosed()) {
                            healthy = false;
                        }
                    } catch (SQLException e) {
                        healthy = false;
                    }
                    
                    // Get rid of the connection
                    if (!healthy) {
                        con.closeConnection();
                        connections.remove(i--);
                    // Its ok
                    } else {
                        return con;
                    }
                }
            }
        }
        
        // Create a new connection
        return createConnection();
    }
    
    private ConnectionHandler createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(connectionString, username, password);

        ConnectionHandler handler = new ConnectionHandler(connection);
                
        connections.add(handler);
        return handler;
    }

    public void closeConnections() {
        synchronized(connections) {
            for (ConnectionHandler c : connections) {
                c.closeConnection();
            }
            
            connections.clear();
        }
    }
}