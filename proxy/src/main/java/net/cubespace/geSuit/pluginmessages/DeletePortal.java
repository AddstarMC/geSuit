package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeletePortal {

    public static void execute(Portal p) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("DeletePortal");
            out.writeUTF(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.getServer().ifPresent(server ->
            geSuit.getInstance().getProxy().getScheduler()
                .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.PORTAL_CHANNEL, server, bytes))
                .schedule());
    }
}
