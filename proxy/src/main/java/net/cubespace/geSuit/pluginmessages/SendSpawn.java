package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Spawn;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendSpawn {

    public static void execute(Spawn spawn) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("SendSpawn");
            out.writeUTF(spawn.getName());
            out.writeUTF(spawn.getLocation().getWorld());
            out.writeDouble(spawn.getLocation().getX());
            out.writeDouble(spawn.getLocation().getY());
            out.writeDouble(spawn.getLocation().getZ());
            out.writeFloat(spawn.getLocation().getYaw());
            out.writeFloat(spawn.getLocation().getPitch());
        } catch (IOException e) {
            e.printStackTrace();
        }
        spawn.getLocation().getServer().ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.SPAWN_CHANNEL, server, bytes))
                .schedule());
    }
}
