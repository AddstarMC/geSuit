package net.cubespace.geSuit;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import net.cubespace.geSuit.core.BukkitPlayerManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.ServerManager;
import net.cubespace.geSuit.core.geCore;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelManager;
import net.cubespace.geSuit.core.channel.ConnectionNotifier;
import net.cubespace.geSuit.core.channel.RedisChannelManager;
import net.cubespace.geSuit.core.commands.BukkitCommandManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.storage.RedisConnection;
import net.cubespace.geSuit.core.storage.StorageProvider;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GSPlugin extends JavaPlugin implements ConnectionNotifier {
    
    private RedisConnection redis;
    private RedisChannelManager channelManager;
    
    private ModuleManager moduleManager;
    private BukkitCommandManager commandManager;
    
    @Override
    public void onEnable() {
        getLogger().info("Starting geSuit");
        
        redis = createRedis();
        if (redis == null) {
            getLogger().severe("Redis failed to initialize. Please fix the problem and restart the server.");
            return;
        }
        redis.setNotifier(this);
        
        channelManager = initializeChannelManager();
        
        BukkitPlatform platform = new BukkitPlatform(this);
        StorageProvider storageProvider = new StorageProvider(redis);
        
        // Create global manager
        Channel<BaseMessage> channel = channelManager.createChannel("players", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        BukkitPlayerManager playerManager = new BukkitPlayerManager(channel, channelManager.getRedis(), storageProvider, platform);
        playerManager.initRedis();
        ServerManager serverManager = new ServerManager(platform);
        
        GlobalBukkitManager globalManager = new GlobalBukkitManager(channel, playerManager, serverManager, new Messages());
        
        // Initialize core
        commandManager = new BukkitCommandManager();
        geCore core = new geCore(platform, globalManager, channelManager, commandManager, storageProvider);
        Global.setInstance(core);
        
        // Load player manager
        globalManager.requestNetworkUpdate();
        
        getLogger().info("Initializing modules:");
        moduleManager = new ModuleManager(this);
        moduleManager.loadAll();
        moduleManager.enableAll();
    }
    
    @Override
    public void onDisable() {
        moduleManager.disableAll();
        
        channelManager.shutdown();
        redis.shutdown();
    }
    
    private RedisConnection createRedis() {
        FileConfiguration config = getConfig();
        
        try {
            RedisConnection redis = new RedisConnection(config.getString("redis.host", "localhost"), config.getInt("redis.port", 6379), config.getString("redis.password", ""), Bukkit.getPort());
            redis.connect();
            return redis;
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to connect to redis:", e);
            return null;
        }
    }
    
    private RedisChannelManager initializeChannelManager() {
        final RedisChannelManager channelManager = new RedisChannelManager(redis, getLogger());
        
        final CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                channelManager.initialize(latch);
            }
        }, "GSSubscriptionThread");

        thread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        
        return channelManager;
    }
    
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    @Override
    public void onConnectionRestored() {
        getLogger().info("Connection to redis has been restored.");
    }

    @Override
    public void onConnectionLost(Throwable e) {
        getLogger().log(Level.WARNING, "Connection to redis has been lost. Most geSuit functions will be unavailable until it is restored.", e);
    }
}