# Changelog

## 0.3.5

- Added the Vertical Compact Cannon Mount for cannons assembled above or below the mount.
- Added the Ready Ammunition Compartment, an ordered ammunition container that supplies complete projectile-and-charge sets directly to cannon mounts and supports Mechanical Arm input and output.
- Added the 24-station Carousel Ready Rack with rotational indexing, configurable ammunition order, adaptive outlet direction, and Mechanical Arm interaction.
- Added the Spent Casing Collector with area collection, visible empty/partial/full fill states, and automated casing extraction.
- Added the Automatic Cannon Controller with single, three-round burst, and continuous fire; polling and salvo dispatch; rising-edge redstone activation; ammunition selection; and signal-strength fire-rate control.
- Added optional Drive By Wire remote control without replacing normal local redstone control.
- Added spyglass rangefinding that reports straight-line distance and resolves physical-space coordinates for supported Sable/Aeronautics environments.
- Added bilingual Ponder tutorials for mounts, ammunition equipment, fire control, cannon limiters, and rangefinding.
- Expanded ammunition and mount compatibility for CBC Military Supplement, CBC Neo/Modern Warfare, CBC Advanced Technologies, and CBC Enhanced Shells integrations.
- Updated the NeoForge target to Create Big Cannons 5.11.7 and retained the supported Create 6.0.x line.
- Refined large autocannon models and textures, separated twin muzzle brakes, reduced breech thickness, and gave twin parts independent oriented selection and collision shapes.

## 0.3.1

- Added the Large Autocannon Ammo Box with CBC-style ammo container behavior.
- Set the large ammo box capacity to 64 main rounds plus 64 matching tracer rounds for autocannon or machine gun ammunition.
- Added a NeoForge 1.21.1 compatibility bridge for the Create Big Cannons 5.11.6 recoil API change.
- Prevented older CBC addon cannon contraptions from crashing when they call the old two-argument recoil hook.
- Added compact cannon mount pitch data for CBC: Neo Warfare, CBC Military Supplement, and CBC: Advanced Technologies cannon types.
- Added defensive compact mount checks around redstone updates, contraption direction lookup, and cannon stress calculation.
- This should help reports involving CBC: Advanced Technologies, CBC: Neo Warfare, and CBC Military Supplement cannons on CBC 5.11.6.
- Kept the Forge 1.20.1 target on the matching release version.

## 0.2.0

- Updated the NeoForge 1.21.1 target to Create Big Cannons 5.11.6 for the newer Aeronautics/Simulated compatibility line.
- Updated the Forge 1.20.1 target to Create 6.0.8, Create Big Cannons 5.11.4, and Ritchie's Projectile Library 2.1.1.
- Added fuze support to the Cannon Magazine Loader so fuzes can be applied to stored fuzed projectiles before loading.
- Prevented the Cannon Magazine Loader from loading fuzed projectiles before a fuze is attached.
- Updated README, Modrinth description, and usage guides for the fuze loading workflow and CBC 5.11.6 dependency.
- Kept the compact cannon mount reassembly compatibility fix for newer Create Big Cannons versions.
- Kept both release targets on the same mod version for the matching release package.

## 0.1.8

- Added the Cannon Limiter tool for compact cannon mounts and compact autocannon mounts.
- Added a Cannon Limiter configuration screen with enable toggles, sliders, and numeric angle fields.
- Changed the limiter into an installable template item: configure it first, then right-click a compact mount to install it.
- Added mount-side limiter storage so the installed limiter and pitch/yaw limits persist after save/reload.
- Added sneak-right-click removal for installed limiters, which also clears the mount limits.
- Added an installed limiter visual marker rendered on compact mounts.
- Clamped compact mount mechanical rotation against saved pitch and yaw limits.
- Added goggle tooltip information for the current limiter state.
- Added survival crafting recipes, item model, creative tab entry, and Chinese/English localization for the Cannon Limiter.
- Fixed a compact cannon mount crash when Sable/Simulated swivel bearings disassemble sublevels.
- Added delayed reassembly handling for newer Create Big Cannons versions.
- Kept compatibility with the 5.11.x Create Big Cannons release line.

## 0.1.7

- Added survival crafting recipes for the compact mounts, ammo feed, and magazine loader.
- Added hopper and andesite funnel recipes for the Autocannon Ammo Feed.
- Fixed NeoForge 1.21.1 recipe loading by moving recipe data to the 1.21 `recipe` directory.
- Updated NeoForge iron plate recipe tags to the Create 6 `c:plates/iron` tag.
- Added Chinese and English usage guides with release images.

## 0.1.6

- Reworked the Cannon Magazine Loader automation interface into virtual ports.
- Added side, back, and bottom spent big-cartridge output for hoppers, chutes, and funnels.
- Changed mechanical loading so automated input fills all three ammunition pairs before a cycle starts.
- Prevented side and top automation from extracting unfired ammunition.
- Made spent-cartridge ejection use the runtime-stable empty big cartridge item API.
- Added safer checks around mounted cannon spent-cartridge removal.
- Added release documentation and MIT license text.

## 0.1.5

- Renamed the project to CBC: Firepower Components.
- Added the Cannon Magazine Loader test implementation.
- Added dynamic ammunition display for the loader.
- Added NeoForge 1.21.1 packaging.
