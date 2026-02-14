package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Home;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.pluginmessages.TeleportToLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomesManager {
    public static void createNewHome(GSPlayer player, int serverLimit, int globalLimit, String home, Location loc) {
        if (getHome(player, home) == null) {
            int globalHomeCount = getPlayersGlobalHomeCount(player);
            int serverHomeCount = getPlayersServerHomeCount(player);

            if (globalHomeCount >= globalLimit) {
                PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_HOMES_ALLOWED_GLOBAL);
                return;
            }

            if (serverHomeCount >= serverLimit) {
                PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_HOMES_ALLOWED_SERVER);
                return;
            }
    
            player.getHomes().computeIfAbsent(player.getServer(), k -> new ArrayList<>());

            Home homeObject = new Home(player, home, loc);
            player.getHomes().get(player.getServer()).add(homeObject);
            DatabaseManager.homes.addHome(homeObject);

            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.HOME_SET.replace("{home}", home));
        } else {
            Home home1 = getHome(player, home);
            if (home1 != null && home1.loc.getServerName().equals(loc.getServerName())) {
                home1.setLoc(loc);
                DatabaseManager.homes.updateHome(home1);
                PlayerManager.sendMessageToTarget(player, ConfigManager.messages.HOME_UPDATED.replace("{home}", home));
            } else {
                PlayerManager.sendMessageToTarget(player, ConfigManager.messages.HOME_EXISTS_OTHER_SERVER.replace("{home}", home));
            }
        }
    }

    private static int getPlayersGlobalHomeCount(GSPlayer player) {
        int count = 0;

        for (ArrayList<Home> list : player.getHomes().values()) {
            count += list.size();
        }

        return count;
    }

    private static int getPlayersServerHomeCount(GSPlayer player) {
        ArrayList<Home> list = player.getHomes().get(player.getServer());

        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    public static void listPlayersHomes(GSPlayer player, int maxhomes) {
        if (player.getHomes().isEmpty()) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_HOMES);
            return;
        }

        PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SHOWING_YOUR_HOMES.replace("{player}", player.getName()));
        int currcount = 0;
        for (String server : player.getHomes().keySet()) {
            // Skip if the home list for this server is empty (shouldn't happen)
            if (player.getHomes().get(server).isEmpty()) {
                continue;
            }
    
            StringBuilder homes;
            if (server.equals(player.getServer())) {
                homes = new StringBuilder(ConfigManager.messages.HOMES_PREFIX_THIS_SERVER.replace("{server}", server));
            } else {
                homes = new StringBuilder(ConfigManager.messages.HOMES_PREFIX_OTHER_SERVER.replace("{server}", server));
            }

            for (Home h : player.getHomes().get(server)) {
                homes.append(h.name).append(", ");
                currcount++;
            }

            // V: Was handled earlier, but in case getHomes().isEmpty() didn't work - which it didn't when I tested..
            if (currcount == 0) {
                PlayerManager.sendMessageToTarget(player, ConfigManager.messages.NO_HOMES);
                return;
            }

            PlayerManager.sendMessageToTarget(player, homes.substring(0, homes.length() - 2));

        }

        // V: Added showing global current/max homes to player
        String showmax = ConfigManager.messages.MAXIMUM_HOMES;
        showmax += " " + (currcount) + " " + ConfigManager.messages.MAXIMUM_HOMES_OF + " " + (maxhomes) + " ";
        showmax += ConfigManager.messages.MAXIMUM_HOMES2;
        PlayerManager.sendMessageToTarget(player, showmax);
    }

    public static void listOtherPlayersHomes(GSPlayer sender, String playername) {
        GSPlayer player = DatabaseManager.players.loadPlayer(playername);

        if (player == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST.replace("{player}", playername));
            return;
        }

        loadPlayersHomes(player);

        if (player.getHomes().isEmpty()) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_OTHER_HOMES.replace("{player}", player.getName()));
            return;
        }

        int currcount = 0;
        PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.SHOWING_OTHER_HOMES.replace("{player}", player.getName()));
        for (String server : player.getHomes().keySet()) {
            // Skip if the home list for this server is empty (shouldn't happen)
            if (player.getHomes().get(server).isEmpty()) {
                continue;
            }
    
            StringBuilder homes;
            if (sender.getServer() != null && server.equals(sender.getServer())) {
                homes = new StringBuilder(ConfigManager.messages.HOMES_PREFIX_THIS_SERVER.replace("{server}", server));
            } else {
                homes = new StringBuilder(ConfigManager.messages.HOMES_PREFIX_OTHER_SERVER.replace("{server}", server));
            }

            for (Home h : player.getHomes().get(server)) {
                homes.append(h.name).append(", ");
                currcount++;
            }

            // V: Was handled earlier, but in case getHomes().isEmpty() didn't work - which it didn't when I tested..
            if (currcount == 0) {
                PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_OTHER_HOMES.replace("{player}", player.getName()));
                return;
            }

            PlayerManager.sendMessageToTarget(sender, homes.substring(0, homes.length() - 2));
        }

        String showCount = ConfigManager.messages.MAXIMUM_HOMES_OTHER.replace("{player}", player.getName());
        showCount += " " + currcount + " " + ConfigManager.messages.HOMES_SET;
        PlayerManager.sendMessageToTarget(sender, showCount);
    }

    public static void loadPlayersHomes(GSPlayer player) {
        List<Home> homes = DatabaseManager.homes.getHomesForPlayer(player);

        for (Home home : homes) {
            if (home.loc.getServer() == null) {
                geSuit.getInstance().getLogger().warn("Invalid server for home \"" + home.name + "\" of player " + player.getName() + "!");
                continue;
            }

            if (player.getHomes().get(home.loc.getServerName()) == null) {
                ArrayList<Home> list = new ArrayList<>();
                list.add(home);
                player.getHomes().put(home.loc.getServerName(), list);
            } else {
                player.getHomes().get(home.loc.getServerName()).add(home);
            }
        }
    }


    public static Home getHome(GSPlayer player, String home) {
        HashMap<String, Home> found = new HashMap<>();

        for (ArrayList<Home> list : player.getHomes().values()) {
            for (Home h : list) {
                if (h.name.toLowerCase().equals(home.toLowerCase())) {
                    found.put(h.loc.getServerName(), h);
                }
            }
        }

        if (found.size() == 0) {
            return null;
        } else {
            if (found.containsKey(player.getServer())) {
                return found.get(player.getServer());
            }

            return found.values().iterator().next();
        }
    }

    public static void sendPlayerToHome(GSPlayer player, String home) {
        Home h = getHome(player, home);
        if (h == null) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.HOME_DOES_NOT_EXIST.replace("{home}", home));
            return;
        }

        TeleportToLocation.execute(player, h.loc);

        PlayerManager.sendMessageToTarget(player, ConfigManager.messages.SENT_HOME.replace("{home}", home));
    }

    public static void sendPlayerToOtherHome(GSPlayer sender, String playername, String home) {
        GSPlayer player = DatabaseManager.players.loadPlayer(playername);

        if (player == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }

        loadPlayersHomes(player);
        Home h = getHome(player, home);
        if (h == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.HOME_DOES_NOT_EXIST.replace("{home}", home));
            return;
        }

        TeleportToLocation.execute(sender, h.loc);

        PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.SENT_HOME.replace("{home}", home));
    }

    public static void deleteHome(String player, String home) {
        GSPlayer p = PlayerManager.getPlayer(player);
        Home h = getHome(p, home);

        if (h == null) {
            PlayerManager.sendMessageToTarget(p, ConfigManager.messages.HOME_DOES_NOT_EXIST.replace("{home}", home));
            return;
        }

        for (ArrayList<Home> list : p.getHomes().values()) {
            if (list.contains(h)) {
                list.remove(h);
                break;
            }
        }

        DatabaseManager.homes.deleteHome(h);

        PlayerManager.sendMessageToTarget(p, ConfigManager.messages.HOME_DELETED.replace("{home}", home));
    }

    public static void deleteOtherHome(GSPlayer sender, String playername, String home) {
        GSPlayer player = DatabaseManager.players.loadPlayer(playername);
        if (player == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.PLAYER_DOES_NOT_EXIST);
            return;
        }
        loadPlayersHomes(player);
        Home h = getHome(player, home);
        if (h == null) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.HOME_DOES_NOT_EXIST.replace("{home}", home));
            return;
        }
        for (ArrayList<Home> list : player.getHomes().values()) {
            if (list.contains(h)) {
                list.remove(h);
                break;
            }
        }
        DatabaseManager.homes.deleteHome(h);
        PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.HOME_OTHER_DELETED.replace("{home}", home).replace("{player}", playername));
        if (geSuit.getInstance().getProxy().getPlayer(player.getName()).isPresent()) {
            PlayerManager.sendMessageToTarget(player, ConfigManager.messages.HOME_DELETED.replace("{home}", home));
        }
    }
}