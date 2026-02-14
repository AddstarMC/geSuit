package net.cubespace.geSuit.objects;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cubespace.geSuit.geSuit;

import java.util.Optional;

public class Portal {
    final String name;
    final String server;
    final String fillType;
    final String type;
    final String dest;
    final Location max;
    final Location min;

    public Portal(String name, String server, String fillType, String type, String dest, Location max, Location min) {
        this.name = name;
        this.server = server;
        this.fillType = fillType;
        this.type = type;
        this.dest = dest;
        this.max = max;
        this.min = min;
    }

    public String getServerName() {
        return server;
    }

    public Optional<RegisteredServer> getServer() {
        return geSuit.getInstance().getProxy().getServer(server);
    }

    public String getName() { return name; }
    public String getFillType() { return fillType; }
    public String getType() { return type; }
    public String getDest() { return dest; }
    public Location getMax() { return max; }
    public Location getMin() { return min; }
}
