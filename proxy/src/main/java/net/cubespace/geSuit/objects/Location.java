package net.cubespace.geSuit.objects;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cubespace.geSuit.geSuit;

/**
 * Represents a location on a backend server. Server is stored by name so that
 * we can resolve to RegisteredServer when sending plugin messages.
 */
public class Location {
    private String serverName;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location(String server, String world, double x, double y, double z, float yaw, float pitch) {
        this.serverName = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(String serialised) {
        String[] loc = serialised.split("~!~");
        serverName = loc[0];
        world = loc[1];
        x = Double.parseDouble(loc[2]);
        y = Double.parseDouble(loc[3]);
        z = Double.parseDouble(loc[4]);
        yaw = Float.parseFloat(loc[5]);
        pitch = Float.parseFloat(loc[6]);
    }

    public Location(RegisteredServer server, String world, double x, double y, double z) {
        this.serverName = server.getServerInfo().getName();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        yaw = 0;
        pitch = 0;
    }

    public Location(RegisteredServer server, String world, double x, double y, double z, float yaw, float pitch) {
        this.serverName = server.getServerInfo().getName();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location(String serverName, String world, double x, double y, double z) {
        this.serverName = serverName;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        yaw = 0;
        pitch = 0;
    }

    /** Server name (for serialization and display). */
    public String getServerName() {
        return serverName;
    }

    /** Resolve to Velocity RegisteredServer for sending plugin messages. */
    public java.util.Optional<RegisteredServer> getServer() {
        return geSuit.getInstance().getProxy().getServer(serverName);
    }

    public void setServer(String server) {
        this.serverName = server;
    }

    public void setServer(RegisteredServer server) {
        this.serverName = server.getServerInfo().getName();
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    @Override
    public String toString() {
        return serverName + "~!~" + world + "~!~" + x + "~!~" + y + "~!~" + z + "~!~" + yaw + "~!~" + pitch;
    }
}
