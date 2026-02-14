package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class BanCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (source instanceof Player) {
            return;
        }
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_BAN_USAGE);
            return;
        }
        String reason = "";
        if (args.length > 1) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                if (i != 1) builder.append(' ');
                builder.append(args[i]);
            }
            reason = builder.toString();
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        if (Utilities.isIPAddress(args[0])) {
            BansManager.banIP(senderName, args[0], reason);
        } else {
            if (reason.isEmpty()) {
                PlayerManager.sendMessageToTarget(source, ConfigManager.messages.BAN_REASON_REQUIRED);
                return;
            }
            BansManager.banPlayer(source, args[0], reason, false);
        }
    }
}
