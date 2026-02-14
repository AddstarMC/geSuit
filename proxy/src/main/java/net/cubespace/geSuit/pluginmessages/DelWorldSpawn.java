package net.cubespace.geSuit.pluginmessages;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DelWorldSpawn {

    public static void execute(RegisteredServer server, String world) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("DelWorldSpawn");
            out.writeUTF(world);
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.getInstance().getProxy().getScheduler()
            .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.SPAWN_CHANNEL, server, bytes))
            .schedule();
    }
}
