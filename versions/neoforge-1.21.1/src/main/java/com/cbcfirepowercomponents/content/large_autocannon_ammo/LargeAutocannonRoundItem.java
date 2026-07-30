package com.cbcfirepowercomponents.content.large_autocannon_ammo;

import com.cbcfirepowercomponents.registry.MTEntityTypes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.index.CBCDataComponents;
import rbasamoyai.createbigcannons.index.CBCItems;
import rbasamoyai.createbigcannons.munitions.autocannon.AbstractAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoType;
import rbasamoyai.createbigcannons.munitions.autocannon.ap_round.APAutocannonRoundItem;
import rbasamoyai.createbigcannons.munitions.autocannon.config.AutocannonProjectilePropertiesComponent;

public class LargeAutocannonRoundItem extends APAutocannonRoundItem implements AutocannonAmmoItem {
	private static final AutocannonProjectilePropertiesComponent AP_PROPERTIES = new AutocannonProjectilePropertiesComponent(3.0d, false);
	private static final AutocannonProjectilePropertiesComponent HE_PROPERTIES = new AutocannonProjectilePropertiesComponent(3.5d, false);

	private final Kind kind;

	public LargeAutocannonRoundItem(Properties properties, Kind kind) {
		super(properties);
		this.kind = kind;
	}

	@Override
	public AbstractAutocannonProjectile getAutocannonProjectile(ItemStack stack, Level level) {
		return switch (this.kind) {
			case ARMOR_PIERCING -> MTEntityTypes.LARGE_AUTOCANNON_AP_PROJECTILE.get().create(level);
			case HIGH_EXPLOSIVE -> {
				LargeAutocannonHEProjectile projectile = MTEntityTypes.LARGE_AUTOCANNON_HE_PROJECTILE.get().create(level);
				if (projectile != null)
					projectile.setFuze(CBCItems.IMPACT_FUZE.asStack());
				yield projectile;
			}
		};
	}

	@Override
	public EntityType<?> getEntityType(ItemStack stack) {
		return switch (this.kind) {
			case ARMOR_PIERCING -> MTEntityTypes.LARGE_AUTOCANNON_AP_PROJECTILE.get();
			case HIGH_EXPLOSIVE -> MTEntityTypes.LARGE_AUTOCANNON_HE_PROJECTILE.get();
		};
	}

	@Override
	public AutocannonProjectilePropertiesComponent getAutocannonProperties(ItemStack stack) {
		return this.kind == Kind.ARMOR_PIERCING ? AP_PROPERTIES : HE_PROPERTIES;
	}

	@Override
	public boolean isTracer(ItemStack stack) {
		return stack.getOrDefault(CBCDataComponents.AUTOCANNON_TRACER, false);
	}

	public boolean isIncendiary(ItemStack stack) {
		return false;
	}

	@Override
	public void setTracer(ItemStack stack, boolean tracer) {
		if (tracer)
			stack.set(CBCDataComponents.AUTOCANNON_TRACER, true);
		else
			stack.remove(CBCDataComponents.AUTOCANNON_TRACER);
	}

	@Override
	public ItemStack getSpentItem(ItemStack stack) {
		return CBCItems.EMPTY_AUTOCANNON_CARTRIDGE.asStack();
	}

	@Override
	public AutocannonAmmoType getType() {
		return AutocannonAmmoType.AUTOCANNON;
	}

	public enum Kind {
		ARMOR_PIERCING,
		HIGH_EXPLOSIVE
	}
}
