package ru.paulevs.bismuthlib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import ru.paulevs.bismuthlib.data.BlockLights;
import ru.paulevs.bismuthlib.data.info.LightInfo;
import ru.paulevs.bismuthlib.data.info.SimpleLight;
import ru.paulevs.bismuthlib.gui.CFOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightPropagator {
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final byte MASK_OFFSET = 17;
	private static final BlockPos[] OFFSETS;
	
	private final List<Set<BlockPos>> buffers = new ArrayList<>(2);
	private final MutableBlockPos[] positions = new MutableBlockPos[35937];
	private final Set<TransformerInfo> transformers = new HashSet<>();
	private final boolean[] mask = new boolean[35937];
	MutableBlockPos pos = new MutableBlockPos();
	
	public LightPropagator() {
		buffers.add(new HashSet<>());
		buffers.add(new HashSet<>());
		for (int i = 0; i < positions.length; i++) {
			positions[i] = new MutableBlockPos();
		}
	}
	
	public void fastLight(Level level, BlockPos sectionPos, int[] data) {
		if (sectionPos.getY() < level.getMinSection() || sectionPos.getY() > level.getMaxSection()) return;
		LevelChunk chunk = level.getChunk(sectionPos.getX(), sectionPos.getZ());
		if (chunk.getPos().x != sectionPos.getX() || chunk.getPos().z != sectionPos.getZ()) return;
		LevelChunkSection section = chunk.getSection(sectionPos.getY() - level.getMinSection());
		if (section == null) return;
		
		MutableBlockPos pos = positions[0];
		MutableBlockPos pos2 = positions[1];
		
		for (byte x = 0; x < 16; x++) {
			pos.setX(sectionPos.getX() << 4 | x);
			for (byte y = 0; y < 16; y++) {
				pos.setY(sectionPos.getY() << 4 | y);
				for (byte z = 0; z < 16; z++) {
					pos.setZ(sectionPos.getZ() << 4 | z);
					int index = x << 8 | y << 4 | z;
					data[index] = ColorMath.ALPHA;
					
					BlockState state = section.getBlockState(x, y, z);
					LightInfo light = BlockLights.getLight(state);
					
					if (light != null) {
						data[index] = light.getSimple(level, pos, (byte) 0) | ColorMath.ALPHA;
						continue;
					}
					
					if (state.useShapeForLightOcclusion()) {
						if (state.isCollisionShapeFullBlock(level, pos)) {
							continue;
						}
					}
					
					for (byte i = 0; i < 27; i++) {
						pos2.set(pos).move(OFFSETS[i]);
						state = level.getBlockState(pos2);
						light = BlockLights.getLight(state);
						if (light != null) {
							data[index] = ColorMath.maxBlend(data[index], light.getSimple(level, pos, i)) | ColorMath.ALPHA;
						}
					}
				}
			}
		}
	}
	
	public void advancedLight(Level level, BlockPos sectionPos, int[] data) {
		if (sectionPos.getY() < level.getMinSection() || sectionPos.getY() > level.getMaxSection()) return;
		LevelChunk chunk = level.getChunk(sectionPos.getX(), sectionPos.getZ());
		if (chunk.getPos().x != sectionPos.getX() || chunk.getPos().z != sectionPos.getZ()) return;
		LevelChunkSection section = chunk.getSection(sectionPos.getY() - level.getMinSection());
		if (section == null) return;
		
		BlockPos secMin = new BlockPos(
			sectionPos.getX() << 4,
			sectionPos.getY() << 4,
			sectionPos.getZ() << 4
		);
		BlockPos secMax = secMin.offset(16, 16, 16);
		
		int x1 = secMin.getX() - 16;
		int y1 = secMin.getY() - 16;
		int z1 = secMin.getZ() - 16;
		int x2 = x1 + 48;
		int y2 = y1 + 48;
		int z2 = z1 + 48;
		
		Arrays.fill(data, ColorMath.ALPHA);
		
		for (int x = x1; x < x2; x++) {
			pos.setX(x);
			for (int y = y1; y < y2; y++) {
				pos.setY(y);
				for (int z = z1; z < z2; z++) {
					pos.setZ(z);
					BlockState state = level.getBlockState(pos);
					LightInfo light = BlockLights.getLight(state);
					if (light != null && canAffect(pos, light.getRadius(), secMin, secMax)) {
						//fastFillLight(data, pos, light, secMin, secMax);
						fillLight(level, data, pos, light, secMin, secMax, CFOptions.modifyColor());
					}
				}
			}
		}
		
		transformers.forEach(info -> fillLight(level, data, info.pos, info.light, secMin, secMax, false));
		transformers.clear();
	}
	
	private boolean canAffect(BlockPos lightPos, int radius, BlockPos secMin, BlockPos secMax) {
		if (lightPos.getX() + radius < secMin.getX() || lightPos.getX() - radius > secMax.getX()) return false;
		if (lightPos.getY() + radius < secMin.getY() || lightPos.getY() - radius > secMax.getY()) return false;
		if (lightPos.getZ() + radius < secMin.getZ() || lightPos.getZ() - radius > secMax.getZ()) return false;
		return true;
	}
	
	private void fastFillLight(Level level, int[] data, BlockPos pos, LightInfo info, BlockPos secMin, BlockPos secMax) {
		int radius = info.getRadius();
		MutableBlockPos p = positions[0];
		for (int i = -radius; i <= radius; i++) {
			p.setX(pos.getX() + i);
			for (int j = -radius; j <= radius; j++) {
				p.setY(pos.getY() + j);
				for (int k = -radius; k <= radius; k++) {
					p.setZ(pos.getZ() + k);
					int dist = Math.abs(i) + Math.abs(j) + Math.abs(k);
					if (dist >= radius) continue;
					setLight(data, p, info.getAdvanced(level, pos, (byte) dist), secMin, secMax, true);
				}
			}
		}
	}
	
	private void fillLight(Level level, int[] data, BlockPos pos, LightInfo info, BlockPos secMin, BlockPos secMax, boolean modify) {
		Arrays.fill(mask, false);
		mask[getMaskIndex(MASK_OFFSET, MASK_OFFSET, MASK_OFFSET)] = true;
		
		int color = info.getAdvanced(level, pos, (byte) 0);
		setLight(data, pos, color, secMin, secMax, true);
		
		buffers.get(0).add(pos);
		byte radius = (byte) info.getRadius();
		byte bufferIndex = 0;
		
		for (byte i = 1; i < radius; i++) {
			Set<BlockPos> starts = buffers.get(bufferIndex);
			bufferIndex = (byte) ((bufferIndex + 1) & 1);
			Set<BlockPos> ends = buffers.get(bufferIndex);
			color = info.getAdvanced(level, pos, i);
			
			for (BlockPos start: starts) {
				for (Direction offset: DIRECTIONS) {
					byte maskX = (byte) (start.getX() - pos.getX() + offset.getStepX());
					byte maskY = (byte) (start.getY() - pos.getY() + offset.getStepY());
					byte maskZ = (byte) (start.getZ() - pos.getZ() + offset.getStepZ());
					
					if (maskX < -radius || maskY < -radius || maskZ < -radius || maskX > radius || maskY > radius || maskZ > radius) continue;
					
					maskX += MASK_OFFSET;
					maskY += MASK_OFFSET;
					maskZ += MASK_OFFSET;
					
					int maskIndex = getMaskIndex(maskX, maskY, maskZ);
					if (mask[maskIndex]) continue;
					
					BlockPos p = positions[maskIndex].set(start).move(offset);
					BlockState state = level.getBlockState(p);
					
					int transformer = BlockLights.getTransformer(state);
					if (modify && transformer != ColorMath.WHITE) {
						int mixedColor = ColorMath.mulBlend(color, transformer);
						transformers.add(new TransformerInfo(new SimpleLight(mixedColor, radius - i, false), p.immutable()));
						mask[maskIndex] = true;
						continue;
					}
					else if (BlockLights.getLight(state) != null) {
						mask[maskIndex] = true;
						continue;
					}
					else if (blockFace(state, level, p, offset) && blockLight(state, level, p)) {
						continue;
					}
					
					mask[maskIndex] = true;
					setLight(data, p, color, secMin, secMax, true);
					ends.add(p);
				}
			}
			starts.clear();
		}
		
		buffers.get(0).clear();
		buffers.get(1).clear();
	}
	
	private boolean blockFace(BlockState state, Level level, BlockPos pos, Direction dir) {
		return state.isFaceSturdy(level, pos, dir) || state.isFaceSturdy(level, pos, dir.getOpposite());
	}
	
	private boolean blockLight(BlockState state, Level level, BlockPos pos) {
		return state.getMaterial().isSolidBlocking() || !state.propagatesSkylightDown(level, pos);
	}
	
	private static int getMaskIndex(byte x, byte y, byte z) {
		return x * 1089 + y * 33 + z;
	}
	
	private void setLight(int[] data, BlockPos pos, int light, BlockPos secMin, BlockPos secMax, boolean maxBlend) {
		if (greaterThan(pos, secMin) && smallerThan(pos, secMax)) {
			int index = (pos.getX() & 15) << 8 | (pos.getY() & 15) << 4 | (pos.getZ() & 15);
			if (maxBlend) data[index] = ColorMath.maxBlend(data[index], light) | ColorMath.ALPHA;
			else data[index] = ColorMath.mulBlend(data[index], light) | ColorMath.ALPHA;
		}
	}
	
	private boolean greaterThan(BlockPos a, BlockPos b) {
		return a.getX() >= b.getX() && a.getY() >= b.getY() && a.getZ() >= b.getZ();
	}
	
	private boolean smallerThan(BlockPos a, BlockPos b) {
		return a.getX() < b.getX() && a.getY() < b.getY() && a.getZ() < b.getZ();
	}
	
	static {
		List<BlockPos> positions = new ArrayList<>(27);
		
		for (byte i = -1; i < 2; i++) {
			for (byte j = -1; j < 2; j++) {
				for (byte k = -1; k < 2; k++) {
					positions.add(new BlockPos(i, j, k));
				}
			}
		}
		
		OFFSETS = positions
			.stream()
			.sorted(Comparator.comparingDouble(b -> b.distSqr(Vec3i.ZERO)))
			.toList()
			.toArray(new BlockPos[27]);
	}
	
	private class TransformerInfo {
		final LightInfo light;
		final BlockPos pos;
		
		private TransformerInfo(LightInfo light, BlockPos pos) {
			this.light = light;
			this.pos = pos;
		}
	}
}
