package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class TempBanCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (source instanceof Player) return;
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_TEMPBAN_USAGE);
            return;
        }
        StringBuilder reason = new StringBuilder();
        for (int x = 2; x < args.length; x++) {
            if (reason.length() == 0) reason.append(args[x]);
            else reason.append(" ").append(args[x]);
        }
        if (reason.length() == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.TEMP_BAN_REASON_REQUIRED);
            return;
        }
        int seconds = TimeParser.parseString(args[1]);
        if (seconds == 0) return;
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.tempBanPlayer(senderName, args[0], seconds, reason.toString());
    }
}
