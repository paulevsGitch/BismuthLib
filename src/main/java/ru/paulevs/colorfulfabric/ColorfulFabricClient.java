package ru.paulevs.colorfulfabric;

import java.awt.Color;
import java.util.Set;

import com.google.common.collect.Sets;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import ru.paulevs.colorfulfabric.storage.BlockStateColors;

public class ColorfulFabricClient implements ClientModInitializer {
	public static final Set<BlockPos> UPDATE = Sets.newHashSet();
	
	@Override
	public void onInitializeClient() {
		BlockStateColors.load();
		
		float[] hsb = new float[3];
		if (FabricLoader.getInstance().isModLoaded("lamps")) {
			for (DyeColor dye: DyeColor.values()) {
				Color color = new Color(dye.getMaterialColor().color);
				Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
				color = Color.getHSBColor(hsb[0], 1, 1);
				Block block = getBlock("lamps", dye.asString() + "_lamp");
				BlockStateColors.addColor(block, color);
			}
		}
	}
	
	private Block getBlock(String modID, String name) {
		return Registry.BLOCK.get(new Identifier(modID, name));
	}
}
