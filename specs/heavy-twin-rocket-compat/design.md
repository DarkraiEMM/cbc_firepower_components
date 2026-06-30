# Heavy / Twin / Rocket Compatibility Design

## Design Summary

Use a three-layer compatibility model:

1. Mount layer: data-driven recognition, pitch range, safe control, and crash prevention.
2. Input strategy layer: small adapters that decide whether an item can be inserted into a mounted weapon.
3. Optional content layer: dedicated large boxes, feeders, or tutorial scenes only after the first two layers are stable.

This keeps the compact mount responsible for movement and control, while ammunition logic stays in family-specific adapters.

## Hidden Assumptions

- External addon class and resource names visible in the provided jars reflect the runtime behavior closely enough for initial design, but not enough for final implementation.
- CBC:AT heavy autocannon ammunition is not equivalent to normal CBC autocannon ammunition, because the jar exposes separate heavy autocannon ammo container classes.
- Rocket pods and medium rocket pods are launcher systems, not autocannon systems, even if they are mounted through CBC-style contraptions.
- CBCMS projectile racks and torpedo tubes are special weapon systems and should be treated as mount-only until their loading API is verified.

## If-Then Branches

| Condition | Design Result |
| --- | --- |
| If the addon weapon accepts normal CBC autocannon/machine gun container input | Reuse the existing Large Autocannon Ammo Box path. |
| If the addon weapon exposes a distinct heavy autocannon ammo container API | Add a dedicated heavy autocannon input strategy; do not route it through normal autocannon logic. |
| If the addon weapon exposes only a custom inventory handler | Add an optional adapter after verifying slot rules with javap/decompilation and in-game tests. |
| If the addon weapon has no stable public input path | Support mount control only; document that automatic loading is unsupported. |
| If the weapon is rocket pod / medium rocket pod | Treat as launcher class; direct automation requires a future rocket adapter, not the current feeders. |
| If the weapon is CBCMS projectile rack / torpedo tube | Treat as special launcher class; do not mix into Cannon Magazine Loader behavior. |

## Category Design

### CBC:AT Heavy Autocannon

Current evidence: the jar contains `HeavyAutocannonAmmoContainerBlock`, `HeavyAutocannonAmmoContainerItem`, `HeavyAutocannonInventoryHandler`, and heavy autocannon munition classes.

Design:

- Keep compact mount pitch/fire support data-driven.
- Add a `HeavyAutocannonInputStrategy` only after confirming its container interface.
- Keep heavy ammunition separate from normal autocannon/machine gun ammunition.
- Future optional content: Large Heavy Autocannon Ammo Box, but only after the strategy is stable.

Do not:

- Let the normal Large Autocannon Ammo Box accept heavy autocannon ammunition by default.
- Let Autocannon Ammo Feed insert heavy ammunition unless a dedicated heavy feed path is implemented.

### CBC:AT Twin Autocannon

Current evidence: the jar contains twin autocannon breech, inventory handler, mounted contraption, and vertical twin variants.

Design:

- Mount control should work through existing compact autocannon mount data.
- Treat firing behavior as owned by the external contraption.
- First test whether direct item input already works through the mounted weapon.
- If normal CBC autocannon ammunition is accepted, use existing ammo paths.
- If twin weapons use custom inventory rules, add a `TwinAutocannonInputStrategy`.

Do not:

- Create a separate twin ammo box unless the twin system actually has distinct ammo semantics.
- Assume horizontal and vertical twin variants share identical placement offsets without testing.

### CBC:AT Rocket Pod / Medium Rocket Pod

Current evidence: the jar contains separate rocket pod and medium rocket pod breeches, inventory handlers, mounted contraptions, rocket items, and rocket cartridge items.

Design:

- Compact cannon mount can support movement/fire if the mounted contraption behaves like a CBC cannon type.
- Loading support must be separate from autocannon feed and cannon magazine loader.
- A future `RocketPodInputStrategy` should verify whether the expected input is rocket item, rocket cartridge item, or paired rocket plus propellant item.
- Documentation should mark rocket pod support as mount/control support until the input strategy is implemented.

Do not:

