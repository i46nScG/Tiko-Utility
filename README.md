# Tiko Utility

Tiko Utility is a server-side utility mod for Minecraft built with Fabric. It adds a small set of commands for player convenience and basic server moderation.

## Features

| Command          | Description                                                                                       | Access                         |
| ---------------- | ------------------------------------------------------------------------------------------------- | ------------------------------ |
| `/afk`           | Toggles your AFK status and an `[AFK]` scoreboard prefix. Moving automatically clears the status. | All players                    |
| `/trash`         | Opens a 27-slot trash inventory. Its contents are cleared when the menu is closed.                | All players                    |
| `/ping <player>` | Sends a title notification to another player. You cannot ping yourself.                           | All players                    |
| `/heal`          | Restores your health to its maximum.                                                              | Gamemaster permission required |
| `/feed`          | Restores your hunger and saturation.                                                              | Gamemaster permission required |

## Requirements

- Minecraft **26.3**
- Java **25** or later
- Fabric Loader **0.19.5** or later
- Fabric API **0.162.0+26.3** or a compatible version for your Minecraft and Loader versions

This mod runs on the server; players do not need to install it on their clients to use the server commands.

## Install

1. Install a Fabric server for Minecraft 26.3 with Java 25 or later.
2. Add the matching Fabric API jar to the server's `mods` directory.
3. Add the Tiko Utility jar to the same `mods` directory.
4. Start or restart the server.

Download or build the mod jar, then place it in the server's `mods` folder. Keep the Fabric Loader, Fabric API, and Minecraft versions compatible.

## Build from source

Clone or download this project, ensure Java 25 is available, and run the Gradle wrapper from the project directory:

```bash
./gradlew build
```

On Windows, use:

```bat
gradlew.bat build
```

The built mod jar is written to `build/libs/` (for example, `tiko-utility-1.1.0.jar`).

To launch a local Fabric development server:

```bash
./gradlew runServer
```

## Project structure

- `src/main/java/com/tokioshy/TikoUtility.java` — mod initialization and command/event logic
- `src/main/resources/fabric.mod.json` — Fabric mod metadata and dependency requirements
- `src/main/resources/assets/tiko-utility/` — mod assets

## License

This project is released under [CC0 1.0 Universal](LICENSE).
