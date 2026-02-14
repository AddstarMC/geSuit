geSuit - Velocity proxy plugin
===
Proxy plugin for Velocity. Communicates with geSuit Bukkit modules via plugin messages.

See [MIGRATION.md](MIGRATION.md) for technical details of the proxy implementation.

Building
---
From the repo root, run:

    mvn package -f proxy/pom.xml

Or from the parent: `mvn package -f geSuit-parent/pom.xml`

The jar will be in `proxy/target/`. Use JDK 21 to build.

Installing
---
Requires [Yamler](https://www.spigotmc.org/resources/yamler.315/) (or the project's Yamler-Core) and a MySQL server.

* Place geSuit.jar (and Yamler) inside your Velocity _plugins/_ folder and restart Velocity.
* Fill in your MySQL server's information in config.yml (inside the _geSuit/_ folder)
* Configure anything else you want in the files in the _geSuit/_ folder
* Give the players permission to use the commands
* Done!

Additional features:
---

The following Bukkit / Spigot plugins are optional, and require the base geSuit to function.

* [Homes](https://github.com/AddstarMC/geSuitHomes)
* [Bans](https://github.com/AddstarMC/geSuitBans)
* [Teleportation](https://github.com/AddstarMC/geSuitTeleport)
* [Spawn](https://github.com/AddstarMC/geSuitSpawn)
* [Portals](https://github.com/AddstarMC/geSuitPortals)
* [Warps](https://github.com/AddstarMC/geSuitWarps)
* [Admin](https://github.com/AddstarMC/geSuitAdmin)

Additonal geSuit supports the use of 

Integration Tests:
---
Requirements:
- Mysql server - with a user:root password:password and a db:gesuit.