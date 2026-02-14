package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LeavingServer {

    public static void execute(GSPlayer player) {
        if (player.getServer() == null) {
            return;
        }
        player.getPlayer().getCurrentServer().ifPresent(connection -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            try {
                out.writeUTF("LeavingServer");
                out.writeUTF(player.getName());
                out.writeUTF(connection.getServerInfo().getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.TELEPORT_CHANNEL, connection.getServer(), bytes))
                .schedule();
        });
    }
}
