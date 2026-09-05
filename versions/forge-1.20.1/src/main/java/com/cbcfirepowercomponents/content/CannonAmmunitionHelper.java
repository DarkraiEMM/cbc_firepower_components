package com.cbcfirepowercomponents.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonCartridgeItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlockItem;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCannonPropellantBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.propellant.BigCartridgeBlockItem;
import rbasamoyai.createbigcannons.munitions.fuzes.FuzeItem;

public final class CannonAmmunitionHelper {
	private static final TagKey<Item> MW_MEDIUM_CANNON_CARTRIDGES = TagKey.create(Registries.ITEM,
		new ResourceLocation("cbcmodernwarfare", "mediumcannon_cartridges"));
	private static final TagKey<Item> MW_SPENT_MEDIUM_CANNON_CASINGS = TagKey.create(Registries.ITEM,
		new ResourceLocation("cbcmodernwarfare", "spent_mediumcannon_casings"));
	private static final TagKey<Item> CBC_AUTOCANNON_ROUNDS = TagKey.create(Registries.ITEM,
		new ResourceLocation("createbigcannons", "autocannon_rounds"));
	private static final TagKey<Item> CBC_BIG_CANNON_PROJECTILES = TagKey.create(Registries.ITEM,
		new ResourceLocation("createbigcannons", "big_cannon_projectiles"));
	private static final TagKey<Item> MW_MEDIUM_CANNON_ROUNDS = TagKey.create(Registries.ITEM,
		new ResourceLocation("cbcmodernwarfare", "mediumcannon_rounds"));

	private CannonAmmunitionHelper() {}

	public static boolean isProjectile(ItemStack stack) {
		return stack.getItem() instanceof ProjectileBlockItem
			|| stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ProjectileBlock
			|| stack.is(CBC_BIG_CANNON_PROJECTILES)
			|| stack.is(MW_MEDIUM_CANNON_ROUNDS);
	}

	public static boolean isLoadReadyProjectile(ItemStack stack) {
		return isProjectile(stack);
	}

	/**
	 * Ammunition which already contains its propellant. CBC autocannon rounds,
	 * and CBCMW medium-cannon cartridges use this path and therefore occupy one
	 * ready-rack group without a second item.
	 */
	public static boolean isSelfContainedRound(ItemStack stack) {
		if (stack.isEmpty())
			return false;
		if (stack.getItem() instanceof AutocannonCartridgeItem)
			return AutocannonCartridgeItem.hasProjectile(stack);
		return stack.getItem() instanceof rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem
			|| stack.is(CBC_AUTOCANNON_ROUNDS)
			|| stack.is(MW_MEDIUM_CANNON_CARTRIDGES);
	}

	public static boolean isReadyAmmunition(ItemStack stack) {
		return isLoadReadyProjectile(stack) || isSelfContainedRound(stack);
	}

	public static boolean requiresSeparatePropellant(ItemStack stack) {
		return isLoadReadyProjectile(stack) && !isSelfContainedRound(stack);
	}

	public static boolean isPropellant(ItemStack stack) {
		if (!(stack.getItem() instanceof BlockItem blockItem))
			return false;
		Block block = blockItem.getBlock();
		if (block instanceof BigCartridgeBlock)
			return stack.getItem() instanceof BigCartridgeBlockItem && BigCartridgeBlockItem.getPower(stack) > 0;
		return block instanceof BigCannonPropellantBlock;
	}

	public static boolean isFuze(ItemStack stack) {
		return stack.getItem() instanceof FuzeItem;
	}

	public static boolean canApplyFuze(ItemStack projectile) {
		return projectile.getItem() instanceof BlockItem blockItem
			&& blockItem.getBlock() instanceof FuzedProjectileBlock
			&& FuzedProjectileBlock.getFuzeFromItemStack(projectile).isEmpty();
	}

	public static void applyFuze(ItemStack projectile, ItemStack fuze) {
		ItemStack storedFuze = fuze.copy();
		storedFuze.setCount(1);
		CompoundTag blockEntityTag = projectile.getOrCreateTagElement("BlockEntityTag");
		blockEntityTag.put("Fuze", storedFuze.save(new CompoundTag()));
	}

	public static boolean wasAccepted(ItemStack original, ItemStack remainder) {
		return !ItemStack.matches(remainder, original) || remainder.getCount() != original.getCount();
	}

	public static ItemStack copyWithCount(ItemStack stack, int count) {
		if (stack.isEmpty() || count <= 0)
			return ItemStack.EMPTY;
		ItemStack copy = stack.copy();
		copy.setCount(count);
		return copy;
	}

	public static boolean isEmptyBigCartridge(ItemStack stack) {
		return stack.getItem() instanceof BigCartridgeBlockItem && BigCartridgeBlockItem.getPower(stack) <= 0;
	}

	public static ItemStack emptyBigCartridge() {
		return BigCartridgeBlockItem.getWithPower(0);
	}

	public static boolean isEmptyAutocannonCartridge(ItemStack stack) {
		return stack.is(CBCItems.EMPTY_AUTOCANNON_CARTRIDGE.get());
	}

	public static boolean isSpentCasing(ItemStack stack) {
		return isEmptyBigCartridge(stack) || isEmptyAutocannonCartridge(stack)
			|| stack.is(MW_SPENT_MEDIUM_CANNON_CASINGS)
			|| stack.getItem() instanceof AutocannonCartridgeItem && !AutocannonCartridgeItem.hasProjectile(stack);
	}

	public static boolean sameRound(ItemStack projectileA, ItemStack propellantA,
									ItemStack projectileB, ItemStack propellantB) {
		return ItemStack.isSameItemSameTags(projectileA, projectileB)
			&& ItemStack.isSameItemSameTags(propellantA, propellantB);
	}
}
