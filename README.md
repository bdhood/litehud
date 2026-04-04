# LiteHud

LiteHud is a minimal, always-on-screen overlay for Fabric that gives you the stats you actually care about — without the bloat.

Features
- FPS — real-time frame rate
- TPS — server tick rate, averaged over the last 10 ticks
- Ping — your latency to the current server (multiplayer only)
- XYZ Coordinates — your exact position, updated every tick
- Facing — cardinal direction, yaw, and pitch
- Speed — movement speed in blocks per second

Clean & Customizable
- Uses a crisp Roboto Mono font for easy readability
- Fully configurable: toggle individual stats on/off, change text color, outline color, and background opacity
- Press H to open the settings screen in-game
- Press B to toggle the HUD on/off
- All settings are saved automatically to config/litehud.json

Lightweight by design. No dependencies beyond Fabric API. No performance impact.

## Build

```
./gradlew build
```

## Clean

```
rm build
rm run
```

## Run game (offline mode only)
```
./gradlew runClient
```
