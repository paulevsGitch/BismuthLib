package ru.paulevs.bismuthlib.data.transformer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class LightTransformer {
	public abstract int getColor(Level level, BlockPos pos);
}
