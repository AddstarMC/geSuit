package net.cubespace.geSuit.configs;

import net.cubespace.Yamler.Config.ConfigMode;

@SuppressWarnings("CanBeFinal")
public class Messages extends BaseConfig {


    public Messages() {
        super("messages");
        CONFIG_MODE = ConfigMode.FIELD_IS_KEY;
    }

    public static final String PROXY_COMMAND_NEWSPAWN_USAGE = "&cUsage: !newSpawn <player>";
    public String PROXY_COMMAND_SEEN_USAGE = "&cUsage: /seen <player>";
    public String PLAYER_CONNECT_PROXY = "{player}&e has joined the server!";
    public String PLAYER_DISCONNECT_PROXY = "{player}&e has left the server!";
    public String PLAYER_DOES_NOT_EXIST = "&c" + "That player does not exist";
    public String PLAYER_LOAD = "Loaded player &9{player}&7 ({uuid})";
    public String PLAYER_LOAD_CACHED = "Loaded player from cache &9{player}&7 ({uuid})";
    public String PLAYER_CREATE = "Created player &b{player}&7 ({uuid})";
    public String PLAYER_UNLOAD = "Unloaded player &c{player}";
    public String PLAYER_NOT_ONLINE = "&c" + "That player is not online";
    public String NO_PERMISSION = "&c" + "You do not have permission to use that command";
    public String NEW_PLAYER_BROADCAST = "&eNotice to everyone: &b{player} &ahas just joined this server for the first time. Please make them feel welcome!";
    public String PLAYER_BANNED_ALT_JOIN = "&c{player} same IP as banned player {alt}";
    public String PLAYER_ALT_JOIN = "&b{player} same IP ({ip}) as {alt}";
    public String PLAYER_GEOIP = "&8{player} is from {location}";
    public String PLAYER_NOGEOIP = "&8{player}'s location could not be determined";
    public String GEOIP_REPORT = "&8{ip} : {location}";

    public String PLAYER_JOIN_NAMECHANGE = "&e{player} ({old}) joined the game.";
    public String PLAYER_JOIN_NAMECHANGE_PROXY = "&ePlayer {player} was previously known as {old}.";

    // teleport specific messages
    public String ALL_PLAYERS_TELEPORTED = "&6" + "All players have been teleported to &c{player}";
    public String TELEPORTED_TO_PLAYER = "&6" + "You have been teleported to &c{player}";
    public String PLAYER_TELEPORT_PENDING = "&c" + "You already have a teleport pending";
    public String PLAYER_TELEPORT_PENDING_OTHER = "&c" + "That player already has a teleport pending";
    public String PLAYER_TELEPORT_WRONG_SERVER = "&c" + "You are not allowed to teleport to that player from here.";
    public String PLAYER_TELEPORTED_TO_YOU = "&c" + "{player} &6has teleported to you";
    public String PLAYER_TELEPORTED = "&c" + "{player} &6has teleported to &c{target}";
    public String PLAYER_REQUESTS_TO_TELEPORT_TO_YOU = "&6" + "{player} has requested to teleport to you.{N}&6To teleport, type &c/tpaccept&6.{N}&6To deny this request, type &c/tpdeny&6.{N}&6This request will timeout after &c20 seconds&6.";
    public String PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM = "&6" + "{player} has requested that you teleport to them.{N}&6To teleport, type &c/tpaccept&6.{N}&6To deny this request, type &c/tpdeny&6.{N}&6This request will timeout after &c20 seconds&6.";
    public String TELEPORT_ACCEPTED = "&6" + "You accepted {player}'s teleport request";
    public String TELEPORT_REQUEST_ACCEPTED = "&6" + "{player} accepted your teleport request";
    public String TELEPORT_DENIED = "&6" + "You denied &c{player}'s &6teleport request";
    public String TELEPORT_REQUEST_DENIED = "&c" + "{player} &6denied your teleport request";
    public String NO_TELEPORTS = "&c" + "You do not have any pending teleports";
    public String TELEPORT_REQUEST_SENT = "&6" + "Request sent to &c{player}";
    public String TPA_REQUEST_TIMED_OUT = "&6" + "Your request to teleport to &c{player} &6has timed out";
    public String TP_REQUEST_OTHER_TIMED_OUT = "&c" + "{player}'s teleport request has timed out";
    public String TPAHERE_REQUEST_TIMED_OUT = "&c" + "Your request to have {player} teleport to you has timed out";
    public String NO_BACK_TP = "&c" + "You do not have anywhere to go back to";
    public String SENT_BACK = "&6" + "You have been sent back";
    public String TELEPORT_TOGGLE_ON = "&6" + "Teleports have been toggled on";
    public String TELEPORT_TOGGLE_OFF = "&c" + "Teleports have been toggled off";
    public String TELEPORT_UNABLE = "&6" + "You are unable to teleport to &c{player}";

