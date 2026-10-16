# Radar EA 3.1.3 compatibility validation

Date: 2026-10-16. Release line: NeoForge 1.21.1 / CBC Firepower Components 0.4.3.

## Environment

Java 21, NeoForge 21.1.228, Create 6.0.10-280, Create Big Cannons 5.11.7,
Ritchie's Projectile Library 2.1.2, and Sable 2.0.3. Radar versions were run
separately, never installed together.

## Runtime scenarios

| Scenario | Assertions | Evidence log in the development checkout |
| --- | --- | --- |
| EA 3.1.3, standard CBC autocannon on a real Sable sub-level | Translating and turning with yaw/pitch/roll; both target positions tracked; inherited point velocity matches Sable in blocks/tick; muzzle matches actual spawned projectile; ammunition consumed; empty and lost-target output cleared | `radar-release-moving.log` |
| EA 3.1.3, single large autocannon AP | Tracking, automatic output, ammunition consumption, real projectile creation, empty/lost-target stop | `radar-large-ap-baseline.log` |
| EA 3.1.3, single large autocannon HE on vertical compact mount | Same firing assertions on the vertical mount | `radar-large-vertical-he.log` |
| EA 3.1.3, twin large autocannon HE | Tracking, real ammunition consumption/projectile, empty/lost-target stop | `radar-twin-he.log` |
| EA 3.1.3, twin AP on compact autocannon mount | Same firing assertions; predicted muzzle agrees with actual alternating-barrel spawn position | `radar-release-twin.log` |
| EA 3.1.3, CBC steel big cannon with powder charge and solid shot | Native controller/WFC automatic firing consumes both charge and shot, spawns a projectile, then clears empty output | `radar-big.log` |
| Radar 0.4.9.4, CBC autocannon | Real pig track, native WeaponGroupCoordinator, yaw/pitch, automatic firing, cartridge consumption, projectile creation; autoFire=false, empty, dead target, replaced fire controller, and unlink all stop output | `radar-release-build.log` |
| Radar EA 1.2.2 regression | Startup, assembled mount, old CBC context bridge, public mount yaw/pitch controller ticks | `radar-release-122.log` |
| Radar absent | Dedicated-server startup without optional Radar linkage errors | `radar-release-absent.log` |
| EA 3.1.3 client startup | WEAPON_API selected, public adapters registered, shaders uploaded, Minecraft window responding | `radar-release-client.log` |

The NeoForge `build` task passed. The packaged metadata reports version `0.4.3`;
Radar and Sable classes are not bundled into the artifact. SHA-256 of the release JAR:
`2e200abc8c338883f38eb79c0983e9e40b3b72bc996c47c5478aca28c1ad080e`.

## What these tests exercise

The opt-in server harness creates and assembles real CBC blocks and contraptions,
uses the installed Radar controllers/weapon network/firing control, and checks real
ammunition consumption and projectile entities. Kinetic input and ticks are driven
by the harness. EA 3 receives synthetic tracks at two positions; 4.9 receives an
actual spawned pig track. Sable tests allocate a real sub-level and physics body,
then apply controlled translation/rotation and nonzero linear/angular velocity.

These are dedicated-server integration tests, not a claim that every modpack,
sensor scan, UI interaction, multiplayer latency case, or projectile impact has
been tested. The EA 1.2.2 regression covers startup/aiming, not its older moving-ship
automatic-fire limitation. The moving-ship scenario applies to EA 3.1.3; it does
not establish moving-ship support for Radar 4.9.

## Fixes verified during this work

- EA 3's generic solver supplies world angles. Mount-local angles need conversion
  through Radar's public physics API on tilted ships.
- CBC's Sable projectile inheritance is configurable. Its point velocity is in
  blocks/second and must be divided by 20 for Radar's per-tick ballistic model.
- Twin barrels alternate their muzzle offset. The shot adapter reads the same
  offset used by this mod's projectile-spawn mixin.
- Radar 4.9 resolves CBC mount entities directly. A read-through CBC mount view
  lets its native firing controller operate on a compact mount, while a narrow
  output relay transfers the actual firing decision.
- Legacy empty-ammo output and stale output after controller removal/unlink were
  reproduced before adding guards. The guards do not replace Radar's targeting,
  line-of-sight, alignment, or safe-zone decisions.

## Reproduction

Run from the repository root with the actual optional JAR paths:

```powershell
.\gradlew.bat :neoforge-1.21.1:runServer --offline `
  '-Pradar_dev_jar=D:/create_radar-5.0-EA-3.1.3-1.21.1.jar' `
  '-Psable_dev_jar=D:/mods/sable-neoforge-1.21.1-2.0.3.jar' `
  -Pradar_self_test -Pradar_test_variant=moving --console=plain
```

Other variants: `standard`, `large-ap`, `large-vertical-he`, `twin-he`,
`twin-auto-mount-ap`, `big`, and `legacy-fire` (use the 0.4.9.4 JAR).
Omit `radar_dev_jar` for the absent-mod startup test. Do not run servers concurrently
against the same development world. The harness shuts down the server itself.
Check explicit `CBCFPC_* PASS` markers and absence of `CBCFPC_* FAIL`; Gradle can
report success even when an event-driven self-test logs a failure.

## Upstream diagnostics

EA 3's supplied JAR logs missing/invalid recipe references for its jammer and
copper plate ingredient. The client also reports a missing
`sky_radar_sublevel_connector` model. These originate in Radar's resources and
were not changed or bundled into this mod.
