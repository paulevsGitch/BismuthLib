package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class BlockStateColors {
	private static final Map<BlockState, Color> COLORS = Maps.newHashMap();
	
	public static void load() {
		COLORS.clear();
		
		Color lightBlue = new Color(255, 128, 0);
		addColor(Blocks.GLOWSTONE, Color.YELLOW);
		addColor(Blocks.REDSTONE_TORCH, Color.RED.darker());
		
		addColor(Blocks.SOUL_FIRE, lightBlue);
		addColor(Blocks.SOUL_TORCH, lightBlue);
		addColor(Blocks.SOUL_LANTERN, lightBlue);
		
		addColor(Blocks.LAVA, Color.RED);
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
}
