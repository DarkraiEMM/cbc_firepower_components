# CBC: Firepower Components

Small Create Big Cannons addon focused on compact cannon mounts and cleaner ammo handling.

## What it adds

- **Compact Cannon Mount** - a one-block side-mounted mount for big cannons.
- **Compact Autocannon Mount** - the same idea, but tuned for autocannons.
- **Autocannon Ammo Feed** - feeds loose autocannon rounds into adjacent assembled compact mounts.
- **Cannon Magazine Loader** - a three-round external magazine for big cannon mounts.
- **Cannon Limiter** - a configurable item that installs pitch and yaw limits onto compact mounts.

The magazine loader takes projectile + powered cartridge pairs, waits until it has three complete rounds, then feeds the mounted cannon. Fuzes can be inserted after a fuzed projectile is stored; the loader writes the fuze onto the cached projectile before loading it. Spent empty big cartridges can be pulled from the sides, back, or bottom with normal item automation.

The Cannon Limiter stores pitch lower, pitch upper, left yaw, and right yaw settings on the item itself. Right-click air to configure the limiter, right-click a compact mount to install it, and sneak-right-click the mount to remove it and clear the limits.

## Versions

| Minecraft | Loader | Status |
| --- | --- | --- |
| 1.21.1 | NeoForge 21.1.x | NeoForge release line |
| 1.20.1 | Forge 47.x | Forge release line |

The 1.21.1 build expects Create 6.0.x, Create Big Cannons 5.11.6, and Ritchie's Projectile Library 2.1.2.

The 1.20.1 Forge build expects Create 6.0.8, Create Big Cannons 5.11.4, and Ritchie's Projectile Library 2.1.1.

## Download

Grab the latest jar from the GitHub Releases page.

For 1.21.1 NeoForge, use the file named like:

```text
cbc_firepower_components-neoforge-1.21.1-<version>+mc.1.21.1-neoforge.jar
```

## Building

Build the current NeoForge version:

```powershell
.\gradlew.bat :neoforge-1.21.1:build
```

The jar will be in:

```text
versions\neoforge-1.21.1\build\libs
```

The Forge 1.20.1 source is under `versions/forge-1.20.1` and can be built as its own release line:

```powershell
.\gradlew.bat :forge-1.20.1:build
```
