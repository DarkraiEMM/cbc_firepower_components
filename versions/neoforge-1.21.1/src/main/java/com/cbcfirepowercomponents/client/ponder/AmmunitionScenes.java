package com.cbcfirepowercomponents.client.ponder;

import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlock;
import com.cbcfirepowercomponents.content.cannon_magazine_loader.CannonMagazineLoaderBlockEntity;
import com.cbcfirepowercomponents.content.carousel_ammunition_rack.CarouselAmmunitionRackBlockEntity;
import com.cbcfirepowercomponents.content.compact_cannon_mount.CompactCannonMountBlock;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlock;
import com.cbcfirepowercomponents.content.ready_ammunition_compartment.ReadyAmmunitionCompartmentBlockEntity;
import com.cbcfirepowercomponents.content.spent_casing_collector.SpentCasingCollectorBlock;
import com.cbcfirepowercomponents.registry.MTBlocks;
import com.simibubi.create.AllBlocks;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonCartridgeItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlockItem;

public final class AmmunitionScenes {
	private AmmunitionScenes() {
	}

	public static void autocannonFeed(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "autocannon_feed", "Using an Autocannon Ammo Feed");
		BlockPos arm = util.grid().at(4, 1, 7);
		BlockPos feed = util.grid().at(4, 1, 5);
		BlockPos mount = util.grid().at(4, 1, 4);
		BlockPos cannonMountPoint = mount.north();
		BlockPos breech = cannonMountPoint.west();
		BlockPos recoilSpring = cannonMountPoint;
		BlockPos barrel = cannonMountPoint.east();

