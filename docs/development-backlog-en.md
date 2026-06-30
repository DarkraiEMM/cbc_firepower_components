# CBC: Firepower Components Development Backlog

This document tracks feature ideas, compatibility work, and conservative workload estimates. It is not a player guide and it is not a release commitment.

## Core Assumptions

- The 0.3.x line should focus on compatibility, crash fixes, and documentation rather than large new weapon systems.
- New barrels, ammunition, ammo boxes, and loaders require code, data, models, textures, Ponder scenes, and in-game testing.
- Addon compatibility should start with mount data and crash prevention. Full gameplay support must be verified per addon.
- NeoForge 1.21.1 is the main feature-development line. Forge 1.20.1 should only receive features that can be migrated safely.
- Estimates are conservative and do not include platform publishing, screenshots, videos, or feedback-driven rework.

## Low-Risk 0.3.x Work

| Item | Scope | Status | Notes |
| --- | --- | --- | --- |
| CBC 5.11.6 recoil API bridge | NeoForge 1.21.1 | Implemented | Prevents crashes from older addons calling the old recoil hook. |
| Compact mount defensive checks | Forge 1.20.1 / NeoForge 1.21.1 | Implemented | Redstone updates, contraption direction, and stress calculation no longer hard-cast blindly. |
| CBC: Advanced Technologies mount data | NeoForge 1.21.1 | Implemented | Heavy autocannon, twin autocannon, manual twin autocannon, rocket pod, and medium rocket pod. |
| CBC Military Supplement mount data | NeoForge 1.21.1 | Present | Dual cannon, projectile rack, and torpedo tube. |
| CBC: Neo Warfare mount data | NeoForge 1.21.1 | Present | Still needs in-game verification per cannon type. |

## Compatibility Priorities

### CBC: Advanced Technologies

Safe early work:

- Compact mount pitch data.
- Crash prevention for unexpected contraption states.
- Verify whether heavy autocannons and twin autocannons can use the existing compact mount item input path.

Do not merge into the first compatibility release:

- Full twin steel autocannon family.
- Full heavy steel autocannon family.
- Heavy autocannon ammunition family.

These are not simple data-pack compatibility tasks. They are effectively new CBC-style weapon families and require a complete code, art, loading, firing, balancing, testing, and documentation loop.

Conservative estimate:

| Work | Code/Data | Model/Texture Review | Testing | Overall |
| --- | ---: | ---: | ---: | --- |
| Pitch data and crash prevention only | 0.5-1 day | 0 | 0.5 day | Small release work |
| Better heavy/twin input support | 1-2 days | 0 | 1-2 days | Small or medium release work |
| Steel autocannon muzzle brake | 2-4 days | 1-2 days | 1-2 days | Medium feature |
| Twin steel autocannon family | 5-8 days | 3-5 days | 3-5 days | Major feature |
| Heavy steel autocannon family | 7-12 days | 4-7 days | 4-6 days | Major feature |
| Heavy autocannon ammunition family | 4-7 days | 2-4 days | 3-5 days | Major feature |

### CBC Military Supplement

Safe early work:

- Keep compact mount data for dual cannons, projectile racks, and torpedo tubes.
- Verify assembly, disassembly, firing, and item input.

Needs separate review:

- Ready ammunition rack. The 4x2 internal storage and hit-triggered explosion behavior make it closer to a standalone hazardous ammunition block than a normal magazine.

Conservative estimate:

| Work | Code/Data | Model/Texture Review | Testing | Overall |
| --- | ---: | ---: | ---: | --- |
| Mount data and basic verification | 0.5-1 day | 0 | 1 day | Small release work |
| Ready rack display/docs support | 1-2 days | 0.5-1 day | 1 day | Small-medium work |
| New standalone ready ammunition rack | 5-9 days | 2-4 days | 4-6 days | Major, high-risk feature |

### CBC: Neo Warfare / Modern Warfare Addons

User reports currently focus on crashes when other addon cannons are mounted. Stability should take priority over new features.

Recommended scope:

- Add defensive checks and pitch data first.
- Identify whether each cannon type needs a custom item input path.
- Do not assume all addon cannons behave like CBC vanilla ItemCannon implementations.

