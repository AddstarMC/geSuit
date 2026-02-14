package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.YamlConfig;
import net.cubespace.geSuit.configs.SubConfig.AnnouncementEntry;
import net.cubespace.geSuit.geSuit;

import java.io.File;
import java.util.HashMap;

public class Announcements extends YamlConfig {
    public Announcements() {
        CONFIG_FILE = new File(geSuit.getInstance().getDataFolder(), "announcements.yml");
    }

    @SuppressWarnings("CanBeFinal")
    public Boolean Enabled = true;
    @SuppressWarnings("CanBeFinal")
    public HashMap<String, AnnouncementEntry> Announcements = new HashMap<>();
}


