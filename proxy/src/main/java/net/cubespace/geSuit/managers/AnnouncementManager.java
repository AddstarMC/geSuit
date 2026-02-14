package net.cubespace.geSuit.managers;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.configs.SubConfig.AnnouncementEntry;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.tasks.GlobalAnnouncements;
import net.cubespace.geSuit.tasks.ServerAnnouncements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AnnouncementManager {
    public static final ArrayList<com.velocitypowered.api.scheduler.ScheduledTask> announcementTasks = new ArrayList<>();

    public static void loadAnnouncements() {
        setDefaults();
        var proxy = geSuit.getInstance().getProxy();
        if (ConfigManager.announcements.Enabled) {
            List<String> global = ConfigManager.announcements.Announcements.get("global").Messages;
            if (!global.isEmpty()) {
                int interval = ConfigManager.announcements.Announcements.get("global").Interval;
                if (interval > 0) {
                    GlobalAnnouncements g = new GlobalAnnouncements();
                    for (String messages : global) {
                        g.addAnnouncement(messages);
                    }
                    var t = proxy.getScheduler().buildTask(geSuit.getInstance(), g).delay(interval, TimeUnit.SECONDS).repeat(interval, TimeUnit.SECONDS).schedule();
                    announcementTasks.add(t);
                }
            }
            for (var rs : proxy.getAllServers()) {
                String server = rs.getServerInfo().getName();
                if (!ConfigManager.announcements.Announcements.containsKey(server)) continue;
                List<String> servermes = ConfigManager.announcements.Announcements.get(server).Messages;
                if (servermes.isEmpty()) continue;
                int interval = ConfigManager.announcements.Announcements.get(server).Interval;
                if (interval > 0) {
                    ServerAnnouncements s = new ServerAnnouncements(rs);
                    for (String messages : servermes) {
                        s.addAnnouncement(messages);
                    }
                    var t = proxy.getScheduler().buildTask(geSuit.getInstance(), s).delay(interval, TimeUnit.SECONDS).repeat(interval, TimeUnit.SECONDS).schedule();
                    announcementTasks.add(t);
                }
            }
        }
    }

    private static void setDefaults() {
        var proxy = geSuit.getInstance().getProxy();
        Map<String, AnnouncementEntry> check = ConfigManager.announcements.Announcements;
        if (!check.containsKey("global")) {
            AnnouncementEntry announcementEntry = new AnnouncementEntry();
            announcementEntry.Interval = 300;
            announcementEntry.Messages.add("&4Welcome to the server!");
            announcementEntry.Messages.add("&aDon't forget to check out our website");
            check.put("global", announcementEntry);
        }
        for (var rs : proxy.getAllServers()) {
            String server = rs.getServerInfo().getName();
            if (!check.containsKey(server)) {
                AnnouncementEntry announcementEntry = new AnnouncementEntry();
                announcementEntry.Interval = 150;
                announcementEntry.Messages.add("&4Welcome to the " + server + " server!");
                announcementEntry.Messages.add("&aDon't forget to check out our website");
                check.put(server, announcementEntry);
            }
        }
        try {
            ConfigManager.announcements.save();
        } catch (InvalidConfigurationException ignored) {
        }
    }

    public static void reloadAnnouncements() {
        for (var task : announcementTasks) {
            task.cancel();
        }
        announcementTasks.clear();
        loadAnnouncements();
    }
}
