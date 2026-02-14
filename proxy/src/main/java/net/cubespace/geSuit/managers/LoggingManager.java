package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;

public class LoggingManager {

    public static void log(String message) {
        geSuit.getInstance().getLogger().info(Utilities.colorizeForComponent(message));
    }
}
