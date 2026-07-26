package com.cbcfirepowercomponents.compat.physics;

import java.util.OptionalDouble;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

interface PhysicalDistanceResolver {
	OptionalDouble distance(Level level, Position from, Position to);
}
