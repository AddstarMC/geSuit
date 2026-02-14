package net.cubespace.geSuit.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LockDownManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.PlayerManager;

public class LockdownCommand implements SimpleCommand {

    @Override
    public void execute(SimpleCommand.Invocation inv) {
        var sender = inv.source();
        String[] args = inv.arguments();
        if (sender instanceof Player) {
            PlayerManager.sendMessageToTarget(sender, Utilities.colorize("&c You cannot perform that command"));
            return;
        }
        if (!(sender.hasPermission("gesuit.admin") || sender.hasPermission("gesuit.lockdown"))) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);
            return;
        }
        long lockdowntime = 0;
        String senderName = sender instanceof Player ? ((Player) sender).getUsername() : "Console";

        if (args.length == 0) {
            long time = TimeParser.parseStringtoMillisecs(ConfigManager.lockdown.LockdownTime);
            if (time > 0) {
                lockdowntime = System.currentTimeMillis() + time;
                LockDownManager.startLockDown(lockdowntime, "");
                PlayerManager.sendMessageToTarget(sender, "Lockdown Expiry Time:" + lockdowntime + " System Time:" + System.currentTimeMillis());
            } else {
                PlayerManager.sendMessageToTarget(sender, "Could not parse time: " + ConfigManager.lockdown.LockdownTime);
            }
            return;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("end")) {
                LockDownManager.endLockDown();
                if (LockDownManager.isLockedDown()) {
                    PlayerManager.sendMessageToTarget(sender, "Lockdown could not end and is persisting");
                }
                return;
            }
            if (args[0].equalsIgnoreCase("status")) {
                if (LockDownManager.checkExpiry()) {
                    PlayerManager.sendMessageToTarget(sender, "Lockdown is not active");
                } else {
                    PlayerManager.sendMessageToTarget(sender, "Lockdown is active until " + LockDownManager.getExpiryTimeString());
                }
                return;
            }
            if (args[0].equalsIgnoreCase("help")) {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.LOCKDOWN_USAGE);
                return;
            }
            try {
                lockdowntime = System.currentTimeMillis() + TimeParser.parseStringtoMillisecs(args[0]);
                if (lockdowntime > System.currentTimeMillis()) {
                    LockDownManager.startLockDown(lockdowntime, "");
                } else {
                    throw new NumberFormatException("Lockdowntime: " + args[0]);
                }
            } catch (NumberFormatException e) {
                PlayerManager.sendMessageToTarget(sender, "Could not format time from " + args[0]);
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.LOCKDOWN_USAGE);
                return;
            }
            return;
        }
        if (args.length > 1) {
            try {
                lockdowntime = TimeParser.parseStringtoMillisecs(args[0]);
                if (lockdowntime == 0) throw new NumberFormatException("Lockdowntime: " + args[0]);
            } catch (NumberFormatException e) {
                PlayerManager.sendMessageToTarget(sender, "Could not format time from " + args[0]);
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.LOCKDOWN_USAGE);
                LoggingManager.log("Could not format time from " + args[0] + " error:" + e.getMessage());
                return;
            }
            long expiryTime = System.currentTimeMillis() + lockdowntime;
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                if (i != 1) builder.append(' ');
                builder.append(args[i]);
            }
            LockDownManager.startLockDown(senderName, expiryTime, builder.toString());
        }
    }
}