    // warp specific messages
    public String WARP_CREATED = "&6" + "Successfully created warp &c{warp}";
    public String WARP_UPDATED = "&6" + "Successfully updated warp &c{warp}";
    public String WARP_DESCRIPTION_UPDATED = "&6" + "Successfully updated the warp description";
    public String WARP_DELETED = "&6" + "Successfully deleted warp &c{warp}";
    public String PLAYER_WARPED = "&6" + "Sending you to &c{warp}";
    public String PLAYER_WARPED_OTHER = "&c" + "{player} &6has been sent to &c{warp}";
    public String WARP_DOES_NOT_EXIST = "&c" + "Error: &4The warp &c{warp} &4does not exist";
    public String WARP_NO_PERMISSION = "&c" + "You do not have permission to use that warp";
    public String WARP_SERVER = "&c" + "Warp not on the same server";

    // portal specific messages
    public String PORTAL_NO_PERMISSION = "&c" + "You do not have permission to enter this portal";
    public String PORTAL_CREATED = "&6" + "You have successfully created a portal";
    public String PORTAL_UPDATED = "&6" + "You have successfully updated the portal";
    public String PORTAL_DELETED = "&c" + "Portal deleted";
    public String PORTAL_FILLTYPE = "&c" + "That filltype does not exist";
    public String PORTAL_DESTINATION_NOT_EXIST = "&c" + "That portal destination does not exist";
    public String PORTAL_DOES_NOT_EXIST = "&c" + "That portal does not exist";
    public String INVALID_PORTAL_TYPE = "&c" + "That is an invalid portal type. Use warp or server";
    public String NO_SELECTION_MADE = "&c" + "No world edit selection has been made";

    // Spawn messages
    public String SPAWN_DOES_NOT_EXIST = "&c" + "The spawn point has not been set yet";
    public String SPAWN_UPDATED = "&6" + "Spawn point updated";
    public String SPAWN_SET = "&6" + "Spawn point set";
    public String SPAWN_DELETED = "&6" + "Spawn point deleted";

    // ban messages
    public String PROXY_COMMAND_BAN_USAGE = "&c" + "Usage: !ban <player|ip> <reason>";
    public String PROXY_COMMAND_WARN_USAGE = "&c" + "Usage: !warn <player> <reason>";
    public String PROXY_COMMAND_WHERE_USAGE = "&c" + "Usage: !where [options] <player|uuid|ip>";
    public String PROXY_COMMAND_TEMPBAN_USAGE = "&c" + "Usage: !tempban <player> <time> <reason>";
    public String PROXY_COMMAND_UNBAN_USAGE = "&c" + "Usage: !unban <player|uuid|ip>";
    public String PROXY_COMMAND_WARNHISTORY_USAGE = "&c" + "Usage: !warnhistory <player|ip>";
    public String PROXY_COMMAND_KICKHISTORY_USAGE = "&c" + "Usage: !kickhistory <player>";
    public String PROXY_COMMAND_KICKHISTORY_DISABLED = "&c" + "Kick History is globally disabled";
    public String PROXY_COMMAND_NAMES_USAGE = "&c" + "Usage: !names <player|uuid>";
    public String PROXY_COMMAND_NAMEHISTORYUPDATE_USAGE = "&c" + "Usage: !UpdateNameHistory <player|uuid>";
    public String PROXY_COMMAND_BATCHNAMEHISTORYUPDATE_USAGE = "&c" + "Usage !ForceBatchNameUpdate <all|<startUUID> " +
            "<endUUID>";
    public String WARN_REASON_REQUIRED = "&c" + "You must provide a reason for the warning";
    public String BAN_REASON_REQUIRED = "&c" + "You must provide a reason for the ban";
    public String TEMP_BAN_REASON_REQUIRED = "&c" + "You must provide a reason for the temp ban";
    public String UNKNOWN_PLAYER_STILL_BANNING = "&c" + "Player is unknown. Banning by name and &nmaybe&r&c by UUID";
    public String UNKNOWN_PLAYER_NOT_WARNING = "&c" + "ERROR: Player is unknown, refusing to give warning";
    public String KICK_PLAYER_MESSAGE = "&c" + "You have been kicked.{N}{N}&bReason: &e{message}";
    public String KICK_PLAYER_BROADCAST = "&b" + "{player} has been kicked. Reason: {message}";
    public String KICK_PLAYER_AUTO_BROADCAST = "&b" + "{player} has been auto-kicked";
    public String PLAYER_ALREADY_BANNED = "&c" + "That player is already banned!";
    public String PLAYER_NOT_BANNED = "&a" + "That player is not banned";
    public String PLAYER_NEVER_BANNED = "&6" + "No ban history for &c{player}";
    public String PLAYER_NEVER_WARNED = "&6" + "No warning history for &c{player}";
    public String PLAYER_NEVER_WARNED_OR_BANNED = "&6" + "No warning or ban history for &c{player}";
    public String PLAYER_NEVER_KICKED = "&6" + "No kick history for &c{player}";
    public String IPBAN_PLAYER = "&4" + "Your IP has been banned.{N}{N}&cReason: &e{message}";
    public String IPBAN_PLAYER_BROADCAST = "&c" + "{player} &bhas been ip banned. Reason: {message}";
    public String DEFAULT_BAN_REASON = "Unknown";
    public String DEFAULT_KICK_MESSAGE = "&cUnknown";
    // Deprecated:
    public String DEFAULT_WARN_REASON = "Unknown";
    public String BAN_PLAYER_MESSAGE = "&c" + "You have been banned.{N}{N}&cReason: &e{message}";
    public String BAN_PLAYER_BROADCAST = "&b" + "{player} has been banned. Reason: {message}";
    public String BAN_PLAYER_AUTO_BROADCAST = "&b" + "{player} has been auto-banned.";
    public String BAN_FAILED_UNKNOWN_REASON = "&c" + "The ban has failed for an unknown reason";
    public String TEMP_BAN_MESSAGE = "&c" + "You have been temporarily banned.{N}{N}&cReason: &e{message}{N}{N}&cRemaining ban time: &e{shortleft}";
    public String TEMP_BAN_BROADCAST = "&b" + "{player} has been temporarily banned ({shortleft}). Reason: {message}";
    public String TEMP_BAN_AUTO_BROADCAST = "&b" + "{player} has been auto temp-banned ({shortleft}).";
    public String PLAYER_UNBANNED = "&c" + "{player} has been unbanned!";
    public String WARN_PLAYER_BROADCAST = "&b" + "{player} has received a warning. Reason: {message}";

