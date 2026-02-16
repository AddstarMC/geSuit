package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class LastLoginsCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_LASTLOGINS_USAGE);
            return;
        }
        int num = 5;
        if (args.length == 2) {
            try {
                num = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                PlayerManager.sendMessageToTarget(source, "You specified an invalid number.");
                return;
            }
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.displayLastLogins(senderName, args[0], num);
    }
}
