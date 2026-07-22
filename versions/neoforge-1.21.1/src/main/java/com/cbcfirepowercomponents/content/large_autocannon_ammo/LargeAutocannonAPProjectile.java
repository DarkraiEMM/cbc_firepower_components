package com.cbcfirepowercomponents.content.large_autocannon_ammo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.munitions.autocannon.ap_round.APAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.config.AutocannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.autocannon.config.InertAutocannonProjectileProperties;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;

public class LargeAutocannonAPProjectile extends APAutocannonProjectile {
	private static final InertAutocannonProjectileProperties PROPERTIES = new InertAutocannonProjectileProperties(
		new BallisticPropertiesComponent(-0.025d, 0.01d, false, 3.5f, 4.0f, 2.0f, 0.6f),
		new EntityDamagePropertiesComponent(18.0f, false, true, false, 0.75f),
		new AutocannonProjectilePropertiesComponent(2.0d, true));

	public LargeAutocannonAPProjectile(EntityType<? extends LargeAutocannonAPProjectile> type, Level level) {
		super(type, level);
	}

	@Override
	protected InertAutocannonProjectileProperties getAllProperties() {
		return PROPERTIES;
	}
}
