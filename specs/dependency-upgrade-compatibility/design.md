# Dependency Upgrade and Cross-Version Compatibility Design

## Design Summary

Treat version upgrades and optional-addon compatibility as separate layers:

1. Pin each Minecraft branch to a verified CBC baseline.
2. Keep CBC-native code compiled directly against that baseline.
3. Put Sable, Create Aeronautics, and shell-addon behavior behind narrow optional adapters.
4. Validate runtime combinations rather than assuming a successful compile proves compatibility.
5. Keep the Forge 1.20.1 feature set intentionally smaller than NeoForge 1.21.1.

This design avoids adding version checks throughout cannon mounts, ammunition racks, and controllers.

## Evidence and API Findings

### CBC 5.11.6 to 5.11.7

Binary comparison shows only four changed classes relevant to Java behavior:

- `rbasamoyai.createbigcannons.compat.sable.ShellSubLevelImpactCallback`
- `rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem`
- `rbasamoyai.createbigcannons.munitions.FuzedProjectileBlockItem`
- `rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.FluidShellBlockItem`

The public signatures used by this project are unchanged. The current NeoForge source compiles successfully when `cbc_version` is overridden to `5.11.7`.

CBC 5.11.7 itself declares:

- NeoForge `21.1.228`
- Sable `[2.0.0,)` as optional
- Simulated `[1.3.0,)` as optional
- Aeronautics `[1.3.0,)` as optional
- Offroad `[1.3.0,)` as optional

Therefore the project already has the necessary NeoForge baseline at `21.1.228`; the required dependency change is CBC `5.11.7`.

### Sable 2.0.3

Sable 2.0.3 exposes the public API:

```java
Sable.HELPER.distanceSquaredWithSubLevels(Level, Position, Position)
```

The current reflective rangefinder resolves this exact method. A compile-checked optional adapter is preferable because it detects future API changes during development while preserving startup without Sable.

### Create Aeronautics 1.3.0

The distributed Aeronautics JAR is a bundle containing the physics stack. Its wrapper mod ID is `aeronautics_bundled`; the contained runtime also exposes the component mod IDs used by CBC (`simulated`, `aeronautics`, and `offroad`).

This mod does not need to call Aeronautics flight APIs directly. Physical-space transforms and projectile impacts should remain owned by Sable and CBC. Direct Aeronautics integration would duplicate their transform logic and create another unstable dependency surface.

### Forge 1.20.1

The Forge 1.20.1 source compiles successfully against the supplied CBC 5.11.4 JAR.

The existing 1.20.1 large autocannon ammo box item is not explicitly limited to one item per stack. This preserves the reported risk that a stackable filled container can be consumed as one cannon magazine operation. The 1.20.1 compatibility pass must include the same unstackable-item and one-container insertion invariant as the 1.21.1 branch.

## Architecture

### 1. Dependency Baselines

#### NeoForge 1.21.1

Change:

```properties
cbc_version=5.11.7
cbc_version_range=[5.11.7,5.12.0)
```

Keep:

```properties
neoforge_version=21.1.228
create_neoforge_version=6.0.10-280
rpl_version=2.1.2
```

Add development-only coordinates for Sable 2.0.3 and, if available through a stable repository, Aeronautics 1.3.0. They must be `compileOnly`/`localRuntime` or test-runtime dependencies, never required implementation dependencies.

#### Forge 1.20.1

Keep Curse Maven file ID `8169547`, because it resolves to the supplied and verified CBC 5.11.4 binary.

Keep metadata range:

```properties
cbc_version_range=[5.11.4,5.12.0)
```

No CBC downgrade fallback will be added.

### 2. Optional Physics Compatibility Boundary

Introduce a dependency-neutral facade:

```text
PhysicalDistanceResolver
└── OptionalDouble distance(Level level, Position from, Position to)
```

Implementations:

- `VanillaDistanceResolver`: Euclidean distance in a normal level.
- `Sable203DistanceResolver`: calls the Sable 2.0.3 public helper API.

Selection:

