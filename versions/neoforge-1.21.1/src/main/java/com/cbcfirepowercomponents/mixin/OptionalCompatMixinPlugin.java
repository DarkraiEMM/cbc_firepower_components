package com.cbcfirepowercomponents.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.FMLLoader;

public class OptionalCompatMixinPlugin implements IMixinConfigPlugin {
	private static final String RADAR_API_REGISTRY =
		"com/happysg/radar/api/mount/RadarMountRegistry.class";

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
			// API bridge mixins are @Pseudo and therefore safe when Radar is absent.
			// Do not gate them on FML/resource discovery: this callback runs early
			// enough that those two views of the mod class path can disagree.
			if (mixinClassName.startsWith("com.cbcfirepowercomponents.mixin.radar.api."))
				return true;

			ClassLoader loader = OptionalCompatMixinPlugin.class.getClassLoader();
			boolean publicApiAvailable = loader.getResource(RADAR_API_REGISTRY) != null;
			boolean radarLoaded = FMLLoader.getLoadingModList().getModFileById("create_radar") != null;
			return radarLoaded && !publicApiAvailable;
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
