# CBC: Firepower Components

Compact mounts and automation-friendly ammunition feeders for Create Big Cannons.

## Release Target

The current release candidate is:

- Minecraft 1.21.1
- NeoForge 21.1.x
- Create 6.0.x
- Create Big Cannons 5.11.3
- Ritchie's Projectile Library 2.1.2

The `versions/forge-1.20.1` source set is kept as a legacy development target and is not part of the current public release candidate.

## Features

- Compact Cannon Mount: a one-block side-mounted CBC cannon mount.
- Compact Autocannon Mount: a compact mount intended for autocannons.
- Autocannon Ammo Feed: pushes loose autocannon ammo into adjacent assembled compact mounts.
- Cannon Magazine Loader: stores three projectile plus big-cartridge pairs, feeds adjacent assembled big cannon mounts, and outputs spent empty big cartridges from its side, back, and bottom automation faces.

## Cannon Magazine Loader Rules

- Manual insertion and removal are always allowed.
- Automation input accepts projectiles first, then a powered big cartridge in the matching column.
- Automation waits for all three ammunition pairs before starting a mechanical loading cycle.
- During a cycle, automation input is locked until all three pairs have been sent and spent empty cartridges have been removed.
- Side, back, and bottom item handlers expose spent empty big cartridges as a single output slot for hoppers, chutes, and funnels.
- The front display face and top face are deposit-only virtual ports, so automation cannot pull unfired ammunition out of the loader.

## Build

```powershell
.\gradlew.bat :neoforge-1.21.1:build
```

Output jars are written under:

```powershell
versions\neoforge-1.21.1\build\libs
```

## Release Checklist

- Build the NeoForge 1.21.1 target.
- Test in a clean client with only required dependencies.
- Test on a dedicated server.
- Verify three-round magazine loading, firing, spent-cartridge ejection, and bottom extraction.
- Verify save/reload with partially filled and locked loaders.
- Prepare screenshots that show the compact mounts, ammo feed, and magazine loader in use.
