package com.cbcfirepowercomponents.content.large_autocannon_ammo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import rbasamoyai.createbigcannons.munitions.autocannon.config.AutocannonProjectilePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.autocannon.flak.FlakAutocannonProjectile;
import rbasamoyai.createbigcannons.munitions.autocannon.flak.FlakAutocannonProjectileProperties;
import rbasamoyai.createbigcannons.munitions.config.components.BallisticPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.EntityDamagePropertiesComponent;
import rbasamoyai.createbigcannons.munitions.config.components.ExplosionPropertiesComponent;
import rbasamoyai.createbigcannons.munitions.fragment_burst.ProjectileBurstParentPropertiesComponent;

public class LargeAutocannonHEProjectile extends FlakAutocannonProjectile {
	private static final FlakAutocannonProjectileProperties PROPERTIES = new FlakAutocannonProjectileProperties(
		new BallisticPropertiesComponent(-0.025d, 0.01d, false, 2.0f, 1.2f, 0.75f, 0.7f),
		new EntityDamagePropertiesComponent(10.0f, false, true, false, 0.75f),
		new AutocannonProjectilePropertiesComponent(2.0d, true),
		new ExplosionPropertiesComponent(1.2f, 4.0f),
		new ProjectileBurstParentPropertiesComponent(0.3d, 20));

	public LargeAutocannonHEProjectile(EntityType<? extends LargeAutocannonHEProjectile> type, Level level) {
		super(type, level);
	}

	@Override
	protected FlakAutocannonProjectileProperties getAllProperties() {
		return PROPERTIES;
	}
}
