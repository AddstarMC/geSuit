package net.cubespace.geSuit.listeners;

import java.util.concurrent.TimeUnit;

import au.com.addstar.bc.event.BCPlayerJoinEvent;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeChatListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoinMessage(BCPlayerJoinEvent event) {
//        GSPlayer player = PlayerManager.getPlayer(event.getPlayer());
//        if (player != null) {
//            // No join message for new players, alternate message is used
//            if (player.isFirstJoin()) {
//                event.setJoinMessage(null);
//            } else if (player.getLastName() != null) {
//                Track lastName = player.getLastName();
//                
//                // Display the last name if it changed less than the config value days ago
//                if (System.currentTimeMillis() - lastName.getLastSeen() < TimeUnit.DAYS.toMillis(ConfigManager.bans.NameChangeNotifyTime)) {
//                	// Always log recent name changes to console
//                    geSuit.getLogger().info(Global.getMessages().get(
//                	        "connect.join.namechange.log",
//                			"player", event.getPlayer().getDisplayName(),
//                			"old", lastName.getName()));
//
//                    // Do not show this for nicknamed players (usually the case that the previous name was rude or inappropriate)
//                    if (event.getPlayer().getName().equals(event.getPlayer().getDisplayName())) {
//                        event.setJoinMessage(Global.getMessages().get(
//                                "connect.join.namechange",
//                                "player", event.getPlayer().getDisplayName(),
//                                "old", lastName.getName()));
//                    }
//                }
//            }
//        }
    }
}
