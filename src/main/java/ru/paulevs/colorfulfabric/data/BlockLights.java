package ru.paulevs.colorfulfabric.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import ru.paulevs.colorfulfabric.data.info.LightInfo;

import java.util.HashMap;
import java.util.Map;

public class BlockLights {
	private static final Map<BlockState, Integer> TRANSFORMERS = new HashMap<>();
	private static final Map<BlockState, LightInfo> LIGHTS = new HashMap<>();
	
	public static void addLight(BlockState state, LightInfo light) {
		if (light == null) {
			LIGHTS.remove(state);
		}
		else {
			LIGHTS.put(state, light);
		}
	}
	
	public static void addLight(Block block, LightInfo light) {
		block.getStateDefinition().getPossibleStates().forEach(state -> addLight(state, light));
	}
	
	public static LightInfo getLight(BlockState state) {
		return LIGHTS.get(state);
	}
	
	public static void addTransformer(BlockState state, int color) {
		TRANSFORMERS.put(state, reverse(color));
	}
	
	public static void addTransformer(Block block, int color) {
		block.getStateDefinition().getPossibleStates().forEach(state -> addTransformer(state, color));
	}
	
	public static int getTransformer(BlockState state) {
		return TRANSFORMERS.getOrDefault(state, 0);
	}
	
	public static void clear() {
		LIGHTS.clear();
		TRANSFORMERS.clear();
	}
	
	private static int reverse(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return b << 16 | g << 8 | r;
	}
	
	/*public static List<String> lightsAsText() {
		List<String> result = new ArrayList<>(LIGHTS.size());
		LIGHTS.forEach((state, light) -> {
			int bgr = light.getSimple((byte) 0);
			int b = (bgr >> 16) & 255;
			int g = (bgr >> 8) & 255;
			int r = bgr & 255;
			String color = Integer.toHexString(r << 16 | g << 8 | b);
			while (color.length() < 6) {
				color = "0" + color;
			}
			result.add(state.toString() + " = " + color);
		});
		Collections.sort(result);
		return result;
	}*/
}
