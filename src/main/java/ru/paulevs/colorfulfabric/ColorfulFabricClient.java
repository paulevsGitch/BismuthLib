package ru.paulevs.colorfulfabric;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import ru.paulevs.colorfulfabric.commands.PrintCommand;
import ru.paulevs.colorfulfabric.data.BlockLights;
import ru.paulevs.colorfulfabric.data.LevelShaderData;
import ru.paulevs.colorfulfabric.data.LightInfo;
import ru.paulevs.colorfulfabric.gui.CFOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ColorfulFabricClient implements ClientModInitializer {
	public static final String MOD_ID = "colorfulfabric";
	
	private static final ResourceLocation LIGHTMAP_ID = new ResourceLocation(MOD_ID, "colored_light");
	private static LevelShaderData data;
	
	private static boolean fastLight = false;
	private static boolean modifyLight = false;
	
	@Override
	public void onInitializeClient() {
		/*for (DyeColor color: DyeColor.values()) {
			Block block = Registry.BLOCK.get(new ResourceLocation(color.getName() + "_stained_glass"));
			BlockLights.addTransformer(block, color.getMaterialColor().col);
		}*/
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			int x1 = 0;
			int y1 = 0;
			int x2 = 100;
			int y2 = 100;
			
			HudRenderCallback.EVENT.register((matrixStack, delta) -> {
				Matrix4f matrix = matrixStack.last().pose();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
				RenderSystem.setShaderTexture(0, LIGHTMAP_ID);
				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				
				bufferBuilder.vertex(matrix, x1, y2, 0).uv(0.0F, 1.0F).endVertex();
				bufferBuilder.vertex(matrix, x2, y2, 0).uv(1.0F, 1.0F).endVertex();
				bufferBuilder.vertex(matrix, x2, y1, 0).uv(1.0F, 0.0F).endVertex();
				bufferBuilder.vertex(matrix, x1, y1, 0).uv(0.0F, 0.0F).endVertex();
				
				BufferUploader.drawWithShader(bufferBuilder.end());
			});
		}
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PrintCommand.register(dispatcher);
		});
		
		final Gson gson = new GsonBuilder().create();
		final ResourceLocation location = new ResourceLocation(MOD_ID, "resource_reloader");
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return location;
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				LightInfo[] white = new LightInfo[15];
				for (byte i = 0; i < 15; i++) {
					int c = (i + 1) << 4;
					white[i] = new LightInfo(c << 16 | c << 8 | c, i + 1);
				}
				
				Registry.BLOCK.forEach(block -> {
					block.getStateDefinition().getPossibleStates().stream().filter(state -> state.getLightEmission() > 0).forEach(state -> {
						BlockLights.addLight(state, white[state.getLightEmission() - 1]);
					});
				});
				
				Map<ResourceLocation, Resource> list = resourceManager.listResources("lights", resourceLocation ->
					resourceLocation.getPath().endsWith(".json") && resourceLocation.getNamespace().equals(MOD_ID)
				);
				list.forEach((id, resource) -> {
					JsonObject obj = new JsonObject();
					
					try {
						BufferedReader reader = resource.openAsReader();
						obj = gson.fromJson(reader, JsonObject.class);
						reader.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					
					final JsonObject storage = obj;
					storage.keySet().forEach(key -> {
						ResourceLocation blockID = new ResourceLocation(key);
						Block block = Registry.BLOCK.get(blockID);
						if (block != null) {
							JsonObject data = storage.getAsJsonObject(key);
							if (data.keySet().isEmpty()) {
								BlockLights.addLight(block, null);
							}
							else {
								int color = Integer.parseInt(data.get("color").getAsString(), 16);
								JsonElement preRadius = data.get("radius");
								if (preRadius.isJsonPrimitive()) {
									int radius = preRadius.getAsInt();
									BlockLights.addLight(block, new LightInfo(color, radius));
								}
								else {
									data = preRadius.getAsJsonObject();
									String type = data.get("type").getAsString();
									ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
									
									if (type.equals("property")) {
										final JsonObject values = data.getAsJsonObject("values");
										values.keySet().forEach(k -> {
											int radius = values.get(k).getAsInt();
											String[] pair = k.split("=");
											states.forEach(state -> {
												BlockLights.addLight(state, null);
												if (hasPropertyWithValue(state, pair[0], pair[1])) {
													BlockLights.addLight(state, new LightInfo(color, radius));
												}
											});
										});
									}
									else if (type.equals("state")) {
										final JsonObject values = data.getAsJsonObject("values");
										values.keySet().forEach(stateString -> {
											int radius = values.get(stateString).getAsInt();
											Map<String, String> propValues = new HashMap<>();
											Arrays.stream(stateString.split(",")).forEach(entry -> {
												String[] pair = entry.split("=");
												propValues.put(pair[0], pair[1]);
											});
											states.forEach(state -> {
												BlockLights.addLight(state, null);
												AtomicBoolean add = new AtomicBoolean(true);
												propValues.forEach((name, val) -> {
													if (!hasPropertyWithValue(state, name, val)) {
														add.set(false);
													}
												});
												if (add.get()) {
													BlockLights.addLight(state, new LightInfo(color, radius));
												}
											});
										});
									}
								}
							}
						}
					});
				});
			}
		});
	}
	
	private boolean hasPropertyWithValue(BlockState state, String propertyName, String propertyValue) {
		Collection<Property<?>> properties = state.getProperties();
		Iterator<Property<?>> iterator = properties.iterator();
		boolean result = false;
		while (iterator.hasNext()) {
			Property<?> property = iterator.next();
			if (property.getName().equals(propertyName) && state.getValue(property).toString().equals(propertyValue)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public static void initData() {
		int w = CFOptions.getMapRadiusXZ();
		int h = CFOptions.getMapRadiusY();
		int count = CFOptions.getThreadCount();
		
		if (data == null) {
			data = new LevelShaderData(w, h, count);
			Minecraft.getInstance().getTextureManager().register(LIGHTMAP_ID, data.getTexture());
		}
		else if (data.getDataWidth() != w || data.getDataHeight() != h || count != data.getThreadCount()) {
			data.dispose();
			data = new LevelShaderData(w, h, count);
			Minecraft.getInstance().getTextureManager().register(LIGHTMAP_ID, data.getTexture());
		}
		
		boolean fast = CFOptions.isFastLight();
		boolean modify = CFOptions.modifyColor();
		if (fast != fastLight || modify != modifyLight) {
			fastLight = fast;
			modifyLight = modify;
			data.resetAll();
		}
	}
	
	public static void update(Level level, int cx, int cy, int cz) {
		data.update(level, cx, cy, cz);
	}
	
	public static void updateSection(int cx, int cy, int cz) {
		data.markToUpdate(cx, cy, cz);
	}
	
	public static void bindWithUniforms() {
		RenderSystem.setShaderTexture(7, LIGHTMAP_ID);
		ShaderInstance shader = RenderSystem.getShader();
		
		Uniform uniform = shader.getUniform("playerSectionPos");
		if (uniform != null) {
			BlockPos center = data.getCenter();
			uniform.set(center.getX(), center.getY(), center.getZ());
		}
		
		uniform = shader.getUniform("dataScale");
		if (uniform != null) {
			uniform.set(data.getDataWidth(), data.getDataHeight());
		}
		
		uniform = shader.getUniform("dataSide");
		if (uniform != null) {
			uniform.set(data.getDataSide());
		}
		
		uniform = shader.getUniform("fastLight");
		if (uniform != null) {
			uniform.set(fastLight ? 1 : 0);
		}
	}
}
