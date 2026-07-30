package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.FirepowerComponents;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class MTPonderPlugin implements PonderPlugin {
	@Override
	public String getModId() {
		return FirepowerComponents.MOD_ID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		MTPonderScenes.register(helper);
	}
}
