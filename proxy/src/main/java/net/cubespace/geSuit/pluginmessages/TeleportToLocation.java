package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.LoggingManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TeleportToLocation {

    public static void execute(GSPlayer player, Location location) {
        if (location.getServer().isEmpty()) {
            geSuit.getInstance().getLogger().error("Location has no Server, this should never happen. Please check");
            new Exception("").printStackTrace();
            return;
        }
        if (player == null) {
            LoggingManager.log(LegacyComponentSerializer.legacySection().serialize(Component.text("Warning! Teleport called but player is null!")));
            new Exception("").printStackTrace();
            return;
        }
        var server = location.getServer().get();
        if (player.getServer() == null || !player.getServer().equals(location.getServerName())) {
            player.connectTo(server);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        try {
            out.writeUTF("TeleportToLocation");
            out.writeUTF(player.getName());
            out.writeUTF(location.getWorld());
            out.writeDouble(location.getX());
            out.writeDouble(location.getY());
            out.writeDouble(location.getZ());
            out.writeFloat(location.getYaw());
            out.writeFloat(location.getPitch());
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.getInstance().getProxy().getScheduler()
            .buildTask(geSuit.getInstance(), new SendPluginMessage(geSuit.CHANNEL_NAMES.TELEPORT_CHANNEL, server, bytes))
            .schedule();
    }
}
