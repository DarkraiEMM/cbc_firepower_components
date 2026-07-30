# Dependency Upgrade and Cross-Version Compatibility Tasks

## Execution Rules

- Preserve all unrelated and uncommitted 0.3.5 work.
- Review each edited file before moving to the next task group.
- Do not publish, upload, tag, or push.
- Do not claim runtime compatibility from compilation alone.
- Produce test artifacts only after clean builds succeed.

## Task 1: Establish Verified Dependency Baselines

- [x] 1.1 Update NeoForge 1.21.1 `cbc_version` from `5.11.6` to `5.11.7`.
- [x] 1.2 Raise the NeoForge 1.21.1 CBC metadata floor to `5.11.7`.
- [x] 1.3 Confirm NeoForge remains pinned to `21.1.228`, as required by CBC 5.11.7 and Aeronautics 1.3.0.
- [x] 1.4 Retain Forge 1.20.1 Curse Maven CBC file ID `8169547`.
- [x] 1.5 Retain Forge 1.20.1 CBC range `[5.11.4,5.12.0)`.
- [x] 1.6 Record the exact CBC artifact hashes used by both branches.

**Checkpoint:** Both dependency graphs resolve without changing unrelated Create, Flywheel, Ponder, Registrate, or RPL versions.

## Task 2: Add Optional Physics Compatibility Boundary

- [x] 2.1 Introduce a dependency-neutral physical-distance resolver interface.
- [x] 2.2 Move normal Euclidean distance into a vanilla resolver.
- [x] 2.3 Add a Sable 2.0.3 resolver that uses `distanceSquaredWithSubLevels`.
- [x] 2.4 Ensure the Sable-specific class is loaded only when mod ID `sable` is present.
- [x] 2.5 Ensure linkage/API failure returns “unavailable” rather than raw sub-level coordinates.
- [x] 2.6 Route the spyglass rangefinder through the resolver facade.
- [x] 2.7 Add concise, one-time logging for an incompatible Sable API.
- [ ] 2.8 Verify the NeoForge module still compiles and loads without Sable on the classpath at runtime.

**Checkpoint:** Normal-world measurements work without Sable; Sable failure cannot produce absurd coordinate-derived distances.

## Task 3: Verify CBC 5.11.7 Integration Points

- [x] 3.1 Compile all CBC-targeting mixins against 5.11.7.
- [x] 3.2 Verify `CannonMountPointAmmoCompatMixin` injection targets and descriptors.
- [x] 3.3 Verify autocannon ammo-container accessors and capacity mixins.
- [x] 3.4 Verify mounted autocannon firing mixin behavior is not affected by CBC 5.11.7.
- [x] 3.5 Verify projectile/fuze item classification still recognizes CBC 5.11.7 items.
- [x] 3.6 Confirm no local workaround conflicts with CBC 5.11.7's Sable shell-impact fix.

**Checkpoint:** A clean NeoForge compile completes with no mixin target warnings attributable to CBC 5.11.7.

## Task 4: Isolate Enhanced Shells Compatibility

- [x] 4.1 Obtain and fingerprint the exact current NeoForge 1.21.1 Enhanced Shells JAR.
- [x] 4.2 Inspect its mod ID, ammunition classes, mounted contraptions, and insertion methods.
- [x] 4.3 Verify Enhanced Shells ammunition inherits CBC's standard projectile and propellant contracts; do not add redundant registry-name classification.
- [x] 4.4 Verify its cannon-loadable ammunition uses CBC's native big-cannon insertion path; do not add a parallel loader.
- [x] 4.5 Keep core ammunition helpers free of new addon-specific class-name checks.
- [x] 4.6 Preserve native rejection behavior for incompatible holders or launch-platform ammunition.
- [x] 4.7 Ensure the existing native insertion simulation path does not mutate source stacks or mounted inventories.
- [ ] 4.8 Ensure removing Enhanced Shells does not cause startup or class-loading failures.

**Checkpoint:** CBC-native ammunition remains functional with Enhanced Shells both installed and absent.

## Task 5: Audit Sable/Aeronautics Mount Behavior

- [x] 5.1 Audit compact mount position/orientation code for assumptions that only hold in the root level.
- [x] 5.2 Audit vertical compact mount assembly/disassembly on physical structures.
- [x] 5.3 Audit fire signal strength and rising-edge propagation on a physical structure.
- [x] 5.4 Audit item capability exposure for mounted cannons on physical structures.
- [x] 5.5 Avoid adding custom world/sub-level transforms where CBC or Sable already owns the operation.
- [x] 5.6 Fix only locally owned defects found by the audit. No additional local transform defect was identified statically.

