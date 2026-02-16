package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class KickHistoryCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (!ConfigManager.bans.RecordKicks) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_KICKHISTORY_DISABLED);
            return;
        }
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_KICKHISTORY_USAGE);
            return;
        }
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.displayPlayerKickHistory(senderName, args[0], true);
    }
}
