package net.cubespace.geSuit;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.cubespace.geSuit.commands.*;
import net.cubespace.geSuit.listeners.*;
import net.cubespace.geSuit.managers.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.nio.file.Path;

import lombok.Getter;

/**
 * geSuit proxy plugin for Velocity.
 * Handles plugin messages from Bukkit modules and provides proxy-side commands and logic.
 */
@Plugin(
    id = "gesuit",
    name = "geSuit",
    version = "2.0.0-SNAPSHOT",
    description = "geSuit proxy plugin for Velocity",
    authors = {"AddstarMC"}
)
public class geSuit {

    private static geSuit instance;

    public static geSuit getInstance() {
        return instance;
    }

    @Getter
    private final ProxyServer proxy;
    @Getter
    private final Logger logger;
    @Getter
    private final Path dataDirectory;

    private int debugEnabled = 0;
    public static APIManager api;

    /** Cached ChatControlMirror API when the plugin is present; resolved at init via reflection. */
    private volatile Object chatControlMirrorApi;

    @Inject
    public geSuit(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    /** Returns the plugin data directory as a File for config/compat code that expects File. */
    public java.io.File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        LoggingManager.log(LegacyComponentSerializer.legacySection().serialize(Component.text("Starting geSuit")));
        LoggingManager.log(LegacyComponentSerializer.legacySection().serialize(Component.text("Initialising Managers")));
        DatabaseManager.init();
        registerChannels();
        registerListeners();
        registerCommands();
        GeoIPManager.initialize();
        LockDownManager.initialize();
        api = new APIManager();
        resolveChatControlMirrorApi();
    }

    /**
     * Resolve ChatControlMirror API from the plugin container (reflection so CCM is not required at runtime).
     */
    private void resolveChatControlMirrorApi() {
        proxy.getPluginManager().getPlugin("chatcontrolmirror")
                .flatMap(container -> container.getInstance())
                .ifPresent(instance -> {
                    try {
                        Class<?> ccmClass = Class.forName("au.com.addstar.ccm.ChatControlMirror");
                        if (ccmClass.isInstance(instance)) {
                            Object api = ccmClass.getMethod("getApi").invoke(instance);
                            if (api != null) {
                                this.chatControlMirrorApi = api;
                                logger.info("ChatControlMirror API available for staff channel integration.");
                            }
                        }
                    } catch (Throwable t) {
                        logger.debug("ChatControlMirror not available: {}", t.getMessage());
                    }
                });
    }

    /**
     * Returns the ChatControlMirror API instance when the plugin is present and integration is enabled; otherwise null.
     * Stored as Object to avoid loading CCM classes when the plugin is absent.
     */
    public Object getChatControlMirrorApi() {
        return chatControlMirrorApi;
    }

    private void registerChannels() {
        boolean legacy = ConfigManager.main.enableLegacy;
        for (CHANNEL_NAMES name : CHANNEL_NAMES.values()) {
            proxy.getChannelRegistrar().register(name.getIdentifier());
        }
        if (legacy) {
            for (CHANNEL_NAMES name : CHANNEL_NAMES.values()) {
                proxy.getChannelRegistrar().register(name.getLegacyIdentifier());
                proxy.getChannelRegistrar().register(name.getLegacyIdentifierLowerCase());
            }
        }
        logger.info("geSuit Report: Proxy has registered plugin message channels for gesuit:* and legacy geSuit* (if enabled).");
    }

