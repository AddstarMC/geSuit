package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class SeenCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (!(source.hasPermission("gesuit.seen") || source.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        String[] args = inv.arguments();
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_SEEN_USAGE);
            return;
        }
        PlayerManager.sendMessageToTarget(source, PlayerManager.getLastSeeninfos(args[0], source.hasPermission("gesuit.seen.extra"), source.hasPermission("gesuit.seen.vanish")));
    }
}
