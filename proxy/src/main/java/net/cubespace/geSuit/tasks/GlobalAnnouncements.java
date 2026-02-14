package net.cubespace.geSuit.tasks;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;

public class GlobalAnnouncements implements Runnable {

    private final ArrayList<String> list = new ArrayList<>();
    private int count = 0;

    public void addAnnouncement(String message) {
        list.add(Utilities.colorize(message));
    }

    @Override
    public void run() {
        if (list.isEmpty()) return;
        Collection<com.velocitypowered.api.proxy.Player> players = geSuit.getInstance().getProxy().getAllPlayers();
        if (players.isEmpty()) return;
        String msg = list.get(count);
        for (var player : players) {
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
