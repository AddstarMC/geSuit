package net.cubespace.geSuit;

import au.com.addstar.bpandora.MasterPlugin;
import com.google.common.net.InetAddresses;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.profile.Profile;
import net.cubespace.geSuit.tasks.DatabaseUpdateRowUUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import au.com.addstar.bpandora.modules.ChatControlMirror;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utilities {
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isIPAddress(String ip){
        return InetAddresses.isInetAddress(ip);
    }

    public static String colorize(String input) {
    	input = input.replace("{N}", "\n");
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static Map<String, String> getUUID(List<String> names) {
        try {
            Map<String, UUID> uuids = Profile.getOnlineUUIDs(names);
            Map<String, String> results = new HashMap<>();
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
    
    public static void databaseUpdateRowUUID(int id, String playerName)
    {
        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getInstance(), new DatabaseUpdateRowUUID(id, playerName));
    }

    public static String dumpPacket(String channel, String direction, byte[] bytes, boolean consoleOutput) {
        StringBuilder data = new StringBuilder();
        //ByteArrayInputStream ds = new ByteArrayInputStream(bytes);
		//DataInputStream di = new DataInputStream(ds);
		// Read upto 20 parameters from the stream and load them into the string list
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
            if (amount > 1) {
                builder.append(" Days ");
            } else {
                builder.append(" Day ");
            }
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            if (amount > 1) {
                builder.append(" Hours ");
            } else {
                builder.append(" Hour ");
            }
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            if (amount > 1) {
                builder.append(" Mins ");
            } else {
                builder.append(" Min ");
            }
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            if (amount > 1) {
                builder.append(" Secs ");
            } else {
                builder.append(" Sec ");
            }
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
            builder.append(amount);
            builder.append("d ");
            timeDiff -= amount * TimeUnit.DAYS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.HOURS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append("h ");
            timeDiff -= amount * TimeUnit.HOURS.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.MINUTES.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append("m ");
            timeDiff -= amount * TimeUnit.MINUTES.toMillis(1);
            ++count;
        }

        amount = timeDiff / TimeUnit.SECONDS.toMillis(1);
        if (count < precision && amount >= 1) {
            builder.append(amount);
            builder.append("s ");
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

    public static boolean sendOnChatChannel(String channel, String msg) {
        LoggingManager.log(ChatColor.translateAlternateColorCodes('&', msg));

        // ChatControl integration
        if (ConfigManager.main.ChatControlIntegration)
            ChatControlMirror.doChatControlMirror("String", channel, msg);

        return true;
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
