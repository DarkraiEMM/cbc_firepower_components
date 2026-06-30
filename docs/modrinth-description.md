# CBC: Firepower Components

A small Create Big Cannons addon for people who want tighter cannon setups and less awkward ammo routing.

## Blocks

- **Compact Cannon Mount** - a one-block side-mounted big cannon mount.
- **Compact Autocannon Mount** - a compact mount for autocannons.
- **Autocannon Ammo Feed** - moves loose autocannon rounds into nearby assembled compact mounts.
- **Large Autocannon Ammo Box** - stores 64 main rounds and 64 matching tracer rounds for CBC autocannon or machine gun ammunition.
- **Cannon Magazine Loader** - holds three projectile + powered cartridge pairs and feeds an assembled big cannon mount.
- **Cannon Limiter** - a configurable setup item that installs pitch and yaw limits onto compact mounts.

The magazine loader is meant to behave like a simple external magazine. Automation fills projectile slots first, applies fuzes to stored fuzed projectiles when provided, then fills the matching powered cartridges. Once all three rounds are ready, it starts feeding the cannon. Spent empty big cartridges can be extracted from the sides, back, or bottom.

The Cannon Limiter opens a small configuration screen with pitch lower, pitch upper, left yaw, and right yaw controls. Right-click a compact mount to install the configured limiter; sneak-right-click the mount to remove it and clear the limits. Saved yaw limits are relative to the mount's neutral direction, and an installed limiter is rendered on the mount so it is easy to see.

## Addon compatibility

Compatibility with other Create Big Cannons addons has several levels: assembly, rotation, firing, and automatic loading. The current compatibility work focuses on making compact mounts assemble, rotate, fire, and avoid crashes with more addon cannon types. Automatic loading still depends on the cannon's own item-input logic.

NeoForge 1.21.1 includes compact mount data for selected CBC: Advanced Technologies, CBC Military Supplement, and CBC: Neo Warfare-style cannon types. Rocket pods, torpedo tubes, ready racks, and other special weapons may need dedicated loaders later; the Cannon Magazine Loader is still meant for the CBC big-cannon projectile + powered cartridge flow.

Heavy autocannon ammunition, twin/heavy weapon families, specialized ammo boxes, and standalone ready racks are planned ideas, not current-version features.

## Builds

The project has two release lines:

- Minecraft 1.21.1 / NeoForge 21.1.x
- Minecraft 1.20.1 / Forge 47.x

The 1.21.1 NeoForge build expects Create 6.0.x, Create Big Cannons 5.11.6, and Ritchie's Projectile Library 2.1.2.

The 1.20.1 Forge build expects Create 6.0.8, Create Big Cannons 5.11.4, and Ritchie's Projectile Library 2.1.1.