Conservative estimate: 1-3 days, depending on cannon count and custom loading behavior.

## Original Feature Ideas

### Large Ammo Boxes

Candidate items:

- Large machine gun ammo box: no separate box planned for now; machine gun rounds are handled by the Large Autocannon Ammo Box using CBC-style ammo container behavior.
- Large autocannon ammo box: up to 64 autocannon or machine gun rounds plus 64 matching tracer rounds.
- Large heavy autocannon ammo box: up to 128 heavy autocannon rounds of any supported type plus 128 tracer rounds.

Feasibility: medium. This fits the mod's compact automation direction.

Main risks:

- Tracer mixing needs a clear rule: fixed ratio, separate output, ordered insertion, or configurable behavior.
- Large storage needs save data, display state, hopper/funnel/mechanical-arm behavior, and JEI information.
- External ammunition support should use tags or interfaces rather than hard-coded item IDs.

Conservative estimate:

| Work | Code/Data | Model/Texture Review | Testing | Overall |
| --- | ---: | ---: | ---: | --- |
| One basic large ammo box | 3-5 days | 1-2 days | 2-3 days | Medium feature |
| Full three-box set | 7-12 days | 3-6 days | 4-7 days | Major release feature |
| Tracer mixing behavior | 2-4 days | 0-1 day | 2-3 days | Separate risk point |

### Semi-Armor-Piercing and Heavy Ammunition

Candidate items:

- Autocannon semi-armor-piercing round: small impact explosion around 1x1x1, roughly 4 damage.
- Heavy autocannon semi-armor-piercing round: small impact explosion around 3x3x3, roughly 10 damage.
- Heavy autocannon anti-tank round: high penetration that is independent from initial muzzle velocity.

Feasibility: medium-high, but CBC's current damage, explosion, and penetration APIs must be checked first.

Main risks:

- Damage and blast radius need to stay close to CBC balance.
- Velocity-independent penetration may bypass CBC's existing ballistic balance.
- Projectile heads and assembled ammunition should follow CBC's existing item structure.

Conservative estimate:

| Work | Code/Data | Model/Texture Review | Testing | Overall |
| --- | ---: | ---: | ---: | --- |
| Autocannon SAP round | 2-4 days | 1-2 days | 2 days | Medium feature |
| Heavy autocannon SAP round | 3-5 days | 1-2 days | 2-3 days | Medium feature |
| Heavy anti-tank round | 3-6 days | 1-2 days | 3-4 days | Medium-high risk |

### Heavy Autocannon Loader

Candidate item:

- A heavy autocannon loader similar to the normal autocannon ammo feed, with internal storage for 32 heavy autocannon rounds.

Feasibility: relatively high after the heavy ammunition system is defined.

Main risks:

- If heavy autocannons come from an external addon, item matching must be confirmed.
- If this mod adds the heavy autocannon system, the loader should wait until the ammunition API is stable.

Conservative estimate: 3-6 days, not counting the heavy ammunition itself.

## Recommended Version Split

### 0.3.x: Compatibility and Stability

Include:

- CBC 5.11.6 recoil compatibility.
- CBC:AT / CBCMS / CBC: Neo Warfare compact mount data.
- Defensive checks for external contraptions.
- Documentation that distinguishes mount support from full automatic loading support.

Do not include:

- New barrels.
- New ammunition families.
- Specialized ammo boxes beyond the current Large Autocannon Ammo Box.

### 0.4.x: Ammo Box and Feeding Expansion

Recommended focus:

- Start with the large autocannon ammo box as the shared autocannon and machine gun ammo container.
- Define tracer mixing behavior before implementation.
- Add Ponder scenes and JEI information alongside the block.

### 0.5.x or Later: New Weapon Families

Candidates:

- Steel autocannon muzzle brake.
- Twin steel autocannon family.
- Heavy steel autocannon family.
- Heavy autocannon rounds, magazines, and loaders.

This phase needs a separate model review. Barrel models and textures alone should reserve at least 2 days for discussion and rework. Twin and heavy families should reserve 3-5 days of art review, not one rushed session.
