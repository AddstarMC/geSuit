package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class WarnCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (source instanceof Player) return;
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_WARN_USAGE);
            return;
        }
        StringBuilder reason = new StringBuilder();
        for (int x = 2; x < args.length; x++) {
            if (reason.length() == 0) reason.append(args[x]);
            else reason.append(" ").append(args[x]);
        }
        if (reason.length() == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.WARN_REASON_REQUIRED);
            return;
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.warnPlayer(senderName, args[0], reason.toString());
    }
}
