package net.cubespace.geSuit;

import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.profile.Profile;
import net.cubespace.geSuit.tasks.DatabaseUpdateRowUUID;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Shared utilities. */
public class Utilities {
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isIPAddress(String ip) {
        return com.google.common.net.InetAddresses.isInetAddress(ip);
    }

    /** Translates & color codes to section sign for display. */
    public static String colorize(String input) {
        if (input == null) return "";
        return input.replace("{N}", "\n").replace('&', '\u00A7');
    }

    /** Same as colorize; used where the result is passed to Adventure components. */
    public static String colorizeForComponent(String input) {
        return colorize(input);
    }

    public static Map<String, String> getUUID(java.util.List<String> names) {
        try {
            Map<String, UUID> uuids = Profile.getOnlineUUIDs(names);
            Map<String, String> results = new java.util.HashMap<>();
            for (Map.Entry<String, UUID> e : uuids.entrySet()) {
                results.put(e.getKey(), e.getValue().toString().replace("-", ""));
            }
            return results;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public static String getUUID(String name) {
        try {
            UUID id = Profile.getOnlineUUIDs(Collections.singletonList(name)).get(name);
            return id == null ? null : Utilities.getStringFromUUID(id);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void databaseUpdateRowUUID(int id, String playerName) {
        geSuit.getInstance().getProxy().getScheduler().buildTask(geSuit.getInstance(), new DatabaseUpdateRowUUID(id, playerName)).schedule();
    }

    public static String dumpPacket(String channel, String direction, byte[] bytes, boolean consoleOutput) {
        StringBuilder data = new StringBuilder();
        for (byte c : bytes) {
            if (c >= 32 && c <= 126) {
                data.append((char) c);
            } else {
                data.append("\\x").append(Integer.toHexString(c));
            }
        }
        if (consoleOutput) {
            geSuit.getInstance().getLogger().info("DEBUG: [" + channel + "] " + direction + ": " + data);
        }
        return data.toString();
    }

    public static String buildTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(amount);
            builder.append(amount > 1 ? " Days " : " Day ");
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append(amount > 1 ? " Hours " : " Hour ");
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append(amount > 1 ? " Mins " : " Min ");
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append(amount > 1 ? " Secs " : " Sec ");
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }
        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("0 Secs");
        }
        return builder.toString().trim();
    }

    public static String buildShortTimeDiffString(long timeDiff, int precision) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        long amount = timeDiff / TimeUnit.DAYS.toMillis(1);
        if (amount >= 1) {
            builder.append(amount).append("d ");
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount).append("h ");
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount).append("m ");
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }
        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount).append("s ");
            timeDiff -= amount * TimeUnit.SECONDS.toMillis(1);
            ++count;
        }
        if (timeDiff < 1000 && builder.length() == 0) {
            builder.append("0s");
        }
        return builder.toString().trim();
    }

    public static String createTimeStampString(long timeStamp) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(timeStamp);
    }

    /**
     * Log a message and optionally send to a ChatControl channel when ChatControlIntegration is enabled.
     * Uses ChatControlMirror API when the plugin is present.
     */
    public static boolean sendOnChatChannel(String channel, String msg) {
        LoggingManager.log(Utilities.colorizeForComponent(msg));

        if (!Boolean.TRUE.equals(ConfigManager.main.ChatControlIntegration)) {
            return true;
        }
        Object api = geSuit.getInstance().getChatControlMirrorApi();
        if (api == null) {
            return true;
        }
        try {
            Class<?> formatClass = Class.forName("au.com.addstar.ccm.api.MessageFormat");
            Object legacyFormat = formatClass.getMethod("valueOf", String.class).invoke(null, "LEGACY");
            api.getClass()
                    .getMethod("sendChannelMessage", String.class, String.class, formatClass)
                    .invoke(api, channel, msg, legacyFormat);
            return true;
        } catch (Throwable t) {
            geSuit.getInstance().getLogger().warn("Could not send message to ChatControl channel \"{}\": {}", channel, t.getMessage());
            return false;
        }
    }

    public static UUID makeUUID(String uuid) {
        if (uuid.length() < 32) {
            throw new IllegalArgumentException("This is not a UUID");
        }
        if (!uuid.contains("-")) {
            return UUID.fromString(String.format("%s-%s-%s-%s-%s", uuid.substring(0, 8), uuid.substring(8, 12), uuid.substring(12, 16), uuid.substring(16, 20), uuid.substring(20)));
        } else {
            return UUID.fromString(uuid);
        }
    }

    public static String getStringFromUUID(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }
}
