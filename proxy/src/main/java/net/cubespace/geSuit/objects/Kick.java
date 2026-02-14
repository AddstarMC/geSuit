package net.cubespace.geSuit.objects;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by narimm on 8/11/2015.
 */
public class Kick {

    private String uuid;
    private final String name;
    private String bannedBy;
    private String reason;
    private long bannedOn;

    public Kick(String uuid, String name, String bannedBy, String reason, long bannedOn) {
        this.uuid = uuid;
        this.name = name;
        this.bannedBy = bannedBy;
        this.reason = reason;
        this.bannedOn = bannedOn;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        String dateTime = sdf.format(new Date(bannedOn));
        return "name: " + name + ", kickedBy: " + bannedBy + ", reason: " + reason + ", Time: " + dateTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public void setBannedBy(String bannedBy) {
        this.bannedBy = bannedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getBannedOn() {
        return bannedOn;
    }

    public void setBannedOn(long bannedOn) {
        this.bannedOn = bannedOn;
    }
}