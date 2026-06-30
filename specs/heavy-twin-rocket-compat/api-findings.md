# External Loading API Findings

## Scope

This file records local jar inspection results for the heavy/twin/rocket compatibility work. The current implementation target is NeoForge 1.21.1 first.

## CBC: Advanced Technologies

Jar inspected:

- `.codex_tmp/reference_jars/cbc_at_Neoforge_1.21.1_0.1.4c.jar`

Findings:

- Heavy autocannon uses `HeavyAutocannonInventoryHandler`.
  - Implements NeoForge `IItemHandler`.
  - Slot count: 2.
  - Insertion path accepts slot 1.
  - Valid ammunition class: `HeavyAutocannonAmmoItem`.
  - The handler appends one copied round into the breech input buffer while respecting the breech queue limit.
- Twin autocannon uses `TwinAutocannonInventoryHandler`.
  - Implements NeoForge `IItemHandler`.
  - Slot count: 2.
  - Insertion path accepts slot 1.
  - Valid ammunition class: CBC `AutocannonAmmoItem`.
  - The handler appends one copied round into the breech input buffer while respecting the breech queue limit.
- Rocket pod uses `RocketPodInventoryHandler`.
  - Implements NeoForge `IItemHandler`.
  - Slot count: 15.
  - Insertion path accepts slot 1.
  - Valid ammunition class observed in bytecode: `MediumRocketItem`.
- Medium rocket pod uses `MediumRocketPodInventoryHandler`.
  - Implements NeoForge `IItemHandler`.
  - Slot count: 4.
  - Insertion accepts slots greater than or equal to 1.
  - Valid ammunition class: `MediumRocketItem`.

Implementation consequence:

- CBC:AT can be reached without hard compile-time references by delegating to the mounted contraption entity item capability and trying all handler slots.
- This should help hoppers and mechanical arms that initially target slot 0, because the compact mount strategy now searches every slot for a consuming insertion.

## CBC: Military Supplement

Jar inspected:

- `.codex_tmp/reference_jars/CBC-Military-Supplement-1.21.1-2.1.0.jar`

Findings:

- `AmmoRackBlockEntity` exposes `getItemHandler()`, `insertStack`, and `extractStack`.
- `AmmoRackItemHandler`
  - Implements NeoForge `IItemHandler`.
  - Slot count: 6.
  - `insertItem` delegates directly to a Create `SmartInventory`.
  - `isItemValid` returns true.
- `AmmoRackInteractionPoint`
  - Mechanical arm mode is extraction oriented.
  - `insert(...)` always returns `ItemStack.EMPTY`, so it should not be treated as a normal insertion target.
- Dual cannon, projectile rack, and torpedo tube mount points each implement their own `getInsertedResultAndDoSomething` and static insert/load methods.
  - These methods check specific mounted contraption classes such as `MountedDualCannonContraption`, `MountedProjectileRackContraption`, and `MountedTorpedoTubeContraption`.
  - They manipulate each family through its own `cannonBehavior()` and sync helpers.

Implementation consequence:

- CBCMS cannot be safely supported by the generic mounted item capability strategy alone.
- Dual cannon, projectile rack, and torpedo tube automation need dedicated optional strategies after their exact class contracts are copied or reflected carefully.
- Until those dedicated strategies exist, docs should distinguish mount/control compatibility from automatic loading compatibility.