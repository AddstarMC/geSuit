package net.cubespace.geSuit.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.Subscribe;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.events.BanPlayerEvent;
import net.cubespace.geSuit.events.UnbanPlayerEvent;
import net.cubespace.geSuit.events.WarnPlayerEvent;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * Publishes ban, warn, and unban events to Redis PubSub via ValioBungee (RedisBungee) when present.
 * Uses reflection so ValioBungee is a soft dependency and geSuit runs without it.
 * Channels: gesuit:ban, gesuit:warn, gesuit:unban. Payload: JSON with actionBy, targetUsername, targetUuid (+ optional fields).
 */
public class RedisEventPublisher {

    private static final String CHANNEL_BAN = "gesuit:ban";
    private static final String CHANNEL_WARN = "gesuit:warn";
    private static final String CHANNEL_UNBAN = "gesuit:unban";

    private static final String REDIS_BUNGEE_API_CLASS = "com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI";

    private final Gson gson = new Gson();
    private Object redisApi;
    private Method sendChannelMessage;
    private boolean apiResolved;
    private boolean apiUnavailableLogged;

    public RedisEventPublisher() {
        // Lazy-resolve API via reflection when first event is published
    }

    private static String normalizeConsole(String by) {
        if (by == null) return "CONSOLE";
        if ("Console".equalsIgnoreCase(by.trim())) return "CONSOLE";
        return by;
    }

    private static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : "";
    }

    private void resolveApi() {
        if (apiResolved) return;
        apiResolved = true;
        try {
            Class<?> apiClass = Class.forName(REDIS_BUNGEE_API_CLASS);
            Method getApi = apiClass.getMethod("getRedisBungeeApi");
            redisApi = getApi.invoke(null);
            if (redisApi != null) {
                sendChannelMessage = redisApi.getClass().getMethod("sendChannelMessage", String.class, String.class);
            }
        } catch (Throwable t) {
            if (!apiUnavailableLogged) {
                apiUnavailableLogged = true;
                geSuit.getInstance().getLogger().debug("ValioBungee/RedisBungee API not available: {}", t.getMessage());
            }
        }
    }

    private void publish(String channel, String json) {
        resolveApi();
        if (redisApi == null || sendChannelMessage == null) return;
        try {
            sendChannelMessage.invoke(redisApi, channel, json);
        } catch (Throwable t) {
            geSuit.getInstance().getLogger().warn("Failed to publish to Redis channel \"{}\": {}", channel, t.getMessage());
        }
    }

    @Subscribe
    public void onBanPlayer(BanPlayerEvent event) {
        JsonObject o = new JsonObject();
        o.addProperty("actionBy", normalizeConsole(event.getBannedBy()));
        o.addProperty("targetUsername", event.getPlayerName());
        o.addProperty("targetUuid", uuidToString(event.getPlayerId()));
        o.addProperty("reason", event.getReason());
        BanPlayerEvent.BanType type = event.getType();
        String typeStr = type == BanPlayerEvent.BanType.IP ? "ip" : type == BanPlayerEvent.BanType.Temporary ? "temp" : "name";
        o.addProperty("type", typeStr);
        if (type == BanPlayerEvent.BanType.Temporary) {
            Date until = event.getUnbanDate();
            if (until != null) {
                o.addProperty("until", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(until.getTime())));
            }
        }
        if (event.getPlayerIP() != null) {
            o.addProperty("ip", event.getPlayerIP().getHostAddress());
        }
        publish(CHANNEL_BAN, gson.toJson(o));
    }

    @Subscribe
    public void onWarnPlayer(WarnPlayerEvent event) {
        JsonObject o = new JsonObject();
        o.addProperty("actionBy", normalizeConsole(event.getBy()));
        o.addProperty("targetUsername", event.getPlayerName());
        o.addProperty("targetUuid", uuidToString(event.getPlayerId()));
        o.addProperty("reason", event.getReason());
        o.addProperty("warnCount", event.getWarnCount());
        o.addProperty("action", event.getAction().name().toLowerCase());
        o.addProperty("actionExtra", event.getActionExtra() != null ? event.getActionExtra() : "");
        publish(CHANNEL_WARN, gson.toJson(o));
    }

    @Subscribe
    public void onUnbanPlayer(UnbanPlayerEvent event) {
        JsonObject o = new JsonObject();
        o.addProperty("actionBy", normalizeConsole(event.getUnbannedBy()));
        o.addProperty("targetUsername", event.getPlayerName());
        o.addProperty("targetUuid", uuidToString(event.getPlayerId()));
        publish(CHANNEL_UNBAN, gson.toJson(o));
    }
}