    // Home messages
    public String SENT_HOME = "&6" + "You have been sent home";
    public String NO_HOMES_ALLOWED_SERVER = "&c" + "You are not allowed to set any more homes on this server.";
    public String NO_HOMES_ALLOWED_GLOBAL = "&c" + "You are not allowed to set any more homes globally.";
    public String NO_HOMES = "&c" + "You do not have any homes set.";
    public String NO_OTHER_HOMES = "&c" + "{player} does not have any homes set.";
    public String HOME_EXISTS_OTHER_SERVER = "&c" + "Home already exists on another server. Please delete it first.";
    public String SHOWING_YOUR_HOMES = "&eListing your homes:";
    public String SHOWING_OTHER_HOMES = "&eListing homes of {player}:";
    public String HOMES_PREFIX_THIS_SERVER = "&a{server}: &9";
    public String HOMES_PREFIX_OTHER_SERVER = "&e{server}: &9";
    public String MAXIMUM_HOMES = "&eGlobally, you have";
    public String MAXIMUM_HOMES_OTHER = "&eGlobally, {player} has";
    public String MAXIMUM_HOMES_OF = "&eof";
    public String MAXIMUM_HOMES2 = "&emaximum homes set.";
    public String HOMES_SET = "&ehomes set";

    public String HOME_UPDATED = "&6" + "Your home \"{home}\" has been updated";
    public String HOME_SET = "&6" + "Your home \"{home}\" has been set";
    public String HOME_DOES_NOT_EXIST = "&c" + "That home does not exist";
    public String HOME_DELETED = "&c" + "Your home \"{home}\" has been deleted";
    public String HOME_OTHER_DELETED = "&c" + "{player}'s home \"{home}\" has been deleted";
    // Seen messages
    public String PLAYER_SEEN_ONLINE = "&6Player &c{player} &6is &aonline&6 since {timediff}{N}&6 {date}";
    public String PLAYER_SEEN_OFFLINE = "&6Player &c{player} &6is &4offline&6 since {timediff}{N}&6 {date}";
    public String PLAYER_SEEN_ITEM_FORMAT = "&6 - {name}: &r{value}";

    // OnTime messages
    public String PROXY_COMMAND_ONTIME_USAGE = "&c" + "Usage: !ontime <player>";
    public String ONTIME_FIRST_JOINED = "&6First joined:&f {date} ({days} days)";
    public String ONTIME_TIME_SESSION = "&6This login:&f {diff}";
    public String ONTIME_TIME_TODAY = "&6Today:&f {diff}";
    public String ONTIME_TIME_WEEK = "&6This week:&f {diff}";
    public String ONTIME_TIME_MONTH = "&6This month:&f {diff}";
    public String ONTIME_TIME_YEAR = "&6This year:&f {diff}";
    public String ONTIME_TIME_TOTAL = "&6Total ontime:&f {diff}";
    public String ONTIME_TIME_TOP = "&e{num}: &b{time}&a {player}";

    //LastLogin messages
    public String PROXY_COMMAND_LASTLOGINS_USAGE = "&c" + "Usage: !lastlogins <player> <num>";
    public String LASTLOGINS_FORMAT = "&6 Date:&f{date} &6Ontime:&f{ontime}";

    //Lockdown messages
    public String LOCKDOWN_MESSAGE = "&c" + "Server is in maintenance. {message}";
    public String LOCKDOWN_USAGE = "&c" + "Usage: !lockdown end|status|<time> <msg>  (time format like 1h5m10s)";

    //Client Version Message
    public String VERSION_TOO_LOW = "&c" + "Your client version is too low for this server please use atleast: {version}";
}
