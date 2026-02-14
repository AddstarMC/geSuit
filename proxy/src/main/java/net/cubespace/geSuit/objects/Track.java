package net.cubespace.geSuit.objects;

import java.sql.Timestamp;
import java.util.Date;

public class Track {
    private String player;
    private String uuid;
    private String ip;
    private Date firstseen;
    private Date lastseen;
    private boolean nameBanned;
    private boolean ipBanned;
    private String banType;

    public Track(String player, String uuid, String ip, Timestamp firstseen, Timestamp lastseen, String banType, String bannedName, String bannedId, String bannedIp) {
        this.player = player;
        this.uuid = uuid;
        this.ip = ip;
        this.firstseen = firstseen;
        this.lastseen = lastseen;
        
        if ("ipban".equals(banType)) {
            // Could just be a name match
            if (ip.equals(bannedIp)) {
                this.ipBanned = true;
            }
            // If the name matches, treat it as a normal ban too 
            if (uuid.equals(bannedId) || player.equals(bannedName)) {
                this.banType = "ban";
                this.nameBanned = true;
            }
        } else if (banType != null) {
            this.nameBanned = true;
            this.banType = banType;
        }
    }

    public String getPlayer() {
        return player;
    }

    public Date getFirstSeen() {
        return firstseen;
    }

    public Date getLastSeen() {
        return lastseen;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getIp()
    {
        return ip;
    }
    
    public boolean isIpBanned() {
        return ipBanned;
    }
    
    public boolean isNameBanned() {
        return nameBanned;
    }
    
    public String getBanType() {
        return banType;
    }
    
    @Override
    public String toString() {
        return String.format("n: %s id: %s ip: %s date: %s", player, uuid, ip, lastseen.toString());
    }
}