package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class LightSourceInfo {
	private final BlockState state;
	private final BlockPos pos;
	private final Color color;
	
	public LightSourceInfo(BlockPos pos, BlockState state, Color color) {
		this.pos = pos;
		this.state = state;
		this.color = color;
	}
	
	public BlockState getState() {
		return state;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Color getColor() {
		return color;
	}
	
	@Override
	public int hashCode() {
		return pos.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		LightSourceInfo info = (LightSourceInfo) obj;
		return info == null ? false : info.pos.equals(pos);
	}
}