1. Check whether mod ID `sable` is loaded.
2. Only then load the Sable-specific implementation.
3. Catch linkage failure at the adapter boundary.
4. Return an unavailable result when Sable is present but its required physical transform cannot be resolved.
5. Never fall back to raw local coordinates after detecting different physical spaces.

The public rangefinder code will only reference the dependency-neutral facade. No Sable type will appear in rangefinder network payloads, saved data, or common registry initialization.

### 3. Mounted Cannon Compatibility on Physics Structures

Use CBC 5.11.7 as the owner of:

- shell/sub-level collision callbacks;
- cannon contraption behavior;
- projectile coordinate transforms.

Keep this mod responsible for:

- mount assembly position and orientation;
- local redstone/controller signal strength;
- item capability exposure;
- save/load of its own mount state.

Do not add coordinate-transform guesses to `CompactCannonMountBlockEntity`.

Runtime verification will explicitly cover a mount placed inside an Aeronautics/Sable physical structure. If a defect appears, the fix should be placed at the smallest boundary:

- mount position/orientation defect: compact mount code;
- projectile impact transform defect: CBC/Sable compatibility, not duplicated locally;
- rangefinder distance defect: `PhysicalDistanceResolver`;
- remote signal defect: controller/Drive By Wire adapter.

### 4. Enhanced Shells Adapter

Keep Enhanced Shells optional and isolate all addon class names in a dedicated compatibility package.

Refactor the current Enhanced Shells logic into two responsibilities:

```text
EnhancedShellAmmunitionClassifier
- recognizes verified complete rounds, projectiles, and propellants

EnhancedShellMountLoader
- matches supported mounted contraption families
- invokes only verified mount-point insertion contracts
- returns the original stack unchanged on failure
```

The core `CannonAmmunitionHelper` may call the classifier through a neutral interface, but it shall not accumulate more Enhanced Shells class-name checks.

The core `MountedWeaponInputStrategies` registry may register the optional loader, but the loader itself owns reflection or direct optional types.

Supported behavior is version-specific. A class or method mismatch disables that adapter and logs one concise diagnostic; it must not crash registry loading or affect CBC-native cannons.

### 5. Forge 1.20.1 Ammo-Container Safety

Apply these invariants to the large autocannon ammo box:

1. The item has maximum stack size one.
2. When a cannon receives the container, insertion operates on a one-count copy.
3. The source stack shrinks by exactly one only after successful insertion.
4. Simulation never changes the source, its contents, or the mounted cannon.
5. A rejected container returns unchanged.

The primary implementation points are:

- `versions/forge-1.20.1/.../registry/MTItems.java`
- `versions/forge-1.20.1/.../large_autocannon_ammo_box/LargeAutocannonAmmoBoxItem.java`
- `versions/forge-1.20.1/.../compact_cannon_mount/CompactCannonMountBlockEntity.java`
- `versions/forge-1.20.1/.../registry/MTArmInteractionPointTypes.java`

No 1.21 data-component code will be copied mechanically into 1.20.1; the Forge 1.20.1 NBT/container API will remain native to that branch.

## Planned File-Level Changes

### NeoForge 1.21.1

- `gradle.properties`
  - CBC compile version and accepted version floor.
- `build.gradle`
  - Optional development/runtime dependency configuration for compatibility verification if stable coordinates are available.
- `src/main/templates/META-INF/neoforge.mods.toml`
  - Optional compatibility declarations only where they improve load ordering and accurately describe tested versions.
- `content/rangefinder/SableDistanceCompat.java`
  - Replace broad reflection with the dependency-neutral facade or reduce it to adapter selection.
- `compat/physics/*`
  - New optional Sable 2.0.3 adapter and vanilla resolver.
- `content/CannonAmmunitionHelper.java`
  - Delegate Enhanced Shells classification.
- `content/compact_cannon_mount/input/MountedWeaponInputStrategies.java`
  - Delegate Enhanced Shells loading.
- `compat/enhanced_shells/*`
  - Version-isolated classifier and mount loader.

Existing unrelated 0.3.5 changes remain untouched.

### Forge 1.20.1

- `registry/MTItems.java`
  - Make large ammo box items unstackable.
- `content/large_autocannon_ammo_box/LargeAutocannonAmmoBoxItem.java`
  - Enforce a single-container handoff representation.
