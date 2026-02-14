package net.cubespace.geSuit.managers;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.configs.*;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class ConfigManager {
    public static final Announcements announcements = new Announcements();
    public static final BansConfig bans = new BansConfig();
    public static final LockDownConfig lockdown = new LockDownConfig();
    public static final MainConfig main = new MainConfig();
    public static final SpawnConfig spawn = new SpawnConfig();
    public static final TeleportConfig teleport = new TeleportConfig();
    public static final Messages messages = new Messages();
    public static final MOTDFile motd = new MOTDFile("motd.txt");
    public static final MOTDFile motdNew = new MOTDFile("motd-new.txt");

    static {
        try {
            messages.init();
            announcements.init();
            bans.init();
            lockdown.init();
            main.init();
            spawn.init();
            teleport.init();
            motd.init();
            motdNew.init();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
