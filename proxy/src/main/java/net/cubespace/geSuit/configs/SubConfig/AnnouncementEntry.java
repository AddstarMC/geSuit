package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.YamlConfig;

import java.util.ArrayList;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class AnnouncementEntry extends YamlConfig {
    public Integer Interval = 150;
    @SuppressWarnings("CanBeFinal")
    public ArrayList<String> Messages = new ArrayList<>();
}
