# geSuit Proxy (Velocity)

This document describes the geSuit proxy module for Velocity API 3.4.0.

## Overview

- **Directory**: `proxy/` (parent POM references it).
- **POM**: Uses `com.velocitypowered:velocity-api:3.4.0`. PaperMC repo added for Velocity API.
- **Main plugin**: `geSuit` is a Velocity `@Plugin` with `@Inject` constructor; `ProxyInitializeEvent` registers channels, listeners, and commands. Channel names (`gesuit:*` and legacy `geSuit*`) are unchanged for Bukkit compatibility.
- **Plugin messages**: `SendPluginMessage` uses `RegisteredServer.sendPluginMessage(ChannelIdentifier, byte[])`. Plugin message classes use `Optional<RegisteredServer>` where needed.
- **Channel registration**: Uses `ChannelRegistrar.register(MinecraftChannelIdentifier)` and legacy `LegacyChannelIdentifier` when enabled.
- **Message listeners**: Base `MessageListener` uses Velocity `PluginMessageEvent`, `event.getSource() instanceof ServerConnection`, `event.getIdentifier().getId()`, `event.getData()`, and `event.setResult(ForwardResult.handled())`.
- **Objects**: `Location` stores server name (String) and exposes `getServer()` as `Optional<RegisteredServer>`. `GSPlayer` uses Velocity `Player` and `connectTo(RegisteredServer)`. `Portal` uses `getServerName()` and `getServer()` returning `Optional<RegisteredServer>`.
- **Utilities / LoggingManager**: Use Velocity/Adventure; staff channel integration in `Utilities.sendOnChatChannel()` is implemented via the ChatControlMirror API when `ChatControlIntegration` is enabled in config.
- **Config/data path**: `getDataFolder()` returns `dataDirectory.toFile()` for compatibility.

## Event / API reference

| Concept              | Velocity                          |
|----------------------|-----------------------------------|
| Plugin               | @Plugin + @Inject                  |
| ProxyServer          | geSuit.getInstance().getProxy()   |
| Server               | RegisteredServer (proxy.server)   |
| Player               | Player                            |
| Command source       | CommandSource                     |
| PluginMessageEvent   | getSource() instanceof ServerConnection |
| registerChannel      | ChannelRegistrar.register(ChannelIdentifier) |
| send plugin message  | server.sendPluginMessage(identifier, data) |
| LoginEvent (async)   | LoginEvent + EventTask.withContinuation |
| PostLoginEvent       | PostLoginEvent                    |
| ServerConnectedEvent | ServerConnectedEvent              |
| DisconnectEvent      | DisconnectEvent                   |
| ServerPreConnectEvent| ServerPreConnectEvent             |
| Console              | getConsoleCommandSource()         |
| Legacy text           | LegacyComponentSerializer.legacySection().deserialize() |

## Plugin message channels

- `gesuit:teleport`, `gesuit:spawns`, `gesuit:bans`, `gesuit:portals`, `gesuit:warps`, `gesuit:homes`, `gesuit:api`, `gesuit:admin`
- Legacy (if enabled): `geSuitTeleport`, `geSuitSpawns`, etc. (and lowercase variants).

Bukkit modules use the same channel names; the proxy runs on Velocity.
