package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.Registry;
import ru.paulevs.colorfulfabric.mixin.SpriteAccessor;

public class BlockStateColors {
	private static final Map<BlockState, Color> COLORS = Maps.newHashMap();
	private static boolean load = true;
	
	public static void load() {
		if (load) {
			load = false;
			float[] hsv = new float[3];
			BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
			Registry.BLOCK.forEach((block) -> {
				block.getStateManager().getStates().forEach((state) -> {
					if (state.getLuminance() > 0 && !COLORS.containsKey(state)) {
						Color color = getBlockColor(manager, state);
						Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
						if (hsv[1] > 0.2F) {
							float s = hsv[1] * 4;
							color = Color.getHSBColor(hsv[0], s > 1 ? 1 : s, 1);
						}
						addColor(state, color);
					}
				});
			});
		}
	}
	
	public static void addVanillaLights() {
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
	
	private static Color getBlockColor(BlockRenderManager manager, BlockState state) {
		BakedModel model = manager.getModel(state);
		if (model != null) {
			SpriteAccessor sprite = (SpriteAccessor) model.getSprite();
			if (sprite != null) {
				NativeImage[] images = sprite.cfGetImages();
				if (images != null && images.length > 0) {
					NativeImage img = images[0];
					return getAverageBright(img);
				}
			}
		}
		return new Color(state.getMaterial().getColor().color);
	}
	
	private static Color getAverageBright(NativeImage img) {
		long cr = 0;
		long cg = 0;
		long cb = 0;
		long count = 0;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int abgr = img.getPixelColor(x, y);
				int r = abgr & 255;
				int g = (abgr >> 8) & 255;
				int b = (abgr >> 16) & 255;
				if (r > 200 || g > 200 || b > 200) {
					cr += r;
					cg += g;
					cb += b;
					count ++;
				}
			}
		}
		return count < 1 ? Color.DARK_GRAY : new Color((int) (cr / count), (int) (cg / count), (int) (cb / count));
	}
}
