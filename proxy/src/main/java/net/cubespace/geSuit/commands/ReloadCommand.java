package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.managers.AnnouncementManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class ReloadCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (!(source.hasPermission("gesuit.reload") || source.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        try {
            ConfigManager.announcements.reload();
            ConfigManager.bans.reload();
            ConfigManager.main.reload();
            ConfigManager.spawn.reload();
            ConfigManager.messages.reload();
            ConfigManager.teleport.reload();
            ConfigManager.motd.load();
            ConfigManager.motdNew.load();
            AnnouncementManager.reloadAnnouncements();
            PlayerManager.sendMessageToTarget(source, "All Configs reloaded");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            PlayerManager.sendMessageToTarget(source, "Could not reload. Check the logs");
        }
    }
}
