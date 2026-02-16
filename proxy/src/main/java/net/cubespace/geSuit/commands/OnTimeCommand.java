package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class OnTimeCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        String[] args = inv.arguments();
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        if (args.length == 0) {
            if (source instanceof Player) {
                BansManager.displayPlayerOnTime(senderName, senderName);
            } else {
                PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_ONTIME_USAGE);
            }
            return;
        }
        if (args[0].equalsIgnoreCase("top")) {
            int page = 1;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    PlayerManager.sendMessageToTarget(source, "You specified an invalid page number.");
                    return;
                }
            }
            BansManager.displayOnTimeTop(senderName, page);
            return;
        }
        BansManager.displayPlayerOnTime(senderName, args[0]);
    }
}
