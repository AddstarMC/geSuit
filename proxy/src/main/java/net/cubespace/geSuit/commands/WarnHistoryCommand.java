package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class WarnHistoryCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (!(source.hasPermission("gesuit.proxy.warnhistory") || source.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_WARNHISTORY_USAGE);
            return;
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        if (args[0].contains(".")) {
            BansManager.displayIPWarnBanHistory(senderName, args[0]);
        } else {
            BansManager.displayPlayerWarnBanHistory(senderName, args[0]);
        }
    }
}
