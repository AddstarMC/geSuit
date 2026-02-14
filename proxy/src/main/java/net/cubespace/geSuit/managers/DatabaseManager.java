package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.database.*;
import net.cubespace.geSuit.geSuit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class DatabaseManager {
    public static boolean enabled = false;
    public static final ConnectionPool connectionPool;
    public static final Homes homes;
    public static final Bans bans;
    public static final Players players;
    public static final Portals portals;
    public static final Spawns spawns;
    public static final Warps warps;
    public static final Tracking tracking;
    public static final OnTime ontime;

    static {
        //geSuit.getInstance().getLogger().log(Level.INFO, "Instantiating DatabaseManager");
        players = new Players();
        homes = new Homes();
        bans = new Bans();
        portals = new Portals();
        spawns = new Spawns();
        warps = new Warps();
        tracking = new Tracking();
        ontime = new OnTime();

        connectionPool = new ConnectionPool();
        connectionPool.addRepository(players);
        connectionPool.addRepository(homes);
        connectionPool.addRepository(bans);
        connectionPool.addRepository(portals);
        connectionPool.addRepository(spawns);
        connectionPool.addRepository(warps);
        connectionPool.addRepository(tracking);
        connectionPool.addRepository(ontime);
        geSuit.getInstance().getProxy().getScheduler().buildTask(geSuit.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {

                    if (connectionPool.initialiseConnections(ConfigManager.main.Database)) {
                        connectionPool.AddStringColumnIfMissing("warps", "description", 128);
                        AnnouncementManager.loadAnnouncements();
                        WarpsManager.loadWarpLocations();
                        PortalManager.loadPortals();
                        SpawnManager.loadSpawns();
                        enabled = true;
                    }
                } catch (IllegalStateException e) {
                    geSuit.getInstance().getLogger().warn("Gesuit could not initaliaze the database.... as " +
                          "a result no warps portals or spawn locations are loaded....");
                    enabled = false;
                }
            }
        }).schedule();
    }

    public static void init() {
        //creation
    }

}
