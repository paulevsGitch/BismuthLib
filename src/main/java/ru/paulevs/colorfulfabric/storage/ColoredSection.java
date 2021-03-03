package ru.paulevs.colorfulfabric.storage;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import ru.paulevs.colorfulfabric.ColorLightManager;

public class ColoredSection {
	private static final int SIDE = 18;
	private static final int SIDE_LONG = SIDE * SIDE;
	protected static final int CAPACITY = SIDE * SIDE * SIDE; 
	
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
		copyValues(index, x, y, z);
	}
	
	public void addColor(int x, int y, int z, int r, int g, int b) {
		int index = getIndex(x + 1, y + 1, z + 1);
		red[index] = (byte) Math.max(r, red[index] & 255);
		green[index] = (byte) Math.max(g, green[index] & 255);
		blue[index] = (byte) Math.max(b, blue[index] & 255);
		copyValues(index, x, y, z);
	}
	
	private void copyValues(int index, int x, int y, int z) {
		if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
			int nx = x == 0 ? 17 : x == 15 ? 0 : x + 1;
			int ny = y == 0 ? 17 : y == 15 ? 0 : y + 1;
			int nz = z == 0 ? 17 : z == 15 ? 0 : z + 1;
			int sx = (x == 0 ? -1 : x == 15 ? +1 : 0) + pos.getX();
			int sy = (y == 0 ? -1 : y == 15 ? +1 : 0) + pos.getY();
			int sz = (z == 0 ? -1 : z == 15 ? +1 : 0) + pos.getZ();
			int index2 = getIndex(nx, ny, nz);
			ColoredSection section = ColorLightManager.getSectionDirect(sx, sy, sz);
			section.red[index2] = red[index];
			section.green[index2] = green[index];
			section.blue[index2] = blue[index];
		}
	}
	
	public LightDataStorage makeStorage() {
		return new LightDataStorage(red, green, blue);
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public void addLightSource(LightSourceInfo source) {
		sources.put(source.getPos(), source);
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
}
