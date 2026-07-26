# Dependency Upgrade and Cross-Version Compatibility Requirements

## Purpose

This specification defines the compatibility pass for CBC: Firepower Components across its supported Minecraft branches.

The work has two release targets:

- NeoForge 1.21.1: update Create Big Cannons compatibility from 5.11.6 to 5.11.7 and verify the current Sable 2.0.3 and Create Aeronautics 1.3.0 runtime.
- Forge 1.20.1: verify and repair the existing 1.20.1 feature set against Create Big Cannons 5.11.4 using the user-provided JAR.

The 1.21.1 compatibility pass also includes the current Enhanced Shells / `cbcmoreshells` integration already present in the codebase. No project named "Safe Shells" is included until an exact project page or JAR can be verified.

## Verified Dependency Baseline

| Branch | Dependency | Target |
| --- | --- | --- |
| NeoForge 1.21.1 | Create Big Cannons | 5.11.7 |
| NeoForge 1.21.1 | Sable | 2.0.3 |
| NeoForge 1.21.1 | Create Aeronautics | 1.3.0 |
| NeoForge 1.21.1 | Enhanced Shells / `cbcmoreshells` | Current NeoForge 1.21.1 release |
| Forge 1.20.1 | Create Big Cannons | 5.11.4 |

The supplied `createbigcannons-5.11.4-mc.1.20.1-forge.jar` has SHA-256
`11845F0D79A9014977F667A57B14D0527928BEE8B66BC6B50BEDCC29EE81878A`.
It is byte-for-byte identical to the Gradle dependency currently resolved by Curse Maven file ID `8169547`.

## Scope Boundary

This pass preserves the existing feature scope of each branch.

- The NeoForge 1.21.1 branch retains the current 0.3.5 systems and compatibility adapters.
- The Forge 1.20.1 branch retains its current 0.3.1-era systems and receives dependency/API compatibility repairs.
- Porting every 0.3.5 block, GUI, network payload, controller, rack, collector, rangefinder, and Drive By Wire integration to Forge 1.20.1 is a separate feature-backport project.

## Requirements

### R1: NeoForge 1.21.1 CBC Upgrade

When the NeoForge 1.21.1 module is compiled, it shall compile against Create Big Cannons 5.11.7 rather than 5.11.6.

When CBC 5.11.7 changes a class, method, data component, capability, contraption, or ammunition-loading contract used by this mod, the integration shall be updated to the 5.11.7 contract without broad reflective fallbacks where a stable CBC API exists.

When the built mod declares its CBC dependency, the version range shall accept 5.11.7 and shall not claim compatibility with a CBC version that has not been tested.

### R2: Sable 2.0.3 and Create Aeronautics 1.3.0

When Sable 2.0.3 and Create Aeronautics 1.3.0 are installed, the mod shall load without linkage, mixin, capability, or class-resolution errors.

When the spyglass rangefinder measures between different Sable physical spaces, it shall report the resolved physical distance rather than raw sub-level coordinates.

When the current Sable distance API is unavailable or changed, the rangefinder shall fail visibly and safely instead of displaying an absurd coordinate-derived distance.

When Sable and Create Aeronautics are absent, the mod shall still load and all non-physics features shall remain usable.

### R3: Mounted Cannon Behavior on Physical Structures

When a compact or vertical compact cannon mount is placed on a Sable/Create Aeronautics physical structure, assembly, disassembly, pitch/yaw control, redstone firing, save/reload, and ammunition input shall not crash or lose the mounted cannon state.

When redstone or controller input crosses a physical-structure boundary, the cannon shall receive the intended signal strength and firing edge semantics.

### R4: Enhanced Shells Compatibility

When the current NeoForge 1.21.1 Enhanced Shells / `cbcmoreshells` mod is installed, supported projectile items shall be recognized as complete ammunition or paired projectile/propellant ammunition according to that mod's actual loading contract.

When an Enhanced Shells cannon is mounted on this mod's compact mount, supported loading paths shall use the addon's verified mount-point or contraption API.

When Enhanced Shells is absent, no direct class reference shall prevent this mod from loading.

When a third-party shell uses a custom holder, launch platform, or non-CBC loading contract, the mod shall refuse unsupported insertion without consuming or duplicating the item.

### R5: Forge 1.20.1 CBC 5.11.4 Compatibility

When the Forge 1.20.1 module is compiled with the supplied CBC 5.11.4 JAR, the build shall succeed without substituting an older CBC API.

When the existing 1.20.1 compact mounts assemble a CBC 5.11.4 big cannon, autocannon, or other already-supported cannon family, assembly, disassembly, movement, firing, and save/reload shall remain functional.

When the existing 1.20.1 loaders, feeders, and mechanical-arm interaction points handle CBC 5.11.4 ammunition, they shall consume exactly one intended item or one intended ammunition container operation and shall not consume an entire stacked container.

When CBC 5.11.4 is installed, the Forge metadata shall accept it; when an unsupported CBC version is installed, the metadata shall reject it clearly.

### R6: Optional Compatibility Isolation

When any optional addon is absent, compatibility code for that addon shall not be eagerly class-loaded.

When an optional addon changes an internal, non-public class name, the failure shall be isolated to that adapter and shall not disable CBC-native cannon mounting or ammunition handling.

Where an addon exposes a stable public API, the implementation shall prefer a small versioned adapter over scattered class-name checks in core block entities.

### R7: Regression and Artifact Verification

When implementation is complete, both modules shall pass clean Gradle builds from their own module tasks.

The NeoForge 1.21.1 runtime matrix shall include:

- CBC 5.11.7 alone with required Create/RPL dependencies.
- CBC 5.11.7 plus Sable 2.0.3 and Create Aeronautics 1.3.0.
- CBC 5.11.7 plus the current Enhanced Shells release.
- The established CBCMS, CBCMW/NW, and CBCAT compatibility test set where those JARs remain available.

The Forge 1.20.1 runtime matrix shall include CBC 5.11.4 and the existing 1.20.1 feature set.

When test artifacts are produced, their filenames shall identify Minecraft version, loader, and mod version unambiguously, and their SHA-256 hashes shall be recorded.

## Non-Goals

- Do not backport the entire NeoForge 1.21.1 0.3.5 feature set to Forge 1.20.1 in this pass.
- Do not add a hard dependency on Sable, Create Aeronautics, Enhanced Shells, CBCMS, CBCMW/NW, or CBCAT.
- Do not claim compatibility with an unidentified "Safe Shells" project.
- Do not silently accept arbitrary items as cannon ammunition solely because their registry name contains words such as `shell`, `round`, or `projectile`.
- Do not upload or publish a release until the user separately approves the built test results and release scope.

## Acceptance Summary

The work is accepted when:

1. NeoForge 1.21.1 builds and runs against CBC 5.11.7.
2. Sable 2.0.3 and Create Aeronautics 1.3.0 preserve real-distance measurement and mounted-cannon behavior.
3. Enhanced Shells compatibility consumes and loads only verified ammunition forms.
4. Forge 1.20.1 builds and its existing feature set works against the supplied CBC 5.11.4 JAR.
5. Optional addons can be removed without causing startup failures.
6. Both distributable JARs and their hashes are produced for testing, without publishing.
