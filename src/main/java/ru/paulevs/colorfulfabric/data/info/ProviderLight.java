package ru.paulevs.colorfulfabric.data.info;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ProviderLight extends LightInfo {
	private final BlockState state;
	private final BlockColor color;
	private final int radius;
	private final int index;
	
	public ProviderLight(BlockState state, BlockColor color, int index, int radius) {
		this.radius = radius;
		this.state = state;
		this.color = color;
		this.index = index;
	}
	
	@Override
	public int getSimple(Level level, BlockPos pos, byte i) {
		return color.getColor(state, level, pos, index);
	}
	
	@Override
	public int getAdvanced(Level level, BlockPos pos, byte i) {
		return color.getColor(state, level, pos, index);
	}
	
	@Override
	public int getRadius() {
		return radius;
	}
}
