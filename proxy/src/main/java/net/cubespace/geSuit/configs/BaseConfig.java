package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.Yamler.Config.YamlConfig;
import net.cubespace.geSuit.geSuit;

import java.io.File;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 30/05/2020.
 */
public abstract class BaseConfig extends YamlConfig {

    public BaseConfig(String name) {
        this(new File(geSuit.getInstance().getDataFolder(), name + ".yml"));
    }

    protected BaseConfig(File file) {
        CONFIG_FILE = file;
        CONFIG_MODE = ConfigMode.PATH_BY_UNDERSCORE;
    }
}
