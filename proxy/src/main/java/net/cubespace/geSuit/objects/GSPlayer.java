package net.cubespace.geSuit.objects;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GSPlayer {

    private String playername;
    private final String uuid;
    private boolean acceptingTeleports;
    private String server = null;
    private String ip;
    private Timestamp lastOnline;
    private Timestamp firstOnline;

    private final HashMap<String, ArrayList<Home>> homes = new HashMap<>();
    private Track previousName;
    private Location deathBackLocation;
    private Location teleportBackLocation;
    private boolean lastBack;
    private boolean firstConnect = true;
    private boolean joinAnnounced = false;
    private boolean isFirstJoin = false;
    private boolean newSpawn = false;
    private long loginTime;

    public GSPlayer(String name, String uuid, boolean tps) {
        this(name, uuid, tps, null);
    }

    public GSPlayer(String name, String uuid, boolean tps, String ip) {
        this(name, uuid, tps, false, ip, new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
    }

    public GSPlayer(String name, String uuid, boolean tps, boolean newspawn, String ip, Timestamp lastOnline, Timestamp firstOnline) {
        this.playername = name;
        this.uuid = uuid;
        this.acceptingTeleports = tps;
        this.ip = ip;
        this.lastOnline = lastOnline;
        this.firstOnline = firstOnline;
        this.newSpawn = newspawn;
        this.loginTime = new Date().getTime();
    }

    public String getName() {
        return playername;
    }

    public void setName(String newPlayerName) {
        playername = newPlayerName;
    }

    /** Returns the Velocity player if online. */
    public Player getPlayer() {
        return geSuit.getInstance().getProxy().getPlayer(playername).orElse(null);
    }

    /** @deprecated Use getPlayer() for Velocity. Kept for compatibility in method names. */
    public Player getProxiedPlayer() {
        return getPlayer();
    }

    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) return;
        Player p = getPlayer();
        if (p == null) return;
        for (String line : message.split("\n|\\{N\\}")) {
            p.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(line)));
        }
    }

    public boolean acceptingTeleports() {
        return this.acceptingTeleports;
    }

    public void setAcceptingTeleports(boolean tp) {
        this.acceptingTeleports = tp;
    }

    public void setDeathBackLocation(Location loc) {
        deathBackLocation = loc;
        lastBack = true;
    }

    public boolean hasDeathBackLocation() {
        return deathBackLocation != null;
    }

    public void setTeleportBackLocation(Location loc) {
        teleportBackLocation = loc;
        lastBack = false;
    }

    public Location getLastBackLocation() {
        return lastBack ? deathBackLocation : teleportBackLocation;
    }

    public boolean hasTeleportBackLocation() {
        return teleportBackLocation != null;
    }

    public Location getDeathBackLocation() {
        return deathBackLocation;
    }

    public Location getTeleportBackLocation() {
        return teleportBackLocation;
    }

    public String getServer() {
        Player p = getPlayer();
        if (p == null) return server;
        return p.getCurrentServer().map(sc -> sc.getServerInfo().getName()).orElse(server);
    }

    public HashMap<String, ArrayList<Home>> getHomes() {
        return homes;
    }

    public boolean firstConnect() {
        return firstConnect;
    }

    public void connected() {
        firstConnect = false;
    }

    public void connectTo(RegisteredServer s) {
        Player p = getPlayer();
        if (p != null) {
            p.createConnectionRequest(s).fireAndForget();
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ipAddress) {
        this.ip = ipAddress;
    }

    public Timestamp getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Timestamp value) {
        lastOnline = value;
    }

    public Timestamp getFirstOnline() {
        return firstOnline;
    }

    public void setFirstOnline(Timestamp value) {
        firstOnline = value;
    }

    public boolean isFirstJoin() {
        return isFirstJoin;
    }

    public void setFirstJoin(boolean value) {
        isFirstJoin = value;
    }

    public boolean hasJoinAnnounced() {
        return joinAnnounced;
    }

    public void setJoinAnnounced(boolean joinAnnounced) {
        this.joinAnnounced = joinAnnounced;
    }

    public boolean isNewSpawn() {
        return newSpawn;
    }

    public void setNewSpawn(boolean newSpawn) {
        this.newSpawn = newSpawn;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public void setLastName(Track track) {
        previousName = track;
    }

    public Track getLastName() {
        return previousName;
    }
}
