# Heavy / Twin / Rocket Compatibility Requirements

## Purpose

This spec defines the next compatibility work after 0.3.1. The goal is not to create a full new weapon family yet. The goal is to make Compact Cannon Mount behavior, ammunition routing, and documentation safe enough for heavy autocannons, twin autocannons, rocket pods, and adjacent addon weapon types.

## Terminology

- Heavy autocannon: primarily CBC: Advanced Technologies heavy autocannon blocks, breeches, ammunition, and heavy autocannon ammo boxes.
- Twin autocannon: primarily CBC: Advanced Technologies twin/manual twin autocannon families. CBC Military Supplement dual cannons are treated as a separate big-cannon-like family, not as normal autocannon twins.
- Rocket class: CBC: Advanced Technologies rocket pod and medium rocket pod families. CBC Military Supplement projectile racks and torpedo tubes are treated as special launcher families.
- Compatibility: mount recognition, safe assembly/disassembly/fire control, and clearly defined item input behavior. It does not imply full automation support for every addon weapon.

## Core Premises

- 0.3.1 already adds mount data for several CBC:AT, CBCMS, and Neo Warfare-like cannon types on NeoForge 1.21.1.
- CBC:AT contains separate heavy autocannon ammo container classes and separate rocket/twin inventory handlers, so these systems should not be assumed to use vanilla CBC autocannon ammo box behavior.
- CBCMS contains ammo racks, projectile racks, torpedo tubes, and dual cannon systems with their own block entities and contraptions, so they should not be forced through the Cannon Magazine Loader or Autocannon Ammo Feed without API verification.
- Forge 1.20.1 should remain conservative. New external addon compatibility work should default to NeoForge 1.21.1 unless the same dependencies and APIs are confirmed for 1.20.1.

## Non-Goals

- Do not add a full self-made heavy autocannon family in this pass.
- Do not add new rocket ammunition or custom rocket pod blocks in this pass.
- Do not make the current Autocannon Ammo Feed accept rockets, torpedoes, projectile racks, or arbitrary addon ammo boxes.
- Do not promise Ponder scenes until the visual layout is separately accepted.

## Requirements

### R1: Stable Mount Compatibility

When a supported addon weapon is mounted on a compact mount, the mod shall avoid hard crashes during assembly, disassembly, redstone firing, save/load, and goggle inspection.

When a supported addon weapon has known pitch data, the compact mount shall use data-driven pitch ranges instead of hardcoded Java checks.

When an addon weapon type lacks confirmed mount behavior, the mod shall fail safely by refusing special automation rather than guessing the weapon contract.

### R2: Ammunition Input Classification

When the mounted weapon uses normal CBC autocannon or machine gun ammunition, existing large autocannon ammo box behavior may be reused.

When the mounted weapon uses CBC:AT heavy autocannon ammunition, the system shall treat it as a separate ammunition family and shall not mix it with normal autocannon/machine gun rounds.

When the mounted weapon is a rocket pod, medium rocket pod, projectile rack, or torpedo tube, the system shall not route ammunition through the Autocannon Ammo Feed or Cannon Magazine Loader unless a dedicated adapter is implemented and tested.

### R3: Adapter-First Design

When external addon APIs expose a stable inventory, ammo container, or insert handler, the mod shall integrate through a small adapter layer rather than special-casing every item in the mount block entity.

When no stable API is available, the mod shall support mount movement and firing only, and documentation shall state that automatic loading is not supported.

### R4: User-Facing Clarity

When a player sees tooltip or guide text for addon compatibility, the text shall distinguish between mount support and automatic ammunition support.

When a weapon family is mount-only, the documentation shall explicitly say so.

When a weapon family has a dedicated input path, the documentation shall say what can be inserted and from which block or face.

### R5: Test Matrix

When implementing any adapter, the mod shall test at minimum: assembly, disassembly, redstone fire, mechanical pitch/yaw, save/reload, and one positive and one negative ammunition insertion case.

When a compatibility target requires an external mod jar, the test shall be run with that jar present and also with that jar absent to verify optional dependency behavior.