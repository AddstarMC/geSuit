package net.cubespace.geSuit.configs;

import java.util.ArrayList;

public class TeleportConfig extends BaseConfig {
    public TeleportConfig() {
        super("teleport");
    }

    public Integer TeleportRequestExpireTime = 10;

    public ArrayList<String> TPAWhitelist = new ArrayList<>();
}
