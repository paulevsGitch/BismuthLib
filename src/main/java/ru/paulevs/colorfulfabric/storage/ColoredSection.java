package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;
import ru.paulevs.colorfulfabric.Texture3D;

public class ColoredSection {
	private static final Mutable POS = new Mutable();
	private static final Vec3i[] OFFSETS;
	private static final int[] INDECIES;
	private static final int[] INDECIES2;
	private static final int SIDE = 18;
	private static final int SIDE_LONG = SIDE * SIDE;
	private static final int CAPACITY = SIDE * SIDE * SIDE; 
	
	private final Map<BlockPos, LightSourceInfo> sources = Maps.newHashMap();
	private final byte[] red = new byte[CAPACITY];
	private final byte[] green = new byte[CAPACITY];
	private final byte[] blue = new byte[CAPACITY];
	private final BlockPos pos;
	
	public ColoredSection(BlockPos pos) {
		this.pos = pos;
	}
	
	public void setColor(int x, int y, int z, int r, int g, int b) {
		int index = getIndex(x + 1, y + 1, z + 1);
		red[index] = (byte) r;
		green[index] = (byte) g;
		blue[index] = (byte) b;
	}
	
	public void addColor(int x, int y, int z, int r, int g, int b) {
		int index = getIndex(x + 1, y + 1, z + 1);
		red[index] = (byte) Math.max(r, red[index] & 255);
		green[index] = (byte) Math.max(g, green[index] & 255);
		blue[index] = (byte) Math.max(b, blue[index] & 255);
	}
	
	public Texture3D makeTexture(BlockPos pos, Texture3D src) {
		/*for (int i = 0; i < 27; i++) {
			if (i != 13) {
				int x = (((i % 3) - 1) << 4) + pos.getX();
				int y = ((((i / 3) % 3) - 1) << 4) + pos.getY();
				int z = (((i / 9) - 1) << 4) + pos.getZ();
				ColoredSection section = ColoredStorage.getSection(x, y, z);
			}
		}*/
		for (int i = 0; i < OFFSETS.length; i++) {
			int index = INDECIES[i];
			int index2 = INDECIES2[i];
			POS.set(pos).move(OFFSETS[i]);
			ColoredSection section = ColoredStorage.getSection(POS.getX(), POS.getY(), POS.getZ());
			red[index] = section.red[index2];
			green[index] = section.green[index2];
			blue[index] = section.blue[index2];
		}
		return src == null ? new Texture3D(red, green, blue) : src.fillTexture(red, green, blue);
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public Color addLightSource(int x, int y, int z, BlockState state) {
		Color color = BlockStateColors.getColor(state);
		if (color != null) {
			BlockPos pos = new BlockPos(x, y, z);
			sources.put(pos, new LightSourceInfo(pos, state, color));
			return color;
		}
		return null;
	}
	
	public void removeLightSource(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		sources.remove(pos);
	}
	
	@Override
	public int hashCode() {
		return pos.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		ColoredSection section = (ColoredSection) obj;
		return section == null ? false : section.pos.equals(pos);
	}

	public Collection<LightSourceInfo> getLightSources() {
		return sources.values();
	}
	
	private static int getIndex(int x, int y, int z) {
		return (z * SIDE_LONG) + (y * SIDE) + x;
	}
	
	static {
		int index = 0;
		OFFSETS = new Vec3i[1736];
		INDECIES = new int[1736];
		INDECIES2 = new int[1736];
		for (int x = 0; x < 18; x++) {
			for (int y = 0; y < 18; y++) {
				for (int z = 0; z < 18; z++) {
					if (x == 0 || y == 0 || z == 0 || x == 17 || y == 17 || z == 17) {
						OFFSETS[index] = new Vec3i(x - 1, y - 1, z - 1);
						INDECIES[index] = getIndex(x, y, z);
						INDECIES2[index] = getIndex(((x - 1) & 15) + 1, ((y - 1) & 15) + 1, ((z - 1) & 15) + 1);
						index++;
					}
				}
			}
		}
	}
}
