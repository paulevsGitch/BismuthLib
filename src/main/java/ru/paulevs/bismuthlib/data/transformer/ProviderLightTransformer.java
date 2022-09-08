package ru.paulevs.bismuthlib.data.transformer;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ProviderLightTransformer extends LightTransformer {
	private final BlockState state;
	private final BlockColor color;
	private final int index;
	
	public ProviderLightTransformer(BlockState state, BlockColor color, int index) {
		this.state = state;
		this.color = color;
		this.index = index;
	}
	
	@Override
	public int getColor(Level level, BlockPos pos) {
		return color.getColor(state, level, pos, index);
	}
}
