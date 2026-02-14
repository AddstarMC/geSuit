package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class WhereCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_WHERE_USAGE);
            return;
        }
        String search, options;
        if (args.length == 1) {
            options = "";
            search = args[0];
        } else {
            options = args[0];
            search = args[1];
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.displayWhereHistory(senderName, options, search);
    }
}
