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
        if (source instanceof Player) return;
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_ONTIME_USAGE);
            return;
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        if (args[0].equalsIgnoreCase("top")) {
            int page = 1;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    PlayerManager.sendMessageToTarget(source, "You specified an invalid page number.");
                    return;
                }
                BansManager.displayOnTimeTop(senderName, page);
            } else {
                BansManager.displayPlayerOnTime(senderName, args[0]);
            }
        } else {
            BansManager.displayPlayerOnTime(senderName, args[0]);
        }
    }
}
