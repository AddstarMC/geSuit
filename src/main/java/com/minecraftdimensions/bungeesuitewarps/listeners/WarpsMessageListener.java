package com.minecraftdimensions.bungeesuitewarps.listeners;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.minecraftdimensions.bungeesuitewarps.managers.WarpsManager;

public class WarpsMessageListener implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player player,
			byte[] message) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));
		String task = null;

		try {
			task = in.readUTF();

			if (task.equals("TeleportPlayerToLocation")) {
				WarpsManager
						.teleportPlayerToWarp(
								in.readUTF(),
								new Location(Bukkit.getWorld(in.readUTF()), in
										.readDouble(), in.readDouble(), in
										.readDouble(), in.readFloat(), in
										.readFloat()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
