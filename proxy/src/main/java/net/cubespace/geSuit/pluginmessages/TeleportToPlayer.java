package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TeleportToPlayer {

    public static void execute(GSPlayer player, GSPlayer target) {
        String targetServer = target.getServer();
        if (targetServer != null && !player.getServer().equals(targetServer)) {
            geSuit.getInstance().getProxy().getServer(targetServer).ifPresent(player::connectTo);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("TeleportToPlayer");
            out.writeUTF(player.getName());
            out.writeUTF(target.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.getInstance().getProxy().getServer(target.getServer()).ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.TELEPORT_CHANNEL, server, bytes))
                .schedule());
    }
}
