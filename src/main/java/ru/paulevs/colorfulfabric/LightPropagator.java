package ru.paulevs.colorfulfabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import ru.paulevs.colorfulfabric.data.BlockLights;
import ru.paulevs.colorfulfabric.data.LightInfo;
import ru.paulevs.colorfulfabric.gui.CFOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightPropagator {
	private static final Direction[] DIRECTIONS = Direction.values();
	private static final byte MASK_OFFSET = 17;
	private static final int ALPHA = 255 << 24;
	private static final BlockPos[] OFFSETS;
	private static final int WHITE = 0xFFFFFFFF;
	
	private final List<Set<BlockPos>> buffers = new ArrayList<>(2);
	private final boolean[] mask = new boolean[35937];
	private final int[] multipliers = new int[35937];
	
	public LightPropagator() {
		buffers.add(new HashSet<>());
		buffers.add(new HashSet<>());
	}
	
	public void fastLight(Level level, BlockPos sectionPos, int[] data) {
		if (sectionPos.getY() < level.getMinSection() || sectionPos.getY() > level.getMaxSection()) return;
		LevelChunk chunk = level.getChunk(sectionPos.getX(), sectionPos.getZ());
		if (chunk.getPos().x != sectionPos.getX() || chunk.getPos().z != sectionPos.getZ()) return;
		LevelChunkSection section = chunk.getSection(sectionPos.getY() - level.getMinSection());
		if (section == null) return;
		
		MutableBlockPos pos = new MutableBlockPos();
		MutableBlockPos pos2 = new MutableBlockPos();
		
		for (byte x = 0; x < 16; x++) {
			pos.setX(sectionPos.getX() << 4 | x);
			for (byte y = 0; y < 16; y++) {
				pos.setY(sectionPos.getY() << 4 | y);
				for (byte z = 0; z < 16; z++) {
					pos.setZ(sectionPos.getZ() << 4 | z);
					int index = x << 8 | y << 4 | z;
					data[index] = ALPHA;
					
					BlockState state = section.getBlockState(x, y, z);
					LightInfo light = BlockLights.getLight(state);
					
					if (light != null) {
						data[index] = light.getSimple((byte) 0) | ALPHA;
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
							data[index] = maxBlend(data[index], light.getSimple(i)) | ALPHA;
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
		
		Arrays.fill(data, ALPHA);
		MutableBlockPos pos = new MutableBlockPos();
		
		for (int x = x1; x < x2; x++) {
			pos.setX(x);
			for (int y = y1; y < y2; y++) {
				pos.setY(y);
				for (int z = z1; z < z2; z++) {
					pos.setZ(z);
					BlockState state = level.getBlockState(pos);
					LightInfo light = BlockLights.getLight(state);
					if (light != null) {
						//fastFillLight(data, pos, light, secMin, secMax);
						fillLight(level, data, pos, light, secMin, secMax);
					}
				}
			}
		}
	}
	
	private void fastFillLight(int[] data, BlockPos pos, LightInfo info, BlockPos secMin, BlockPos secMax) {
		int radius = info.getRadius();
		MutableBlockPos p = new MutableBlockPos();
		for (int i = -radius; i <= radius; i++) {
			p.setX(pos.getX() + i);
			for (int j = -radius; j <= radius; j++) {
				p.setY(pos.getY() + j);
				for (int k = -radius; k <= radius; k++) {
					p.setZ(pos.getZ() + k);
					int dist = Math.abs(i) + Math.abs(j) + Math.abs(k);
					if (dist >= radius) continue;
					setLight(data, p, info.getAdvanced((byte) dist), secMin, secMax, true);
				}
			}
		}
	}
	
	private void fillLight(Level level, int[] data, BlockPos pos, LightInfo info, BlockPos secMin, BlockPos secMax) {
		boolean modify = CFOptions.modifyColor();
		
		Arrays.fill(mask, false);
		if (modify) Arrays.fill(multipliers, WHITE);
		mask[getMaskIndex(MASK_OFFSET, MASK_OFFSET, MASK_OFFSET)] = true;
		
		int color = info.getAdvanced((byte) 0);
		setLight(data, pos, color, secMin, secMax, true);
		
		buffers.get(0).add(pos);
		byte radius = (byte) info.getRadius();
		byte bufferIndex = 0;
		
		byte dx, dy, dz;
		dx = dy = dz = 0;
		int multiplier = 0;
		int transformer = 0;
		float ax, ay, az, max;
		for (byte i = 1; i < radius; i++) {
			Set<BlockPos> starts = buffers.get(bufferIndex);
			bufferIndex = (byte) ((bufferIndex + 1) & 1);
			Set<BlockPos> ends = buffers.get(bufferIndex);
			color = info.getAdvanced(i);
			
			for (BlockPos start: starts) {
				for (Direction offset: DIRECTIONS) {
					BlockPos p = start.relative(offset);
					
					byte maskX = (byte) (p.getX() - pos.getX());
					byte maskY = (byte) (p.getY() - pos.getY());
					byte maskZ = (byte) (p.getZ() - pos.getZ());
					
					if (maskX < -radius || maskY < -radius || maskZ < -radius || maskX > radius || maskY > radius || maskZ > radius) continue;
					
					maskX += MASK_OFFSET;
					maskY += MASK_OFFSET;
					maskZ += MASK_OFFSET;
					
					int maskIndex = getMaskIndex(maskX, maskY, maskZ);
					if (mask[maskIndex]) continue;
					
					if (modify) {
						dx = (byte) (pos.getX() - p.getX());
						dy = (byte) (pos.getY() - p.getY());
						dz = (byte) (pos.getZ() - p.getZ());
						ax = Math.abs(dx);
						ay = Math.abs(dy);
						az = Math.abs(dz);
						max = Math.max(ax, Math.max(ay, az));
						dx = (byte) (Math.round(Math.abs(dx) / max) * Mth.sign(dx));
						dy = (byte) (Math.round(Math.abs(dy) / max) * Mth.sign(dy));
						dz = (byte) (Math.round(Math.abs(dz) / max) * Mth.sign(dz));
					}
					
					BlockState state = level.getBlockState(p);
					int currentColor = color;
					
					if (modify) {
						transformer = BlockLights.getTransformer(state);
						multiplier = multipliers[getMaskIndex(
							(byte) (maskX + dx),
							(byte) (maskY + dy),
							(byte) (maskZ + dz)
						)];
						if (multiplier != WHITE) {
							currentColor = mulBlend(color, multiplier);
							multipliers[maskIndex] = multiplier;
						}
					}
					
					if (modify && transformer != 0) {
						setLight(data, p, currentColor, secMin, secMax, false);
						multipliers[maskIndex] = multiplier != WHITE ? mulBlend(transformer, multiplier) : transformer;
						mask[maskIndex] = true;
						ends.add(p);
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
					setLight(data, p, currentColor, secMin, secMax, true);
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
			if (maxBlend) data[index] = maxBlend(data[index], light) | ALPHA;
			else data[index] = mulBlend(data[index], light) | ALPHA;
		}
	}
	
	private boolean greaterThan(BlockPos a, BlockPos b) {
		return a.getX() >= b.getX() && a.getY() >= b.getY() && a.getZ() >= b.getZ();
	}
	
	private boolean smallerThan(BlockPos a, BlockPos b) {
		return a.getX() < b.getX() && a.getY() < b.getY() && a.getZ() < b.getZ();
	}
	
	private static int maxBlend(int a, int b) {
		int i1 = Math.max(a & 0x00FF0000, b & 0x00FF0000);
		int i2 = Math.max(a & 0x0000FF00, b & 0x0000FF00);
		int i3 = Math.max(a & 0x000000FF, b & 0x000000FF);
		return i1 | i2 | i3;
	}
	
	private static int mulBlend(int a, int b) {
		int i1 = (int) ((((a >> 16) & 255) / 255F) * (((b >> 16) & 255) / 255F) * 255);
		int i2 = (int) ((((a >> 8) & 255) / 255F) * (((b >> 8) & 255) / 255F) * 255);
		int i3 = (int) (((a & 255) / 255F) * ((b & 255) / 255F) * 255);
		return i1 << 16 | i2 << 8 | i3;
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
}
