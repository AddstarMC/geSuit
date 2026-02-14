package net.cubespace.geSuit.events;

import net.cubespace.geSuit.Utilities;

import java.util.UUID;

public class WarnPlayerEvent {

    private final String player;
    private final UUID id;
    private final String by;
    private final String reason;
    private final ActionType action;
    private final String actionExtra;
    private final int warnCount;
    
    public WarnPlayerEvent(String player, String uuid, String by, String reason, ActionType type, String actionExtra, int count) {
        this.player = player;
        this.id = Utilities.makeUUID(uuid);
        this.by = by;
        this.reason = reason;
        this.action = type;
        this.actionExtra = actionExtra;
        this.warnCount = count;
    }
    
    public String getPlayerName() {
        return player;
    }
    
    public UUID getPlayerId() {
        return id;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getBy() {
        return by;
    }
    
    public ActionType getAction() {
        return action;
    }
    
    public String getActionExtra() {
        return actionExtra;
    }
    
    public int getWarnCount() {
        return warnCount;
    }
    
    public enum ActionType {
        None,
        Kick,
        TempBan,
        Ban
    }
}
