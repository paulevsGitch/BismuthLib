package ru.paulevs.bismuthlib.data.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class LightInfo {
	protected int multiply(int color, float value) {
		int i1 = (int) (((color >> 16) & 255) * value) << 16;
		int i2 = (int) (((color >> 8) & 255) * value) << 8;
		int i3 = (int) ((color & 255) * value);
		return i1 | i2 | i3;
	}
	
	protected int reverse(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return b << 16 | g << 8 | r;
	}
	
	public abstract int getSimple(Level level, BlockPos pos, byte i);
	public abstract int getAdvanced(Level level, BlockPos pos, byte i);
	public abstract int getRadius();
}
