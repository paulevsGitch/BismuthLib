package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import ru.paulevs.colorfulfabric.mixin.SpriteAccessor;

public class BlockStateColors {
	private static final Map<BlockState, Color> COLORS = Maps.newHashMap();
	private static boolean load = true;
	
	public static void load() {
		if (load) {
			load = false;
			
			Gson gson = new Gson();
			Set<Item> items = Sets.newHashSet();
			boolean[] addBlock = new boolean[] { false };
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
									addBlock[0] = false;
									if (stateString == null) {
										manager.getStates().forEach((state) -> {
											if (state.getLuminance() > 0) {
												COLORS.put(state, color);
												addBlock[0] = true;
											}
										});
									}
									else {
										String[] states = stateString.split(",");
										Map<String, String> values = Maps.newHashMap();
										for (String state: states) {
											int separator = state.indexOf('=');
											String name = state.substring(0, separator);
											String value = state.substring(separator + 1);
											values.put(name, value);
										}
										
										manager.getStates().forEach((state) -> {
											if (state.getLuminance() > 0) {
												Iterator<Property<?>> iterator = state.getProperties().iterator();
												boolean add = true;
												while (iterator.hasNext()) {
													Property<?> property = iterator.next();
													String prop = property.getName();
													if (values.containsKey(prop)) {
														String value = state.get(property).toString();
														if (!values.get(prop).equals(value)) {
															add = false;
														}
													}
												}
												if (add) {
													COLORS.put(state, color);
													addBlock[0] = true;
												}
											}
										});
										
										if (addBlock[0] && block.asItem() != Items.AIR) {
											items.add(block.asItem());
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
					boolean add = false;
					if (state.getLuminance() > 0 && !COLORS.containsKey(state)) {
						Color color = getBlockColor(manager, state);
						Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
						if (hsv[2] > 0.3) {
							float s = hsv[1] * 4;
							color = Color.getHSBColor(hsv[0], s > 1 ? 1 : s, 1);
							COLORS.put(state, color);
							add = true;
						}
					}
					if (add && block.asItem() != Items.AIR) {
						items.add(block.asItem());
					}
				});
			});
			
			int index = 0;
			ItemStack[] itemStacks = new ItemStack[items.size()];
			for (Item item: items) {
				itemStacks[index++] = new ItemStack(item);
			}
			Arrays.sort(itemStacks, (stack1, stack2) -> {
				String name1 = Registry.ITEM.getId(stack1.getItem()).toString();
				String name2 = Registry.ITEM.getId(stack2.getItem()).toString();
				return name1.compareToIgnoreCase(name2);
			});
			
			new ItemGroup(ItemGroup.GROUPS.length - 1, String.format("%s.%s", "colorfulfabric", "lights")) {
				Random random = new Random(0);
				private long time;
				private int index;
				
				@Override
				public ItemStack getIcon() {
					long time2 = System.currentTimeMillis();
					if (time2 > time) {
						time = time2 + 500;
						index = random.nextInt(itemStacks.length);
					}
					return itemStacks[index];
				}

				@Override
				public void appendStacks(DefaultedList<ItemStack> stacks) {
					for (ItemStack item: itemStacks) {
						stacks.add(item);
					}
					super.appendStacks(stacks);
				}

				@Override
				public ItemStack createIcon() {
					return ItemStack.EMPTY;
				}
			};
		}
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
