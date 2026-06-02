# Changelog

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