		SceneSupport.place(scene, util, mount,
			MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.EAST));
		SceneSupport.place(scene, util, breech,
			facing(CBCBlocks.STEEL_AUTOCANNON_BREECH.getDefaultState(), Direction.EAST));
		SceneSupport.place(scene, util, recoilSpring,
			facing(CBCBlocks.STEEL_AUTOCANNON_RECOIL_SPRING.getDefaultState(), Direction.EAST));
		SceneSupport.place(scene, util, barrel,
			facing(CBCBlocks.STEEL_AUTOCANNON_BARREL.getDefaultState(), Direction.EAST));
		SceneSupport.place(scene, util, feed, MTBlocks.AUTOCANNON_AMMO_FEED.get().defaultBlockState());
		scene.overlay().showText(45)
			.text("Place the ammo feed next to the autocannon mount")
			.pointAt(util.vector().centerOf(feed))
			.placeNearTarget();
		scene.idle(55);

		ItemStack cartridge = filledAutocannonCartridge();
		scene.overlay().showControls(util.vector().topOf(feed), Pointing.DOWN, 35)
			.withItem(cartridge);
		scene.overlay().showText(45)
			.text("Insert loaded CBC autocannon cartridges")
			.pointAt(util.vector().topOf(feed))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(feed), util.vector().centerOf(mount), 45);
		scene.overlay().showText(45)
			.text("The feed passes ammunition directly through the mount")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(55);

		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showControls(util.vector().topOf(arm), Pointing.DOWN, 30)
			.withItem(cartridge);
		scene.overlay().showText(45)
			.text("A Mechanical Arm can keep the feed supplied")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(55);

		scene.effects().emitParticles(util.vector().blockSurface(barrel, Direction.EAST),
			scene.effects().simpleParticleEmitter(net.minecraft.core.particles.ParticleTypes.FLAME,
				util.vector().of(0.12, 0, 0)), 1, 8);
		scene.overlay().showText(40)
			.text("The autocannon can continue firing while ammunition remains")
			.pointAt(util.vector().centerOf(barrel))
			.placeNearTarget();
		scene.idle(50);
		SceneSupport.finish(scene);
	}

	public static void magazineLoader(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "magazine_loader", "Using the Three-Round Cannon Loader");
		BlockPos loader = util.grid().at(3, 1, 4);
		BlockPos mount = loader.east();
		BlockPos cannonMountPoint = mount.east();
		BlockPos breech = cannonMountPoint.north();
		BlockPos chamber = cannonMountPoint;
		BlockPos barrel = cannonMountPoint.south();

		SceneSupport.place(scene, util, mount,
			MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.SOUTH));
		SceneSupport.place(scene, util, breech,
			facing(CBCBlocks.STEEL_QUICKFIRING_BREECH.getDefaultState(), Direction.SOUTH));
		SceneSupport.place(scene, util, chamber,
			facing(CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState(), Direction.SOUTH));
		SceneSupport.place(scene, util, barrel,
			facing(CBCBlocks.STEEL_CANNON_BARREL.getDefaultState(), Direction.SOUTH));
		SceneSupport.place(scene, util, loader,
			MTBlocks.CANNON_MAGAZINE_LOADER.get().defaultBlockState()
				.setValue(CannonMagazineLoaderBlock.FACING, Direction.WEST));
		scene.overlay().showOutline(PonderPalette.OUTPUT, "loader_output", util.select().position(mount), 45);
		scene.overlay().showText(45)
			.text("Point the loader output toward the cannon mount")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(55);

		ItemStack projectile = new ItemStack(CBCBlocks.HE_SHELL.get());
		ItemStack propellant = BigCartridgeBlockItem.getWithPower(2);
		scene.overlay().showControls(util.vector().topOf(loader), Pointing.DOWN, 35)
			.withItem(projectile);
		scene.world().modifyBlockEntity(loader, CannonMagazineLoaderBlockEntity.class,
			blockEntity -> blockEntity.insertManual(projectile.copy()));
		scene.overlay().showText(45)
			.text("Insert one CBC projectile")
			.pointAt(util.vector().topOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(loader), Pointing.DOWN, 35)
			.withItem(propellant);
		scene.world().modifyBlockEntity(loader, CannonMagazineLoaderBlockEntity.class,
			blockEntity -> blockEntity.insertManual(propellant.copy()));
		scene.overlay().showText(45)
			.text("Then insert its propellant charge")
			.pointAt(util.vector().topOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.world().modifyBlockEntity(loader, CannonMagazineLoaderBlockEntity.class, blockEntity -> {
			blockEntity.insertManual(new ItemStack(CBCBlocks.AP_SHELL.get()));
			blockEntity.insertManual(propellant.copy());
			blockEntity.insertManual(new ItemStack(CBCBlocks.SHRAPNEL_SHELL.get()));
			blockEntity.insertManual(propellant.copy());
		});
		scene.overlay().showText(45)
			.text("Each projectile and charge pair occupies one of three positions")
			.pointAt(util.vector().centerOf(loader))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(loader),
			util.vector().centerOf(mount), 45);
		scene.overlay().showText(45)
			.text("The loader sends each pair to the cannon in order")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(55);

		BlockPos arm = loader.west(2);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showText(40)
			.text("A Mechanical Arm can refill the loader")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(50);
		SceneSupport.finish(scene);
	}

	public static void readyCompartment(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "ready_compartment", "Using the Ready Ammunition Compartment");
		BlockPos rack = util.grid().at(3, 1, 4);
		BlockPos mount = rack.east();
		BlockPos cannonMountPoint = mount.east();
		BlockPos breech = cannonMountPoint.north();
		BlockPos chamber = cannonMountPoint;
		BlockPos barrel = cannonMountPoint.south();
		SceneSupport.place(scene, util, rack,
			MTBlocks.READY_AMMUNITION_COMPARTMENT.get().defaultBlockState()
				.setValue(ReadyAmmunitionCompartmentBlock.FACING, Direction.EAST));
		SceneSupport.place(scene, util, mount,
			MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.SOUTH));
		SceneSupport.place(scene, util, breech,
			facing(CBCBlocks.STEEL_QUICKFIRING_BREECH.getDefaultState(), Direction.SOUTH));
		SceneSupport.place(scene, util, chamber,
			facing(CBCBlocks.STEEL_CANNON_CHAMBER.getDefaultState(), Direction.SOUTH));
		SceneSupport.place(scene, util, barrel,
			facing(CBCBlocks.STEEL_CANNON_BARREL.getDefaultState(), Direction.SOUTH));

		ItemStack projectile = new ItemStack(CBCBlocks.HE_SHELL.get());
		ItemStack propellant = BigCartridgeBlockItem.getWithPower(2);
		scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 35)
			.withItem(projectile);
		scene.world().modifyBlockEntity(rack, ReadyAmmunitionCompartmentBlockEntity.class,
			blockEntity -> blockEntity.insert(projectile.copy(), false));
		scene.overlay().showText(40)
			.text("Insert a projectile into the compartment")
			.pointAt(util.vector().topOf(rack))
			.placeNearTarget();
		scene.idle(50);

		scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 35)
			.withItem(propellant);
		scene.world().modifyBlockEntity(rack, ReadyAmmunitionCompartmentBlockEntity.class,
			blockEntity -> blockEntity.insert(propellant.copy(), false));
		scene.overlay().showText(40)
			.text("Add the matching charge to complete that position")
			.pointAt(util.vector().topOf(rack))
			.placeNearTarget();
		scene.idle(50);

		scene.world().modifyBlockEntity(rack, ReadyAmmunitionCompartmentBlockEntity.class, blockEntity -> {
			blockEntity.insert(new ItemStack(CBCBlocks.AP_SHELL.get()), false);
			blockEntity.insert(propellant.copy(), false);
			blockEntity.insert(new ItemStack(CBCBlocks.SHRAPNEL_SHELL.get()), false);
			blockEntity.insert(propellant.copy(), false);
		});
		scene.overlay().showText(45)
			.text("Additional rounds fill the next empty positions in order")
			.pointAt(util.vector().centerOf(rack))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(rack), Pointing.DOWN, 30).rightClick();
		scene.overlay().showText(45)
			.text("Right-click to inspect and reorder the ammunition queue")
			.pointAt(util.vector().topOf(rack))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(rack), util.vector().centerOf(mount), 45);
		scene.overlay().showText(45)
			.text("Point the output toward a mount to supply it directly")
			.pointAt(util.vector().centerOf(mount))
			.placeNearTarget();
		scene.idle(55);

		scene.world().modifyBlockEntity(rack, ReadyAmmunitionCompartmentBlockEntity.class,
			ReadyAmmunitionCompartmentBlockEntity::extractSelectedRound);
		scene.overlay().showText(45)
			.text("After one round leaves, the remaining queue advances")
			.pointAt(util.vector().blockSurface(rack, Direction.EAST))
			.placeNearTarget();
		scene.idle(55);

		BlockPos arm = rack.west(2);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showText(45)
			.text("A Mechanical Arm can insert or extract ammunition")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void carousel(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "carousel_ammunition_rack", "Using the Carousel Ammunition Rack");
		BlockPos carousel = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, carousel, MTBlocks.CAROUSEL_AMMUNITION_RACK.get().defaultBlockState());
		scene.world().showSection(util.select().fromTo(3, 1, 3, 5, 1, 5), Direction.DOWN);
		scene.idle(12);

		scene.overlay().showOutline(PonderPalette.GREEN, "footprint",
			util.select().fromTo(3, 1, 3, 5, 1, 5), 45);
		scene.overlay().showText(45)
			.text("Leave a centered 3 by 3 space for the carousel")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(55);

		BlockPos shaft = carousel.below();
		SceneSupport.place(scene, util, shaft,
			AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
		scene.overlay().showText(40)
			.text("Supply rotational force from below")
			.pointAt(util.vector().centerOf(shaft))
			.placeNearTarget();
		scene.idle(50);

		ItemStack projectile = new ItemStack(CBCBlocks.HE_SHELL.get());
		ItemStack propellant = BigCartridgeBlockItem.getWithPower(2);
		scene.overlay().showControls(util.vector().topOf(carousel), Pointing.DOWN, 35)
			.withItem(projectile);
		scene.world().modifyBlockEntity(carousel, CarouselAmmunitionRackBlockEntity.class, blockEntity -> {
			blockEntity.insert(projectile.copy(), false);
			blockEntity.insert(propellant.copy(), false);
			blockEntity.insert(new ItemStack(CBCBlocks.AP_SHELL.get()), false);
			blockEntity.insert(propellant.copy(), false);
			blockEntity.insert(new ItemStack(CBCBlocks.SHRAPNEL_SHELL.get()), false);
			blockEntity.insert(propellant.copy(), false);
		});
		scene.overlay().showText(45)
			.text("Insert complete rounds; each one occupies a separate position")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(55);

		BlockPos mount = util.grid().at(4, 1, 7);
		SceneSupport.place(scene, util, mount,
			MTBlocks.COMPACT_CANNON_MOUNT.get().defaultBlockState()
				.setValue(CompactCannonMountBlock.HORIZONTAL_FACING, Direction.EAST));
		scene.overlay().showOutline(PonderPalette.OUTPUT, "outlet", util.select().position(4, 1, 5), 45);
		scene.overlay().showText(45)
			.text("The side nearest the mount becomes the outlet")
			.pointAt(util.vector().of(4.5, 1.5, 5.5))
			.placeNearTarget();
		scene.idle(55);

		scene.world().modifyBlockEntity(carousel, CarouselAmmunitionRackBlockEntity.class, blockEntity -> {
			blockEntity.extractAlignedRound();
			blockEntity.setSpeed(64);
		});
		for (int step = 1; step <= 4; ++step) {
			float progress = step / 4.0f;
			scene.world().modifyBlockEntityNBT(util.select().position(carousel),
				CarouselAmmunitionRackBlockEntity.class, tag -> tag.putFloat("IndexProgress", progress));
			scene.idle(5);
		}
		scene.world().modifyBlockEntityNBT(util.select().position(carousel),
			CarouselAmmunitionRackBlockEntity.class, tag -> {
				tag.putInt("CurrentIndex", 1);
				tag.putInt("TargetIndex", 1);
				tag.putFloat("IndexProgress", 0);
			});
		scene.overlay().showText(45)
			.text("After a round leaves, the next occupied position advances")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(55);

		scene.overlay().showControls(util.vector().topOf(carousel), Pointing.DOWN, 30).rightClick();
		scene.overlay().showText(45)
			.text("Right-click to inspect and reorder the carousel")
			.pointAt(util.vector().topOf(carousel))
			.placeNearTarget();
		scene.idle(55);

		BlockPos arm = util.grid().at(7, 1, 5);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showText(45)
			.text("Take the outlet round by hand or with a Mechanical Arm")
			.pointAt(util.vector().of(4.5, 1.5, 5.5))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	public static void spentCollector(SceneBuilder scene, SceneBuildingUtil util) {
		SceneSupport.begin(scene, util, "spent_casing_collector", "Collecting Spent Casings");
		BlockPos collector = util.grid().at(4, 1, 4);
		SceneSupport.place(scene, util, collector, MTBlocks.SPENT_CASING_COLLECTOR.get().defaultBlockState());

		scene.overlay().showOutline(PonderPalette.GREEN, "range",
			util.select().fromTo(1, 1, 1, 7, 3, 7), 60);
		scene.overlay().showText(45)
			.text("Place the collector near the cannon's ejection area")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(55);

		for (int i = 0; i < 5; ++i) {
			scene.world().createItemEntity(util.vector().of(2.0 + i * 0.35, 2.4, 4.5),
				util.vector().of(0.08, 0.02, 0), new ItemStack(CBCItems.EMPTY_AUTOCANNON_CARTRIDGE.get()));
			scene.idle(5);
		}
		scene.overlay().showText(40)
			.text("Spent cartridges inside its range are collected automatically")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(50);

		scene.world().modifyBlock(collector, state -> state.setValue(SpentCasingCollectorBlock.FILL, 1), false);
		scene.overlay().showText(45)
			.text("Look through the top to check how full it is")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(55);

		scene.world().modifyBlock(collector, state -> state.setValue(SpentCasingCollectorBlock.FILL, 2), false);
		scene.overlay().showText(40)
			.text("A pile near the rim means it is almost full")
			.pointAt(util.vector().topOf(collector))
			.placeNearTarget();
		scene.idle(50);

		BlockPos arm = util.grid().at(6, 1, 4);
		SceneSupport.place(scene, util, arm, AllBlocks.MECHANICAL_ARM.getDefaultState());
		scene.overlay().showLine(PonderPalette.OUTPUT, util.vector().centerOf(collector), util.vector().centerOf(arm), 45);
		scene.overlay().showText(45)
			.text("Use a Mechanical Arm to remove the recovered casings")
			.pointAt(util.vector().centerOf(arm))
			.placeNearTarget();
		scene.idle(55);
		SceneSupport.finish(scene);
	}

	private static BlockState facing(BlockState state, Direction direction) {
		return state.setValue(DirectionalBlock.FACING, direction);
	}

	private static ItemStack filledAutocannonCartridge() {
		ItemStack cartridge = new ItemStack(CBCItems.AUTOCANNON_CARTRIDGE.get());
		AutocannonCartridgeItem.writeProjectile(cartridge, new ItemStack(CBCItems.AP_AUTOCANNON_ROUND.get()));
		return cartridge;
	}
}
