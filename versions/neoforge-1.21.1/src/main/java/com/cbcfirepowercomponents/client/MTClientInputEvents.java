package com.cbcfirepowercomponents.client;

import com.cbcfirepowercomponents.FirepowerComponents;
import com.cbcfirepowercomponents.content.automatic_cannon_controller.AutomaticCannonControllerBlock;
import com.cbcfirepowercomponents.network.ControllerHoldInteractionPacket;
import com.cbcfirepowercomponents.network.MTNetwork;
import com.cbcfirepowercomponents.network.MeasureSpyglassDistancePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = FirepowerComponents.MOD_ID, value = Dist.CLIENT)
public final class MTClientInputEvents {
	private static final int CONTROLLER_HOLD_TICKS = 10;
	private static net.minecraft.core.BlockPos heldController;
	private static int heldControllerTicks;
	private static boolean controllerScreenRequested;

	private MTClientInputEvents() {}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		while (MTKeyMappings.MEASURE_DISTANCE.consumeClick()) {
			if (minecraft.player != null
				&& minecraft.player.isUsingItem()
				&& minecraft.player.getUseItem().is(Items.SPYGLASS)) {
				MTNetwork.sendToServer(MeasureSpyglassDistancePacket.INSTANCE);
			}
		}

		if (minecraft.screen == null
			&& minecraft.player != null
			&& minecraft.level != null
			&& !minecraft.player.isShiftKeyDown()
			&& minecraft.options.keyUse.isDown()
			&& minecraft.hitResult instanceof BlockHitResult blockHit
			&& minecraft.level.getBlockState(blockHit.getBlockPos()).getBlock() instanceof AutomaticCannonControllerBlock) {
			net.minecraft.core.BlockPos pos = blockHit.getBlockPos();
			if (!pos.equals(heldController)) {
				heldController = pos.immutable();
				heldControllerTicks = 0;
				controllerScreenRequested = false;
			}
			++heldControllerTicks;
			if (!controllerScreenRequested) {
				boolean openScreen = heldControllerTicks >= CONTROLLER_HOLD_TICKS;
				MTNetwork.sendToServer(new ControllerHoldInteractionPacket(heldController, openScreen));
				if (openScreen)
					controllerScreenRequested = true;
			}
		} else {
			heldController = null;
			heldControllerTicks = 0;
			controllerScreenRequested = false;
		}
	}
}
