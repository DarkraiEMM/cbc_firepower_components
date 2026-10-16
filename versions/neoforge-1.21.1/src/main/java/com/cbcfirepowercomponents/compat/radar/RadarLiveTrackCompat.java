package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Refreshes EA 3's generic weapon snapshot from the network's selected contact. */
public final class RadarLiveTrackCompat {
    private static boolean warned;
    private static final java.util.Set<Object> NETWORK_CONTROLS = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private static final java.util.Map<BlockEntity, java.lang.ref.WeakReference<Object>> CONTROLLER_CONTROLS = new java.util.WeakHashMap<>();
    private RadarLiveTrackCompat() {}

    public static void onControllerTick(Object controller) {
        try {
            if (!(controller instanceof BlockEntity pitch) || !(pitch.getLevel() instanceof ServerLevel level)) return;
            var previous = CONTROLLER_CONTROLS.get(pitch);
            if (previous == null) return;
            Object tracked = previous.get();
            Object control = controller.getClass().getField("firingControl").get(controller);
            if (control != null) {
                var binoculars = control.getClass().getDeclaredField("binoMode");
                binoculars.setAccessible(true);
                if (binoculars.getBoolean(control)) { CONTROLLER_CONTROLS.remove(pitch); return; }
            }
            // A detached endpoint may no longer be visited by the network coordinator.
            Class<?> network = RadarPublicWeaponCompat.type("com.happysg.radar.api.network.RadarNetworkApi");
            boolean linked = (boolean) network.getMethod("isLinked", ServerLevel.class, BlockPos.class).invoke(null, level, pitch.getBlockPos());
            if (control != tracked || !linked) {
                if (tracked != null) {
                    tracked.getClass().getMethod("resetTarget").invoke(tracked);
                    tracked.getClass().getMethod("stopWeaponOutput").invoke(tracked);
                    NETWORK_CONTROLS.remove(tracked);
                }
                if (!linked && control != null) {
                    control.getClass().getMethod("resetTarget").invoke(control);
                    control.getClass().getMethod("stopWeaponOutput").invoke(control);
                }
                CONTROLLER_CONTROLS.put(pitch, new java.lang.ref.WeakReference<>(control));
            }
        } catch (ReflectiveOperationException e) {
            if (!warned) { warned = true; LogUtils.getLogger().warn("Cannot clear detached Radar weapon output", e); }
        }
    }

    public static boolean refresh(Object control) {
        try {
            Class<?> type = control.getClass();
            Object weapon = type.getField("weapon").get(control);
            if (weapon == null) return true;
            Class<?> adapter = RadarPublicWeaponCompat.type("com.happysg.radar.api.weapon.RadarWeaponAdapter");
            if (!(adapter.getMethod("getWeaponIdentity").invoke(weapon) instanceof CompactCannonMountBlockEntity)) return true;
            var binoculars = type.getDeclaredField("binoMode");
            binoculars.setAccessible(true);
            if (binoculars.getBoolean(control)) { NETWORK_CONTROLS.remove(control); return true; }
            if (!(type.getField("level").get(control) instanceof ServerLevel level)
                || !(type.getField("pitchController").get(control) instanceof BlockEntity pitch)) return true;
            Class<?> network = RadarPublicWeaponCompat.type("com.happysg.radar.api.network.RadarNetworkApi");
            if (!(boolean) network.getMethod("isLinked", ServerLevel.class, BlockPos.class).invoke(null, level, pitch.getBlockPos())) {
                if (NETWORK_CONTROLS.remove(control)) {
                    type.getMethod("resetTarget").invoke(control);
                    return false;
                }
                return true;
            }
            NETWORK_CONTROLS.add(control);
            CONTROLLER_CONTROLS.put(pitch, new java.lang.ref.WeakReference<>(control));
            Object selected = ((java.util.Optional<?>) network.getMethod("getSelectedContact", ServerLevel.class, BlockPos.class)
                .invoke(null, level, pitch.getBlockPos())).orElse(null);
            var field = type.getDeclaredField("activetrack");
            field.setAccessible(true);
            Object active = field.get(control);
            if (active == null || selected == null) return false;
            Class<?> contact = RadarPublicWeaponCompat.type("com.happysg.radar.api.tracking.RadarContact");
            if (!contact.getMethod("getId").invoke(active).equals(contact.getMethod("getId").invoke(selected))) return false;
            // Retain Radar's target identity, filter decisions, and jamming metadata.
            // Do not read the entity position directly or turn an unseen entity into a contact.
            if (!active.getClass().isInstance(selected)) return false;
            Object refreshed = selected.getClass().getMethod("copy").invoke(selected);
            long scanned = (long) selected.getClass().getMethod("getScannedTime").invoke(selected);
            // The network publishes at a lower cadence than the weapon tick. Account
            // for recent sample age before the solver adds projectile flight-time lead.
            // Bound extrapolation to one normal network refresh window; never query
            // hidden entity state or mutate the shared network contact.
            long age = Math.max(0, Math.min(10, level.getGameTime() - scanned));
            var position = (net.minecraft.world.phys.Vec3) contact.getMethod("getPosition").invoke(selected);
            var velocity = (net.minecraft.world.phys.Vec3) contact.getMethod("getVelocity").invoke(selected);
            refreshed.getClass().getMethod("setPosition", net.minecraft.world.phys.Vec3.class)
                .invoke(refreshed, position.add(velocity.scale(age)));
            field.set(control, refreshed);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            if (!warned) { warned = true; LogUtils.getLogger().warn("Cannot refresh Radar's selected contact; stopping compact weapon output", e); }
            return false;
        }
    }
}
