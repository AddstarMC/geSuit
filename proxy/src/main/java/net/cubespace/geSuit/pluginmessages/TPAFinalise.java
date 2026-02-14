package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TPAFinalise {

    public static void execute(GSPlayer player, GSPlayer target) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("TeleportAccept");
            out.writeUTF(player.getName());
            out.writeUTF(target.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.getInstance().getProxy().getServer(player.getServer()).ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.TELEPORT_CHANNEL, server, bytes))
                .schedule());
    }
}
