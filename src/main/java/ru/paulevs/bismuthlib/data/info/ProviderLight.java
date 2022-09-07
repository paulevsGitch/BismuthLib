package ru.paulevs.bismuthlib.data.info;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import ru.paulevs.bismuthlib.ColorMath;

public class ProviderLight extends LightInfo {
	private final float[] multipliers;
	private final BlockState state;
	private final BlockColor color;
	private final int radius;
	private final int index;
	
	public ProviderLight(BlockState state, BlockColor color, int index, int radius) {
		this.radius = radius;
		this.state = state;
		this.color = color;
		this.index = index;
		
		multipliers = new float[radius - 1];
		for (int i = 1; i < radius; i++) {
			multipliers[i - 1] = 1.0F - (float) i / radius;
		}
	}
	
	@Override
	public int getSimple(Level level, BlockPos pos, byte i) {
		int rgb = ColorMath.reverse(color.getColor(state, level, pos, index));
		if (i == 0) return rgb;
		return i < 7 ? ColorMath.multiply(rgb, 0.75F) : ColorMath.multiply(rgb, 0.29F);
	}
	
	@Override
	public int getAdvanced(Level level, BlockPos pos, byte i) {
		int rgb = ColorMath.reverse(color.getColor(state, level, pos, index));
		if (i == 0) return rgb;
		return ColorMath.multiply(rgb, multipliers[i - 1]);
	}
	
	@Override
	public int getRadius() {
		return radius;
	}
}
