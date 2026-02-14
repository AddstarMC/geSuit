package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.AdminCommand;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendAdminCommand {
    public static void execute(AdminCommand adminCommand) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        switch (adminCommand.command) {
            case "restart":
                long time = 0L;
                if (adminCommand.getArgs().size() >= 1) {
                    time = TimeParser.parseStringtoMillisecs(adminCommand.getArgs().get(0));
                    if (time == 0) time = 10000L;
                }
                try {
                    out.writeUTF("ServerRestart");
                    out.writeUTF(adminCommand.getServer());
                    out.writeUTF(adminCommand.getCommandSender());
                    out.writeLong(time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                geSuit.getInstance().getProxy().getServer(adminCommand.getServer()).ifPresent(server ->
                    geSuit.getInstance().getProxy().getScheduler()
                        .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.ADMIN_CHANNEL, server, bytes))
                        .schedule());
                break;
            default:
                break;
        }
    }
}