**Checkpoint:** Code review identifies ownership for every physics-related operation; no duplicate transform system is introduced.

## Task 6: Repair Forge 1.20.1 Ammo-Container Semantics

- [x] 6.1 Make the large autocannon ammo box item unstackable.
- [x] 6.2 Sanitize cannon handoff to a one-count item copy.
- [x] 6.3 Make direct mount insertion consume exactly one container only after success.
- [x] 6.4 Make mechanical-arm insertion simulation side-effect free.
- [x] 6.5 Make real mechanical-arm insertion consume exactly one container only after success.
- [x] 6.6 Verify rejected or incompatible containers remain unchanged.
- [ ] 6.7 Confirm existing box contents survive inventory, placement, pickup, and cannon handoff.

**Checkpoint:** No code path can consume a whole source stack as one CBC magazine operation.

## Task 7: Build Verification

- [x] 7.1 Run a clean NeoForge 1.21.1 build against CBC 5.11.7.
- [x] 7.2 Run a clean Forge 1.20.1 build against CBC 5.11.4.
- [x] 7.3 Inspect generated mod metadata inside both output JARs.
- [x] 7.4 Inspect output JARs for accidentally bundled optional-addon classes or JARs.
- [x] 7.5 Record artifact filenames, sizes, and SHA-256 hashes.

**Checkpoint:** Both clean builds succeed and declare the intended dependency ranges.

## Task 8: Prepare Runtime Test Matrix

- [ ] 8.1 Prepare the NeoForge CBC-only test combination.
- [x] 8.2 Prepare the NeoForge CBC + Sable 2.0.3 + Aeronautics 1.3.0 combination.
- [x] 8.3 Prepare the NeoForge CBC + Enhanced Shells combination.
- [x] 8.4 Preserve and verify the existing CBCMS, CBCMW/NW, and CBCAT test JAR set.
- [x] 8.5 Prepare the Forge 1.20.1 CBC 5.11.4 test artifact separately.
- [x] 8.6 Write a concise Chinese test checklist for the user.

**Checkpoint:** Each test combination has an explicit dependency list and expected behavior.

## Task 9: User Runtime Validation

The user performs in-game testing.

- [ ] 9.1 CBC 5.11.7 native cannon assembly, firing, loading, save/reload, and disassembly.
- [ ] 9.2 Cannon mounted on a stationary Aeronautics physical structure.
- [ ] 9.3 Cannon mounted on a moving and rotating Aeronautics physical structure.
- [ ] 9.4 Spyglass distance within one physical space.
- [ ] 9.5 Spyglass distance between root level and a physical structure.
- [ ] 9.6 Spyglass distance between two physical structures.
- [ ] 9.7 Enhanced Shells supported ammunition loading.
- [ ] 9.8 Enhanced Shells unsupported ammunition rejection without loss.
- [ ] 9.9 Existing CBCMS, CBCMW/NW, and CBCAT regression set.
- [ ] 9.10 Forge 1.20.1 CBC 5.11.4 compact mount and ammo-box behavior.

**Checkpoint:** Record pass/fail evidence per scenario; failed scenarios return to the owning task group.

## Task 10: Final Compatibility Report

- [ ] 10.1 Summarize exact dependency versions tested.
- [ ] 10.2 List changed compatibility behavior by branch.
- [ ] 10.3 Clearly distinguish compile verification from user runtime verification.
- [ ] 10.4 List known unsupported addon ammunition contracts.
- [ ] 10.5 Provide both artifact links and SHA-256 hashes.
- [ ] 10.6 Do not upload or publish until separately authorized.

## Dependency Order

```text
Task 1
├── Task 2
├── Task 3
├── Task 4
├── Task 5
└── Task 6
     └── Task 7
          └── Task 8
               └── Task 9
                    └── Task 10
```

Tasks 2–6 may be implemented independently after Task 1, but the worktree is shared and dirty, so edits should be performed sequentially and reviewed after each group.

## Definition of Done

Implementation is complete when:

1. Both clean builds pass.
2. CBC 5.11.7 and CBC 5.11.4 dependency metadata is correct.
3. Sable/Aeronautics and Enhanced Shells remain optional.
4. Cross-physical-space distance never falls back to misleading local coordinates.
5. Forge 1.20.1 ammo boxes are unstackable and never consumed as an entire stack.
6. Test JARs and hashes are delivered without publishing.
7. User runtime results are incorporated into the compatibility report.
