package net.cubespace.geSuit.tasks;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUpdateRowUUID implements Runnable {

    private final int rowID;
    private final String playerName;

    public DatabaseUpdateRowUUID(int id, String pname) {
        rowID = id;
        playerName = pname;
    }

    @Override
    public void run() {
        if (rowID == -1) {
            geSuit.getInstance().getLogger().warn("Incorrect row " + rowID + " for player " + playerName);
            return;
        }
        String uuid = null;
        var opt = geSuit.getInstance().getProxy().getPlayer(playerName);
        if (opt.isPresent()) {
            uuid = opt.get().getUniqueId().toString().replaceAll("-", "");
        }
        if (uuid == null || uuid.isEmpty()) {
            geSuit.getInstance().getLogger().warn("Could not fetch UUID for player " + playerName);
        } else {
            try (Connection con = DatabaseManager.connectionPool.getConnection()) {
                PreparedStatement updateUUID = DatabaseManager.connectionPool.getPreparedStatement("updateRowUUID", con);
                updateUUID.setString(1, uuid);
                updateUUID.setInt(2, rowID);
                updateUUID.executeUpdate();
            } catch (SQLException ex) {
                geSuit.getInstance().getLogger().warn("Error while updating db for player " + playerName + " with UUID " + uuid);
                Logger.getLogger(DatabaseUpdateRowUUID.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
