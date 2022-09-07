package ru.paulevs.bismuthlib.data.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class LightInfo {
	public abstract int getSimple(Level level, BlockPos pos, byte i);
	public abstract int getAdvanced(Level level, BlockPos pos, byte i);
	public abstract int getRadius();
}
