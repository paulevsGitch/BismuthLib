package ru.paulevs.bismuthlib.data.transformer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import ru.paulevs.bismuthlib.ColorMath;

public class SimpleLightTransformer extends LightTransformer {
	private final int color;
	
	public SimpleLightTransformer(int color) {
		this.color = ColorMath.reverse(color);
	}
	
	@Override
	public int getColor(Level level, BlockPos pos) {
		return color;
	}
}
