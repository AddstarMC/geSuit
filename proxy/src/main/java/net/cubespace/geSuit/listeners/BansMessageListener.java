package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.LockDownManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.managers.SpawnManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BansMessageListener extends MessageListener {

    public BansMessageListener(boolean legacy) {
        super(legacy, geSuit.CHANNEL_NAMES.BAN_CHANNEL);
    }

    @Subscribe
    public void receivePluginMessage(PluginMessageEvent event) throws IOException {
        if (!eventMatched(event)) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        switch (task) {
            case "KickPlayer":
                BansManager.kickPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "BanPlayer":
                BansManager.banPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "WarnPlayer":
                BansManager.warnPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "TempBanPlayer":
                BansManager.tempBanPlayer(in.readUTF(), in.readUTF(), in.readInt(), in.readUTF());
                break;
            case "KickAll":
                BansManager.kickAll(in.readUTF(), in.readUTF());
                break;
            case "NewSpawn":
                SpawnManager.sendPlayerToNewPlayerSpawn(in.readUTF(), in.readUTF());
                break;
            case "UnbanPlayer":
                BansManager.unbanPlayer(in.readUTF(), in.readUTF());
                break;
            case "IPBanPlayer":
                BansManager.banIP(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "CheckPlayerBans":
                BansManager.checkPlayersBan(in.readUTF(), in.readUTF());
                break;
            case "DisplayPlayerBanHistory":
                BansManager.displayPlayerBanHistory(in.readUTF(), in.readUTF());
                break;
            case "DisplayPlayerWarnBanHistory":
                String sentBy = in.readUTF();
                String playerOrIP = in.readUTF();

                if (playerOrIP.contains(".")) {
                    BansManager.displayIPWarnBanHistory(sentBy, playerOrIP);
                } else {
                    BansManager.displayPlayerWarnBanHistory(sentBy, playerOrIP);
                }

                break;
            case "DisplayPlayerWarnHistory":
            	BansManager.displayPlayerWarnHistory(in.readUTF(), in.readUTF(), in.readBoolean());
                break;
            case "DisplayWhereHistory":
                BansManager.displayWhereHistory(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "DisplayOnTimeHistory":
                BansManager.displayPlayerOnTime(in.readUTF(), in.readUTF());
                break;
            case "DisplayOnTimeTop":
                BansManager.displayOnTimeTop(in.readUTF(), in.readInt());
                break;
            case "DisplayLastLogins":
                BansManager.displayLastLogins(in.readUTF(), in.readUTF(), in.readInt());
                break;
            case "ReloadBans":
                BansManager.reloadBans(in.readUTF());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
            case "DisplayNameHistory":
                BansManager.displayNameHistory(in.readUTF(), in.readUTF());
                break;
            case "LockDown":
                LockDownManager.startLockDown(in.readUTF(), in.readLong(), in.readUTF());
                break;
            case "EndLockDown":
                LockDownManager.endLockDown(in.readUTF());
                break;
            case "LockDownStatus":
                LockDownManager.checkExpiry(in.readUTF());
                break;
            default:
                geSuit.getInstance().getLogger().info(task + " : has no handler ");
        }
    }

}
