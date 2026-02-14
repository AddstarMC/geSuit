package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class MOTDCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (!(source.hasPermission("gesuit.motd") || source.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        String playerName = source instanceof Player ? ((Player) source).getUsername() : "Console";
        PlayerManager.sendMessageToTarget(source, ConfigManager.motd.getMOTD().replace("{player}", playerName));
    }
}
