package net.cubespace.geSuit.events;

import com.google.common.base.Strings;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.events.BanPlayerEvent.BanType;
import net.cubespace.geSuit.objects.Ban;
import java.util.UUID;

public class UnbanPlayerEvent {
    private Ban ban;
    private String by;
    
    public UnbanPlayerEvent(Ban ban, String by) {
        this.ban = ban;
        this.by = by;
    }
    
    public String getPlayerName() {
        return ban.getPlayer();
    }
    
    public UUID getPlayerId() {
        if (Strings.isNullOrEmpty(ban.getUuid()))
            return null;
        return Utilities.makeUUID(ban.getUuid());
    }
    
    public String getReason() {
        return ban.getReason();
    }
    
    public BanType getType() {
        switch (ban.getType())
        {
        case "tempban":
            return BanType.Temporary;
        case "ipban":
            return BanType.IP;
        default:
            return BanType.Name;
        }
    }
    
    public String getBannedBy() {
        return ban.getBannedBy();
    }
    
    public String getUnbannedBy() {
        return by;
    }
}
