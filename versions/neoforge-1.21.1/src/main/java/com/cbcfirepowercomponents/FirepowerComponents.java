package com.cbcfirepowercomponents;

import com.cbcfirepowercomponents.compat.radar.RadarApiCompat;
import com.cbcfirepowercomponents.registry.MTBlockEntities;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.cbcfirepowercomponents.registry.MTCreativeTabs;
import com.cbcfirepowercomponents.registry.MTEntityTypes;
import com.cbcfirepowercomponents.registry.MTItems;
import com.cbcfirepowercomponents.registry.MTCapabilities;
import com.cbcfirepowercomponents.registry.MTArmInteractionPointTypes;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.event.MTCommonEvents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import rbasamoyai.createbigcannons.cannon_control.config.CannonMountPropertiesHandler;
import rbasamoyai.createbigcannons.cannon_control.config.SimpleBlockMountProperties;

@Mod(FirepowerComponents.MOD_ID)
public class FirepowerComponents {

	public static final String MOD_ID = "cbc_firepower_components";

	public FirepowerComponents(IEventBus modBus) {
		MTBlocks.register(modBus);
		MTItems.register(modBus);
		MTEntityTypes.register(modBus);
		MTBlockEntities.register(modBus);
		MTCreativeTabs.register(modBus);
		MTArmInteractionPointTypes.register(modBus);
		modBus.addListener(MTCapabilities::register);
		modBus.addListener(MTNetwork::register);
		modBus.addListener(this::commonSetup);
		NeoForge.EVENT_BUS.addListener(MTCommonEvents::onTooltip);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		CannonMountPropertiesHandler.registerBlockMountSerializer(MTBlockEntities.COMPACT_CANNON_MOUNT.get(),
			new SimpleBlockMountProperties.Serializer());
		event.enqueueWork(RadarApiCompat::register);
	}

}
