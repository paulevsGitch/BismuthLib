package ru.paulevs.bismuthlib.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import ru.paulevs.bismuthlib.data.info.LightInfo;
import ru.paulevs.bismuthlib.data.transformer.LightTransformer;

import java.util.HashMap;
import java.util.Map;

public class BlockLights {
	private static final Map<BlockState, LightTransformer> TRANSFORMERS = new HashMap<>();
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
	
	public static void addTransformer(BlockState state, LightTransformer transformer) {
		TRANSFORMERS.put(state, transformer);
	}
	
	public static void addTransformer(Block block, LightTransformer transformer) {
		block.getStateDefinition().getPossibleStates().forEach(state -> addTransformer(state, transformer));
	}
	
	public static LightTransformer getTransformer(BlockState state) {
		return TRANSFORMERS.get(state);
	}
	
	public static void clear() {
		LIGHTS.clear();
		TRANSFORMERS.clear();
	}
}
