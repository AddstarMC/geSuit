package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class ForceNameHistoryCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (source instanceof Player) return;
        String[] args = inv.arguments();
        if (args.length != 1) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_NAMEHISTORYUPDATE_USAGE);
            return;
        }
        PlayerManager.retrieveOldNames(source, args[0]);
        PlayerManager.sendMessageToTarget(source, "Player: " + args[0] + " updated.");
        String senderName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        BansManager.displayNameHistory(senderName, args[0]);
    }
}
