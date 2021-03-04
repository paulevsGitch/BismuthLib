package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ru.paulevs.colorfulfabric.mixin.SpriteAccessor;

public class BlockStateColors {
	private static final Map<BlockState, Color> COLORS = Maps.newHashMap();
	private static boolean load = true;
	
	public static void load() {
		if (load) {
			load = false;
			
			Gson gson = new Gson();
			FabricLoader.getInstance().getAllMods().forEach((mod) -> {
				String id = mod.getMetadata().getId();
				JsonObject json = loadJson("/data/" + id + "/color_lights.json", gson);
				if (json != null) {
					JsonArray array = json.getAsJsonArray("lights");
					if (array != null) {
						array.forEach((element) -> {
							JsonObject obj = element.getAsJsonObject();
							if (obj != null) {
								String blockName = obj.get("block").getAsString();
								JsonArray colorArray = obj.get("color").getAsJsonArray();
								String stateString = null;
								if (obj.has("state")) {
									stateString = obj.get("state").getAsString();
								}
								
								Block block = Registry.BLOCK.get(new Identifier(blockName));
								if (block != null) {
									Color color = new Color(colorArray.get(0).getAsInt(), colorArray.get(1).getAsInt(), colorArray.get(2).getAsInt());
									StateManager<Block, BlockState> manager = block.getStateManager();
									if (stateString == null) {
										manager.getStates().forEach((state) -> {
											if (state.getLuminance() > 0) {
												COLORS.put(state, color);
											}
										});
									}
									else {
										BlockState blockState = block.getDefaultState();
										String[] states = stateString.split(",");
										for (String state: states) {
											int separator = state.indexOf('=');
											String name = state.substring(0, separator);
											String value = state.substring(separator + 1);
											Property<?> property = manager.getProperty(name);
											if (property != null) {
												blockState = appendProperty(blockState, property, value);
											}
										}
										if (blockState.getLuminance() > 0) {
											COLORS.put(blockState, color);
										}
									}
								}
							}
						});
					}
				}
			});
			
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
						COLORS.put(state, color);
					}
				});
			});
		}
	}

	private static <T extends Comparable<T>> BlockState appendProperty(BlockState state, Property<T> property, String valueString) {
		Optional<T> optional = property.parse(valueString);
		if (optional.isPresent()) {
			state = (BlockState) state.with(property, optional.get());
		}
		return state;
	}
	
	public static Color getColor(BlockState state) {
		return COLORS.get(state);
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
	
	private static JsonObject loadJson(String path, Gson gson) {
		try {
			InputStream stream = BlockStateColors.class.getResourceAsStream(path);
			if (stream != null) {
				InputStreamReader reader = new InputStreamReader(stream);
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				reader.close();
				stream.close();
				return obj;
			}
		}
		catch (JsonSyntaxException | JsonIOException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