    private void registerCommands() {
        var commandManager = proxy.getCommandManager();
        if (ConfigManager.main.MOTD_Enabled) {
            commandManager.register(commandManager.metaBuilder("motd").build(), new MOTDCommand());
        }
        if (ConfigManager.main.Seen_Enabled) {
            commandManager.register(commandManager.metaBuilder("seen").build(), new SeenCommand());
        }
        commandManager.register(commandManager.metaBuilder("unban").build(), new UnbanCommand());
        commandManager.register(commandManager.metaBuilder("ban").build(), new BanCommand());
        commandManager.register(commandManager.metaBuilder("tempban").build(), new TempBanCommand());
        commandManager.register(commandManager.metaBuilder("warn").build(), new WarnCommand());
        commandManager.register(commandManager.metaBuilder("where").build(), new WhereCommand());
        commandManager.register(commandManager.metaBuilder("reload").build(), new ReloadCommand());
        commandManager.register(commandManager.metaBuilder("debug").build(), new DebugCommand());
        commandManager.register(commandManager.metaBuilder("warnhistory").build(), new WarnHistoryCommand());
        commandManager.register(commandManager.metaBuilder("kickhistory").build(), new KickHistoryCommand());
        commandManager.register(commandManager.metaBuilder("names").build(), new NamesCommand());
        commandManager.register(commandManager.metaBuilder("lockdown").build(), new LockdownCommand());
        commandManager.register(commandManager.metaBuilder("forcenamehistory").build(), new ForceNameHistoryCommand());
        commandManager.register(commandManager.metaBuilder("forcebatchnamehistoryupdate").build(), new ForceBatchNameHistoryUpdateCommand());
        commandManager.register(commandManager.metaBuilder("activekicks").build(), new ActiveKicksCommand());
        commandManager.register(commandManager.metaBuilder("admin").build(), new AdminCommands());
        commandManager.register(commandManager.metaBuilder("backtonewspawn").build(), new BacktoNewSpawn());
        if (ConfigManager.bans.TrackOnTime) {
            commandManager.register(commandManager.metaBuilder("ontime").build(), new OnTimeCommand());
            commandManager.register(commandManager.metaBuilder("lastlogins").build(), new LastLoginsCommand());
        }
    }

    private void registerListeners() {
        boolean legacy = ConfigManager.main.enableLegacy;
        proxy.getEventManager().register(this, new PlayerListener());
        proxy.getEventManager().register(this, new TeleportsListener());
        proxy.getEventManager().register(this, new SpawnListener());
        proxy.getEventManager().register(this, new TeleportsMessageListener(legacy));
        proxy.getEventManager().register(this, new BansMessageListener(legacy));
        proxy.getEventManager().register(this, new WarpsMessageListener(legacy));
        proxy.getEventManager().register(this, new HomesMessageListener(legacy));
        proxy.getEventManager().register(this, new PortalsMessageListener(legacy));
        proxy.getEventManager().register(this, new SpawnMessageListener(legacy));
        proxy.getEventManager().register(this, new APIMessageListener(legacy));
        proxy.getEventManager().register(this, new AdminMessageListener(legacy));
        if (proxy.getPluginManager().getPlugin("redisbungee").isPresent()) {
            proxy.getEventManager().register(this, new RedisEventPublisher());
            logger.info("Redis event publishing enabled (ValioBungee/RedisBungee detected).");
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled > 0;
    }

    public int getDebugLevel() {
        return debugEnabled;
    }

    public void setDebugEnabled(int debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void DebugMsg(String msg) {
        if (isDebugEnabled()) {
            logger.info("DEBUG: " + msg);
        }
    }

    /**
     * Plugin message channel names. Same as used by Bukkit modules for compatibility.
     */
    public enum CHANNEL_NAMES {
        TELEPORT_CHANNEL("gesuit:teleport", "geSuitTeleport"),
        SPAWN_CHANNEL("gesuit:spawns", "geSuitSpawns"),
        BAN_CHANNEL("gesuit:bans", "geSuitBans"),
        PORTAL_CHANNEL("gesuit:portals", "geSuitPortals"),
        WARP_CHANNEL("gesuit:warps", "geSuitWarps"),
        HOME_CHANNEL("gesuit:homes", "geSuitHomes"),
        API_CHANNEL("gesuit:api", "geSuitAPI"),
        ADMIN_CHANNEL("gesuit:admin", "geSuitAdmin");

        private final MinecraftChannelIdentifier identifier;
        private final LegacyChannelIdentifier legacyIdentifier;
        private final LegacyChannelIdentifier legacyIdentifierLowerCase;

        CHANNEL_NAMES(String namespaced, String legacy) {
            this.identifier = MinecraftChannelIdentifier.from(namespaced);
            this.legacyIdentifier = new LegacyChannelIdentifier(legacy);
            this.legacyIdentifierLowerCase = new LegacyChannelIdentifier(legacy.toLowerCase());
        }

        public MinecraftChannelIdentifier getIdentifier() {
            return identifier;
        }

        public LegacyChannelIdentifier getLegacyIdentifier() {
            return legacyIdentifier;
        }

        public LegacyChannelIdentifier getLegacyIdentifierLowerCase() {
            return legacyIdentifierLowerCase;
        }

        /** Namespaced channel string (e.g. gesuit:teleport) for sending when not using legacy. */
        public String toString() {
            return identifier.getId();
        }

        /** Legacy channel name for older Bukkit plugins. */
        public String getLegacy() {
            return legacyIdentifier.getId();
        }
    }
}
