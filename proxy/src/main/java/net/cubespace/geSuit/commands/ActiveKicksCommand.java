package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.Kick;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActiveKicksCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var source = inv.source();
        long timeOut = ConfigManager.bans.KicksTimeOut;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeOut);
        PlayerManager.sendMessageToTarget(source, "Kick TimeOut: " + minutes + "m");
        BansManager.clearKicks();
        List<Kick> kicks = BansManager.getKicks();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        if (kicks.isEmpty()) {
            PlayerManager.sendMessageToTarget(source, "No Kicks Active");
        } else {
            for (Kick kick : kicks) {
                PlayerManager.sendMessageToTarget(source, "Kicks Active");
                String dateTime = sdf.format(new Date(kick.getBannedOn() + timeOut));
                PlayerManager.sendMessageToTarget(source, kick.toString() + " Expiry:" + dateTime);
            }
        }
    }
}
