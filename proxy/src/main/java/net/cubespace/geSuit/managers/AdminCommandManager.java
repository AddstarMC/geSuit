package net.cubespace.geSuit.managers;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.AdminCommand;
import net.cubespace.geSuit.pluginmessages.SendAdminCommand;

public class AdminCommandManager {

    public static void sendAdminCommand(CommandSource sender, String server, String command, String... args) {
        String senderName = sender instanceof Player ? ((Player) sender).getUsername() : "Console";
        AdminCommand adminCommand = new AdminCommand(command, server, senderName, args);
        if (geSuit.getInstance().getProxy().getServer(server).isPresent()) {
            SendAdminCommand.execute(adminCommand);
        } else {
            PlayerManager.sendMessageToTarget(sender, "No server with name " + server);
        }
    }
}