- Insert rockets through the Cannon Magazine Loader.
- Insert rockets through Autocannon Ammo Feed.
- Treat rocket cartridges as big cannon cartridges.

### CBCMS Dual Cannon

Current evidence: CBCMS contains dual cannon block entities, quickfiring/sliding breeches, mounted dual cannon contraption, and dual cannon material data.

Design:

- Treat as big-cannon-like mount compatibility, not autocannon compatibility.
- Keep data-driven pitch support and crash protection.
- Do not add magazine loader support until dual cannon breech loading rules are verified.

### CBCMS Projectile Rack / Torpedo Tube

Current evidence: CBCMS contains projectile rack and torpedo tube block entities, mounted contraptions, material data, and ammunition/rack classes.

Design:

- Treat as special launcher compatibility.
- Mount control can be supported if contraption behavior is stable.
- Automatic loading should wait for a dedicated adapter.
- Ammo rack behavior should remain separate from compact mount generic item input.

## Proposed Code Shape

Add a small strategy boundary instead of expanding compact mount item logic directly:

```text
MountedWeaponInputStrategy
- boolean matches(MountedWeaponContext context)
- boolean canInsert(ItemStack stack, Direction side, MountedWeaponContext context)
- ItemStack insert(ItemStack stack, Direction side, MountedWeaponContext context)
- Component unsupportedReason(MountedWeaponContext context)
```

Initial strategies:

1. `CbcAutocannonInputStrategy` for existing autocannon/machine gun rounds.
2. `CbcBigCannonInputStrategy` for existing big cannon projectile/cartridge behavior.
3. `CbcAtHeavyAutocannonInputStrategy` after API verification.
4. `CbcAtRocketPodInputStrategy` after API verification.
5. `CbcmSpecialLauncherInputStrategy` only if CBCMS exposes a stable loading contract.

The compact mount should ask strategies in order and stop at the first matching strategy. If no strategy matches, it should refuse automation with no crash.

## Data and Tag Plan

Use tags where possible, but do not rely on tags alone for container semantics.

Candidate tags:

- `cbc_firepower_components:normal_autocannon_ammunition`
- `cbc_firepower_components:heavy_autocannon_ammunition`
- `cbc_firepower_components:rocket_pod_ammunition`
- `cbc_firepower_components:special_launcher_ammunition`

Tags classify items. Strategies still decide how insertion works.

## Version Split

### 0.3.2 or 0.4.0: Compatibility Foundation

- Confirm external APIs with javap/decompilation.
- Add strategy interface and no-op safe fallback.
- Add tests for current mounted weapon types.
- No new models.
- No Ponder scenes.

### 0.4.x: Heavy/Twin Input Support

- Add heavy autocannon strategy if CBC:AT exposes stable container behavior.
- Add twin input handling only if existing normal autocannon paths are insufficient.
- Add documentation for supported/unsupported automation.

### 0.5.x or Later: Dedicated Content

- Large Heavy Autocannon Ammo Box.
- Heavy Autocannon Ammo Feed.
- Rocket pod loading adapter or ready-rack style block.
- Ponder scenes after layout approval.

## Validation Matrix

| Target | Mount | Fire | Pitch/Yaw | Save/Reload | Loading | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| CBC:AT heavy autocannon | Required | Required | Required | Required | Adapter-gated | Separate ammo family. |
| CBC:AT twin autocannon | Required | Required | Required | Required | Test first | May reuse normal ammo. |
| CBC:AT rocket pod | Required | Required | Required | Required | Future adapter | Do not use current feeders. |
| CBC:AT medium rocket pod | Required | Required | Required | Required | Future adapter | Separate from rocket pod if needed. |
| CBCMS dual cannon | Required | Required | Required | Required | Future verification | Big-cannon-like, not autocannon. |
| CBCMS projectile rack | Mount-only initially | Required if stable | Required | Required | Future adapter | Special launcher. |
| CBCMS torpedo tube | Mount-only initially | Required if stable | Required | Required | Future adapter | Special launcher. |

## Main Risk

The largest risk is not pitch data. The risk is assuming all addon weapons share CBC's normal item insertion model. They do not appear to. The safe design is to keep movement/fire compatibility broad and keep ammunition automation narrow until each family is verified.