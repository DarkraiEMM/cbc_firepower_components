# Implementation Plan

## Preliminary API Findings

Local jar inspection confirms that the external families expose their own inventory or behavior classes. This means the implementation must not assume that all addon weapons can reuse CBC's normal autocannon or big cannon insertion path.

Confirmed class signatures from the provided jars:

- CBC:AT heavy autocannon uses `com.dsvv.cbcat.cannon.heavy_autocannon.breech.HeavyAutocannonInventoryHandler`, which implements NeoForge `IItemHandler`.
- CBC:AT rocket pod uses `com.dsvv.cbcat.cannon.rocketpod.breech.RocketPodInventoryHandler`, which implements NeoForge `IItemHandler`.
- CBC:AT medium rocket pod uses `com.dsvv.cbcat.cannon.medium_rocketpod.breech.MediumRocketPodInventoryHandler`, which implements NeoForge `IItemHandler`.
- CBC:AT twin autocannon uses `com.dsvv.cbcat.cannon.twin_autocannon.breech.TwinAutocannonInventoryHandler`, which implements NeoForge `IItemHandler`.
- CBCMS ammo rack exposes `AmmoRackBlockEntity#getItemHandler()`, `insertStack`, and `extractStack`.
- CBCMS projectile rack, torpedo tube, and dual cannon block entities expose CBC-style `cannonBehavior()`, but their actual loading rules still need verification.

## Tasks

- [x] 1. Verify external loading contracts in detail
  - Decompile or inspect CBC:AT heavy autocannon, twin autocannon, rocket pod, and medium rocket pod inventory handlers.
  - Confirm accepted item classes, slot counts, slot limits, and whether insertion requires item pairs or staged loading.
  - Inspect CBCMS ammo rack, dual cannon, projectile rack, and torpedo tube loading contracts.
  - Record which targets are mount-only and which targets can safely accept automation.
  - _Requirements: R1, R2, R3, R5_

- [x] 2. Add a NeoForge-only mounted weapon input strategy boundary
  - Add a small context object for the mounted contraption, entity, block entities, and insertion source.
  - Add a strategy interface for `matches`, `canInsert`, `insert`, and optional unsupported reason text.
  - Move the current normal CBC autocannon insertion behavior behind the first strategy.
  - Move the current big cannon insertion behavior behind the second strategy.
  - Add a no-match fallback that returns the original stack and never crashes.
  - _Requirements: R1, R2, R3_

- [x] 3. Route compact mount automation through the strategy layer
  - Update `CompactCannonMountBlockEntity` item insertion to call the strategy resolver.
  - Update `MTArmInteractionPointTypes.CompactCannonMountPoint` to use the same strategy resolver.
  - Keep existing CBC autocannon ammo, large autocannon ammo box, and big cannon magazine behavior unchanged.
  - Confirm that item handler insertion and mechanical arm insertion behave identically.
  - _Requirements: R1, R3, R5_

- [x] 4. Keep current feeders/loaders narrow by default
  - Keep `AutocannonAmmoFeedBlockEntity` limited to normal CBC autocannon/machine-gun-style ammunition until a heavy feed path exists.
  - Keep `CannonMagazineLoaderBlockEntity` limited to current big cannon magazine behavior.
  - Prevent rockets, torpedoes, projectile rack ammunition, and heavy autocannon rounds from being silently routed through the wrong block.
  - Add player-facing wording that mount support and automatic loading support are separate.
  - _Requirements: R2, R4_

- [x] 5. Implement CBC:AT compatibility gates after contract verification
  - Add a heavy autocannon strategy only if the heavy inventory handler accepts stable insertion through the mounted contraption capability.
  - Add a twin autocannon strategy only if normal CBC autocannon logic is insufficient.
  - Add rocket pod and medium rocket pod strategies only after confirming expected rocket item/cartridge semantics.
  - Use optional-mod detection and class-name checks carefully so the mod still loads without CBC:AT.
  - _Requirements: R1, R2, R3, R5_

- [ ] 6. Implement CBCMS compatibility gates after contract verification
  - Treat dual cannon as big-cannon-like only after confirming insertion through its own mount point or behavior.
  - Treat projectile rack and torpedo tube as mount-only unless their loading path is verified.
  - Do not connect CBCMS ammo rack behavior to compact mount automation unless it has a clear stable insertion contract.
  - Use optional-mod detection and avoid hard runtime dependency failures.
  - _Requirements: R1, R2, R3, R5_

- [ ] 7. Add compatibility documentation and tooltips
  - Document each supported addon target as one of: mount/control support, redstone fire support, automatic loading support.
  - Add Chinese and English wording for unsupported automation cases if they become visible to players.
  - Keep docs explicit about which block accepts which item and from which path.
  - _Requirements: R4_

- [x] 8. Build NeoForge 1.21.1 first
  - Build the NeoForge target to catch compile-time and resource errors.
  - Do not block landing on extra CBC:AT/CBCMS game-session compatibility testing; this is an explicit product decision for speed.
  - Keep runtime support claims limited to the generic mounted `IItemHandler` path and existing CBC paths.
  - _Requirements: R1, R5_

- [ ] 9. Decide Forge 1.20.1 scope after NeoForge verification
  - Check whether equivalent CBC:AT/CBCMS versions and class contracts exist for Forge 1.20.1.
  - If not confirmed, leave Forge 1.20.1 unchanged except for shared documentation that does not claim support.
  - If confirmed, port only the stable subset and run the same regression tests.
  - _Requirements: R1, R2, R5_

- [ ] 10. Prepare release notes after implementation builds
  - Summarize compatibility target by target.
  - Explicitly list automation support gaps.
  - Do not market CBCMS-specific loading systems as fully supported unless a stable insertion path is implemented.
  - _Requirements: R4, R5_