package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EnableBukkitDebug {
    public static void execute(String serverName) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("EnableDebug");
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.getInstance().getProxy().getServer(serverName).ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.ADMIN_CHANNEL, server, bytes))
                .schedule());
    }
}
