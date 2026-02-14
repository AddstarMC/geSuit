package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class ForceBatchNameHistoryUpdateCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        if (source instanceof Player) return;
        String[] args = inv.arguments();
        if (args.length == 0 || args.length > 2) {
            PlayerManager.sendMessageToTarget(source, ConfigManager.messages.PROXY_COMMAND_BATCHNAMEHISTORYUPDATE_USAGE);
            return;
        }
        boolean all = args[0].equals("all");
        String startUUID = args.length == 2 ? args[0] : "";
        String endUUID = args.length == 2 ? args[1] : "";
        PlayerManager.batchUpdatePlayerNames(source, all, startUUID, endUUID);
    }
}
