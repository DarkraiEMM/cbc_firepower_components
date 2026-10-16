package com.cbcfirepowercomponents.compat.radar;

import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlockEntity;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.ItemStackHandler;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

/** Opt-in integration fixture: native item links, scanning, selection and projectile collision. */
final class RadarScanImpactSelfTest {
    private static Object call(Object receiver, String name, Object... args) throws ReflectiveOperationException {
        Class<?> type = receiver instanceof Class<?> c ? c : receiver.getClass();
        for (var method : type.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            boolean matches = true;
            for (int i = 0; i < args.length; i++)
                if (args[i] != null && !method.getParameterTypes()[i].isInstance(args[i])) matches = false;
            if (matches) return method.invoke(receiver instanceof Class<?> ? null : receiver, args);
        }
        throw new NoSuchMethodException(type.getName() + "." + name);
    }
    private static ItemStack item(String name) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create_radar", name));
        if (item == net.minecraft.world.item.Items.AIR) throw new IllegalStateException("Missing item " + name);
        return new ItemStack(item);
    }
    private static BlockEntity place(ServerLevel level, BlockPos pos, String id) {
        var state = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create_radar", id)).defaultBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) state = state.setValue(BlockStateProperties.FACING, Direction.SOUTH);
        else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        level.setBlock(pos, state, 3);
        var be = level.getBlockEntity(pos);
        if (be == null) throw new IllegalStateException("Missing fixture " + id);
        be.onLoad();
        return be;
    }
    private static void link(ServerLevel level, BlockPos source, BlockPos endpoint) {
        var player = FakePlayerFactory.getMinecraft(level);
        player.setItemInHand(InteractionHand.MAIN_HAND, item("data_link"));
        player.setShiftKeyDown(false);
        for (var pos : java.util.List.of(source, endpoint)) {
            player.setPos(pos.getCenter().add(0, 2, 0));
            var context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(pos.getCenter().add(0, .5, 0), Direction.UP, pos, false));
            var result = player.getMainHandItem().useOn(context);
            if (!result.consumesAction()) throw new IllegalStateException("Data-link item rejected " + pos + ": " + result);
        }
        if (level.getBlockEntity(endpoint.above()) == null) throw new IllegalStateException("Data link not placed on " + endpoint);
    }
    private static void filter(ItemStackHandler inventory, int slot, String item, String key, boolean animals) {
        var stack = item(item);
        CompoundTag settings = new CompoundTag();
        for (String flag : java.util.List.of("animal", "mob", "autoTarget", "lineSight")) settings.putBoolean(flag, true);
        for (String flag : java.util.List.of("player", "contraption", "sable", "projectile", "item")) settings.putBoolean(flag, false);
        settings.putBoolean("animal", animals);
        settings.putBoolean("mob", animals);
        CompoundTag data = new CompoundTag();
        data.put(key, settings);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        inventory.setStackInSlot(slot, stack);
    }
    static void run(ServerLevel level, CompactCannonMountBlockEntity mount, BlockEntity pitch) throws ReflectiveOperationException {
        BlockPos radarPos = mount.getBlockPos().offset(5, 0, 0), filterPos = radarPos.south(4);
        var target = EntityType.PIG.create(level);
        var cannon = (MountedAutocannonContraption) mount.getContraption().getContraption();
        var breech = (AbstractAutocannonBreechBlockEntity) cannon.presentBlockEntities.get(cannon.getStartPos());
        ItemStack ammo = breech.getInputBuffer().iterator().next().copy();
        mount.setLimitedPitch(0);
        mount.setLimitedYaw(180);
        pitch.getClass().getMethod("setTargetAngle", float.class).invoke(pitch, 0F);
        level.getBlockEntity(mount.getBlockPos().below()).getClass().getMethod("setTargetAngle", float.class)
            .invoke(level.getBlockEntity(mount.getBlockPos().below()), 180F);
        // Create initializes its mounted block entities on the first entity tick.
        mount.getContraption().tick();
        breech = (AbstractAutocannonBreechBlockEntity) cannon.presentBlockEntities.get(cannon.getStartPos());
        if (breech.getInputBuffer().isEmpty()) cannon.insertItemIntoCannon(ammo.copy(), false);
        java.util.Set<Entity> projectiles = new java.util.HashSet<>();
        String variant = System.getProperty("cbcfpc.radarTestVariant", "scan-impact");
        double distance = variant.contains("far") ? 90 : 45;
        try {
            for (int x=-20; x<=20; x++) for (int z=-20; z<=20; z++) level.getChunk(x,z);
            var radar = place(level, radarPos, "plane_radar");
            var filterer = place(level, filterPos, "network_filterer");
            // Exercise precisely the server-side item interaction used by a player.
            link(level, filterPos, radarPos);
            link(level, filterPos, pitch.getBlockPos());
            var inventory = (ItemStackHandler) call(filterer, "getItemHandler");
            filter(inventory, 0, "radar_filter_item", "detection", true);
            filter(inventory, 2, "target_filter_item", "targeting", false);
            call(filterer, "applyFiltersToNetwork");
            target.setNoAi(true);
            target.setNoGravity(true);
            target.setPos(mount.getBlockPos().getCenter().add(variant.contains("reverse") ? 12 : -12, 1, -distance));
            level.addFreshEntity(target);
            LogUtils.getLogger().info("CBCFPC_SCAN_SETUP radar={} range={} fov={} nearby={} targetAlive={}", radar.getBlockState(), call(radar, "getRange"), call(radar, "getFovDegrees"), level.getEntitiesOfClass(Entity.class, target.getBoundingBox().inflate(2)).size(), target.isAlive());
            var yaw = level.getBlockEntity(mount.getBlockPos().below());
            boolean scanned = false, selected = false;
            int shots = 0;
            Vec3 velocity = new Vec3(variant.contains("reverse") ? -.4 : variant.contains("fast") ? .4 : .12, 0, 0);
            var filterTick = filterer.getClass().getMethod("tick", net.minecraft.world.level.Level.class,
                BlockPos.class, net.minecraft.world.level.block.state.BlockState.class, filterer.getClass());
            for (int tick=0; tick<500 && target.getHealth() == target.getMaxHealth(); tick++) {
                if (tick == 30) {
                    if (!scanned || selected || shots != 0) throw new IllegalStateException("Target filter failed to exclude scanned animal");
                    filter(inventory, 2, "target_filter_item", "targeting", true);
                    call(filterer, "applyFiltersToNetwork");
                }
                ((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime()+1);
                target.setPos(target.position().add(velocity));
                target.setDeltaMovement(velocity);
                call(radar, "tick");
                for (Object track : (java.util.Collection<?>) call(radar, "getTracks"))
                    if (target.getStringUUID().equals(call(track, "getId"))) scanned = true;
                filterTick.invoke(null, level, filterPos, filterer.getBlockState(), filterer);
                Object active = call(filterer, "getActiveRadarContact");
                if (active != null && target.getStringUUID().equals(call(active, "getId"))) selected = true;
                ((KineticBlockEntity) level.getBlockEntity(pitch.getBlockPos().east())).setSpeed(120);
                ((KineticBlockEntity) level.getBlockEntity(yaw.getBlockPos().below())).setSpeed(120);
                call(pitch, "tick");
                call(yaw, "tick");
                call(RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.WeaponGroupCoordinator"), "onLevelTick",
                    new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, level));
                Object control = pitch.getClass().getField("firingControl").get(pitch);
                if (tick % 50 == 0 && active != null && control != null) {
                    var field = control.getClass().getDeclaredField("activetrack");
                    field.setAccessible(true);
                    Object controlTrack = field.get(control);
                    LogUtils.getLogger().info("CBCFPC_TRACK_DIAGNOSTIC selected={} control={} velocity={}", call(active,"getPosition"), controlTrack == null ? null : call(controlTrack,"getPosition"), controlTrack == null ? null : call(controlTrack,"getVelocity"));
                }
                if (breech.getInputBuffer().isEmpty()) cannon.insertItemIntoCannon(ammo.copy(), false);
                mount.getContraption().tick();
                java.util.List<Entity> current = new java.util.ArrayList<>();
                level.getAllEntities().forEach(e -> { if (e instanceof AbstractCannonProjectile && e.isAlive()) current.add(e); });
                for (Entity projectile : current) {
                    if (projectiles.add(projectile)) {
                        shots++;
                        if (shots <= 3) LogUtils.getLogger().info("CBCFPC_SHOT pos={} velocity={} yaw={} pitch={} target={}", projectile.position(), projectile.getDeltaMovement(), mount.getCannonYaw(), mount.getCannonPitch(), target.position());
                    }
                    projectile.tick();
                }
                if (tick % 50 == 0) LogUtils.getLogger().info("CBCFPC_SCAN_PROGRESS tick={} scanned={} selected={} shots={} health={} target={} assembled={} entityAlive={} controller={} ammo={} pitch={} yaw={}", tick, scanned, selected, shots, target.getHealth(), target.position(), mount.isRunning(), mount.getContraption().isAlive(), mount.getContraption().getController(), breech.getInputBuffer(), mount.getCannonPitch(), mount.getCannonYaw());
            }
            if (!scanned || !selected || shots == 0 || target.getHealth() == target.getMaxHealth())
                throw new IllegalStateException("Scan/impact failed: scanned="+scanned+" selected="+selected+" shots="+shots+" health="+target.getHealth());
            // Remove the actual link through the normal block lifecycle and verify
            // the network cannot leave a cached firing command behind.
            level.setBlock(pitch.getBlockPos().above(), Blocks.AIR.defaultBlockState(), 3);
            for (int i=0; i<25; i++) {
                ((net.minecraft.world.level.storage.ServerLevelData) level.getLevelData()).setGameTime(level.getGameTime()+1);
                call(radar, "tick");
                filterTick.invoke(null, level, filterPos, filterer.getBlockState(), filterer);
                call(pitch, "tick");
                call(RadarPublicWeaponCompat.type("com.happysg.radar.block.behavior.networks.WeaponGroupCoordinator"), "onLevelTick",
                    new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, level));
            }
            if (mount.getBlockState().getValue(com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock.FIRE_POWERED))
                throw new IllegalStateException("Unlinked network left firing output enabled: linked=" + call(RadarPublicWeaponCompat.type("com.happysg.radar.api.network.RadarNetworkApi"), "isLinked", level, pitch.getBlockPos()) + " linkBE="+level.getBlockEntity(pitch.getBlockPos().above())+" control="+pitch.getClass().getField("firingControl").get(pitch)+" active="+call(filterer,"getActiveRadarContact"));
            LogUtils.getLogger().info("CBCFPC_SCAN_IMPACT_TEST PASS variant={} itemLinks=true filterExclusion=true nativeScan=true autoSelect=true unlinkStop=true shots={} movingTargetVelocity={} distance={} impactHealth={}", variant, shots, velocity, distance, target.getHealth());
        } finally {
            projectiles.forEach(Entity::discard);
            target.discard();
            for (var pos : java.util.List.of(pitch.getBlockPos().above(), radarPos.above(), radarPos, filterPos))
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
