package com.cbcfirepowercomponents.mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.FMLLoader;

public class OptionalCompatMixinPlugin implements IMixinConfigPlugin {
	private static final String RADAR_API_REGISTRY =
		"com/happysg/radar/api/mount/RadarMountRegistry.class";
	private static final Logger LOGGER = LoggerFactory.getLogger("CBCFPC/RadarCompatibility");
	private enum RadarLayer { NONE, LEGACY, API, WEAPON_API, UNSUPPORTED }
	private RadarLayer radarLayer;

	private synchronized RadarLayer radarLayer() {
		if (radarLayer != null)
			return radarLayer;
		var radar = FMLLoader.getLoadingModList().getModFileById("create_radar");
		if (radar == null)
			return radarLayer = RadarLayer.NONE;
		// Read the discovered mod archive directly: optional classes need not yet be
		// visible through this plugin's classloader during Mixin preparation.
		boolean api = Files.isRegularFile(radar.getFile().findResource(RADAR_API_REGISTRY));
		String pitch = "com.happysg.radar.block.controller.pitch.AutoPitchControllerBlockEntity";
		String yaw = "com.happysg.radar.block.controller.yaw.AutoYawControllerBlockEntity";
		boolean supported = api
			? hasMethods(pitch, "resolveKineticMount", "refreshMountCache", "getFiringControl", "resolvePrimaryCbcMount", "canEngageTrack")
				&& hasMethods(yaw, "resolveKineticMount", "refreshMountCache")
				&& hasMethods("com.happysg.radar.block.controller.pitch.ApiMountPitch", "tick")
				&& hasMethods("com.happysg.radar.block.controller.yaw.ApiMountYaw", "tick")
				&& hasMethods("com.happysg.radar.compat.cbc.CannonMountContext", "of", "getContraption", "setPitch", "pitchLimits", "notifyUpdate", "controller", "getControllerBlockPos")
			: hasMethods(pitch, "resolveMount", "tick", "getFiringControl", "setTarget", "setAndAcquireTrack", "setAndAcquirePos", "atTargetPitch")
				&& hasMethods("com.happysg.radar.block.controller.firing.FireControllerBlockEntity", "setPoweredInternal")
				&& hasMethods(yaw, "tick", "setTarget", "atTargetYaw")
				&& hasMethods("com.happysg.radar.block.datalink.DataLinkBlockItem", "useOn", "isCannonMountBE");
		boolean weaponApi = Files.isRegularFile(radar.getFile().findResource(
			"com/happysg/radar/api/weapon/RadarWeaponRegistry.class"));
		if (weaponApi)
			supported &= hasMethods("com.happysg.radar.block.behavior.networks.WeaponNetworkRuntime", "addDirectMount")
				&& hasMethods("com.happysg.radar.block.behavior.networks.WeaponFiringControl", "tickGenericWeapon", "stopWeaponOutput")
				&& hasMethods("com.happysg.radar.api.network.RadarNetworkApi", "isLinked", "getSelectedContact")
				&& hasMethods("com.happysg.radar.block.radar.track.RadarTrack", "copy", "getScannedTime", "setPosition")
				&& hasMethods("com.happysg.radar.api.weapon.RadarWeaponAdapter", "getWeaponIdentity", "setFiring", "createContext")
				&& hasMethods("com.happysg.radar.api.weapon.WeaponShotProfile", "ballistic", "disabled");
		radarLayer = supported ? (api ? (weaponApi ? RadarLayer.WEAPON_API : RadarLayer.API) : RadarLayer.LEGACY) : RadarLayer.UNSUPPORTED;
		LOGGER.info("Selected Radar compatibility layer: {}", radarLayer);
		if (!supported)
			LOGGER.warn("Radar's controller contract is unsupported; skipping dedicated mount Mixins to preserve game startup.");
		return radarLayer;
	}

	private boolean hasMethods(String className, String... names) {
		var radar = FMLLoader.getLoadingModList().getModFileById("create_radar");
		var path = radar.getFile().findResource(className.replace('.', '/') + ".class");
		if (!Files.isRegularFile(path))
			return false;
		try (var input = Files.newInputStream(path)) {
			ClassNode node = new ClassNode();
			new ClassReader(input).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return java.util.Arrays.stream(names).allMatch(name -> node.methods.stream().anyMatch(method -> method.name.equals(name)));
		} catch (IOException | RuntimeException exception) {
			LOGGER.warn("Cannot inspect Radar compatibility target {}", className, exception);
			return false;
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("com.cbcfirepowercomponents.mixin.radar.")) {
			// These families are mutually exclusive. @Pseudo only tolerates absent
			// classes, not missing API methods on controllers also present in 4.9.
			// Never fall back to legacy injections on an unrecognized API build.
			boolean apiMixin = mixinClassName.startsWith("com.cbcfirepowercomponents.mixin.radar.api.");
			if (radarLayer() == RadarLayer.WEAPON_API)
				return apiMixin && (mixinClassName.endsWith("ApiMountPitchSpeedMixin")
					|| mixinClassName.endsWith("ApiMountYawSpeedMixin")
					|| mixinClassName.endsWith("WeaponNetworkRuntimeMixin")
					|| mixinClassName.endsWith("WeaponLiveTrackMixin")
					|| mixinClassName.endsWith("WeaponNetworkLifecycleMixin")
					|| mixinClassName.endsWith("MountApiPriorityMixin"));
			if (mixinClassName.endsWith("WeaponNetworkRuntimeMixin") || mixinClassName.endsWith("WeaponLiveTrackMixin")
				|| mixinClassName.endsWith("WeaponNetworkLifecycleMixin")) return false;
			return radarLayer() == (apiMixin ? RadarLayer.API : RadarLayer.LEGACY);
		}
		if (mixinClassName.startsWith("com.cbcfirepowercomponents.mixin.vestalihy."))
			return FMLLoader.getLoadingModList().getModFileById("vestalihy") != null;
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
