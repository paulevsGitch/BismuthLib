package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

public class BlockStateColors {
	private static final Map<BlockState, Color> COLORS = Maps.newHashMap();
	
	public static void load() {
		COLORS.clear();
		
		addColor(Blocks.GLOWSTONE, Color.YELLOW);
		addColor(Blocks.REDSTONE_TORCH, Color.RED.darker());
		
		Color lightBlue = new Color(0, 128, 255);
		addColor(Blocks.SOUL_FIRE, lightBlue);
		addColor(Blocks.SOUL_TORCH, lightBlue);
		addColor(Blocks.SOUL_WALL_TORCH, lightBlue);
		addColor(Blocks.SOUL_LANTERN, lightBlue);
		
		addColor(Fluids.LAVA, Color.ORANGE);
		addColor(Fluids.FLOWING_LAVA, Color.ORANGE);
	}
	
	public static Color getColor(BlockState state) {
		return COLORS.get(state);
	}
	
	public static void addColor(BlockState state, Color color) {
		COLORS.put(state, color);
	}
	
	public static void addColor(Block block, Color color) {
		block.getStateManager().getStates().forEach((state) -> addColor(state, color));
	}
	
	public static void addColor(Fluid fluid, Color color) {
		fluid.getStateManager().getStates().forEach((state) -> addColor(state.getBlockState(), color));
	}
}