- `content/compact_cannon_mount/CompactCannonMountBlockEntity.java`
  - Verify one-count container insertion and success-only source consumption.
- `registry/MTArmInteractionPointTypes.java`
  - Verify mechanical-arm simulation and one-container transfer.
- Metadata and Gradle properties
  - Retain the verified CBC 5.11.4 baseline and explicit range.

## Error Handling

- CBC is required: an unsupported CBC version should be rejected by mod metadata before gameplay.
- Sable/Aeronautics are optional: absence produces normal-world behavior.
- Sable present but API incompatible: range measurement reports unavailable; no raw sub-level distance is shown.
- Enhanced Shells present but adapter incompatible: affected ammunition is rejected unchanged and one diagnostic is logged.
- Mechanical-arm simulation: no stack, NBT, data component, or cannon inventory may mutate.
- Runtime adapter failures must be rate-limited or logged once to avoid per-tick log spam.

## Compatibility Test Matrix

| Branch | Runtime | Build | Load | Mount | Fire | Load ammo | Range | Remove optional mod |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1.21.1 | CBC 5.11.7 | Required | Required | CBC native | Required | Required | Normal | N/A |
| 1.21.1 | CBC + Sable 2.0.3 + Aero 1.3.0 | Required | Required | Physical structure | Required | Required | Cross-space actual distance | Required |
| 1.21.1 | CBC + Enhanced Shells current | Required | Required | Supported families | Required | Positive and negative cases | N/A | Required |
| 1.21.1 | CBC + MS/MW/AT test set | Required | Required | Existing matrix | Required | Existing adapters | N/A | Required |
| 1.20.1 | CBC 5.11.4 | Required | Required | Existing families | Required | One container only | N/A | N/A |

## Runtime Scenarios

### Sable/Aeronautics

1. Measure two points in the same normal level.
2. Measure from the normal level to a physical structure.
3. Measure between two different physical structures.
4. Assemble and fire a cannon on a stationary physical structure.
5. Repeat while the structure is moving and rotating.
6. Save, leave, reload, and disassemble.
7. Remove Sable/Aeronautics and verify clean startup.

### Enhanced Shells

1. Insert each verified supported projectile/round.
2. Insert a required propellant pair where applicable.
3. Reject unsupported custom holders without consumption.
4. Simulate insertion and compare all inventories before/after.
5. Remove Enhanced Shells and verify clean startup.

### Forge 1.20.1

1. Confirm the large ammo box cannot stack above one.
2. Attempt direct cannon insertion.
3. Attempt mechanical-arm insertion.
4. Verify exactly one box operation occurs.
5. Verify failed insertion returns the untouched item.
6. Exercise compact big-cannon and autocannon assembly, fire, save/load, and disassembly.

## Build and Artifact Plan

Run:

```text
gradlew :neoforge-1.21.1:clean :neoforge-1.21.1:build
gradlew :forge-1.20.1:clean :forge-1.20.1:build
```

Build one branch at a time so failures and generated resources remain attributable.

Record:

- artifact filename;
- Minecraft version and loader;
- dependency test set;
- SHA-256;
- compile result;
- runtime scenarios actually tested by the user.

Copy test artifacts only after both clean builds succeed. Do not publish or upload in this task.

## Main Risks

### Dirty 1.21.1 Worktree

The NeoForge branch contains a large uncommitted 0.3.5 feature set. Changes must be narrow and reviewed file-by-file so compatibility work does not overwrite ongoing models, GUIs, controller logic, or ammunition fixes.

### Optional Mod Internals

Enhanced Shells currently requires knowledge of addon-specific classes and methods. The adapter boundary limits damage when those internals change, but runtime testing with the exact current JAR remains mandatory.

### Compile Success Is Not Runtime Proof

CBC 5.11.7 compiles without source changes, but its release specifically changes Sable impact compatibility. Physical-structure firing must therefore be tested in game even if the build is green.

### 1.20.1 Scope Confusion

The 1.20.1 branch will become compatible with current CBC while retaining its existing feature set. It will not gain the full 0.3.5 content set under this design.
