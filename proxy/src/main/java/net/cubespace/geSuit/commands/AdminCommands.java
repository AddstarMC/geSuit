package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.AdminCommandManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class AdminCommands implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var sender = inv.source();
        String[] args = inv.arguments();
        if (args.length == 0) {
            displayHelp(sender);
            return;
        }
        switch (args[0]) {
            case "restart":
                if (args.length != 3) {
                    displayHelp(sender);
                    break;
                }
                String server = args[1];
                String timeString = args[2];
                AdminCommandManager.sendAdminCommand(sender, server, "restart", timeString);
                PlayerManager.sendMessageToTarget(sender, "Server Restart Requested for " + server + " in " + timeString);
                break;
            default:
                displayHelp(sender);
                break;
        }
    }

    private void displayHelp(com.velocitypowered.api.command.CommandSource sender) {
        PlayerManager.sendMessageToTarget(sender, "usage: restart <server> <time>");
    }
}
