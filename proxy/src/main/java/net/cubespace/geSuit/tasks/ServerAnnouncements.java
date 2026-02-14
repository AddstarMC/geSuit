package net.cubespace.geSuit.tasks;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cubespace.geSuit.Utilities;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ServerAnnouncements implements Runnable {

    private final ArrayList<String> list = new ArrayList<>();
    private int count = 0;
    private final RegisteredServer server;

    public ServerAnnouncements(RegisteredServer server) {
        this.server = server;
    }

    public void addAnnouncement(String message) {
        list.add(Utilities.colorize(message));
    }

    @Override
    public void run() {
        if (list.isEmpty()) return;
        String serverName = server.getServerInfo().getName();
        List<Player> players = net.cubespace.geSuit.geSuit.getInstance().getProxy().getAllPlayers().stream()
            .filter(p -> p.getCurrentServer().map(s -> s.getServerInfo().getName().equals(serverName)).orElse(false))
            .toList();
        if (players.isEmpty()) return;
        String msg = list.get(count);
        for (Player player : players) {
            for (String line : msg.split("\n")) {
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(Utilities.colorize(line)));
            }
        }
        count++;
        if ((count + 1) > list.size()) {
            count = 0;
        }
    }
}
