package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendPortal {

    public static void execute(Portal p) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("SendPortal");
            out.writeUTF(p.getName());
            out.writeUTF(p.getType());
            out.writeUTF(p.getDest());
            out.writeUTF(p.getFillType());
            Location max = p.getMax();
            out.writeUTF(max.getWorld());
            out.writeDouble(max.getX());
            out.writeDouble(max.getY());
            out.writeDouble(max.getZ());
            Location min = p.getMin();
            out.writeUTF(min.getWorld());
            out.writeDouble(min.getX());
            out.writeDouble(min.getY());
            out.writeDouble(min.getZ());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getServer().ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.PORTAL_CHANNEL, server, bytes))
                .schedule());
    }
}
