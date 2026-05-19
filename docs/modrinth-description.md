# CBC: Firepower Components

CBC: Firepower Components adds compact mounts and ammunition automation blocks for Create Big Cannons.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Create 6.0.x
- Create Big Cannons 5.11.3
- Ritchie's Projectile Library 2.1.2

## Blocks

- Compact Cannon Mount: one-block side-mounted big cannon mount.
- Compact Autocannon Mount: compact autocannon-focused mount.
- Autocannon Ammo Feed: feeds loose autocannon ammunition into adjacent assembled compact mounts.
- Cannon Magazine Loader: stores three projectile plus powered big-cartridge pairs, feeds adjacent assembled big cannon mounts, and outputs spent empty big cartridges from the sides, back, and bottom.

## Cannon Magazine Loader Automation

The loader is designed as a three-round external magazine. Mechanical input fills projectile slots first, then powered big cartridges in the matching columns. Once automation starts a cycle, new mechanical input is locked until all three ammunition pairs have been sent and spent cartridges have been removed. Manual insertion and removal remain available for testing and emergency changes.

The side, back, and bottom faces expose spent empty big cartridges as a single output slot for hoppers, chutes, and funnels. The front display face and top face are deposit-only virtual input ports, so automation cannot pull unfired ammunition out of the loader.

## Notes

This release targets NeoForge 1.21.1. Older Forge source files may exist in the repository but are not part of this release package.
