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
			int sx1 = x == 0 ? pos.getX() - 1 : pos.getX();
			int sy1 = y == 0 ? pos.getY() - 1 : pos.getY();
			int sz1 = z == 0 ? pos.getZ() - 1 : pos.getZ();
			int sx2 = x == 15 ? pos.getX() + 1 : pos.getX();
			int sy2 = y == 15 ? pos.getY() + 1 : pos.getY();
			int sz2 = z == 15 ? pos.getZ() + 1 : pos.getZ();
			
			for (int sx = sx1; sx <= sx2; sx++) {
				for (int sy = sy1; sy <= sy2; sy++) {
					for (int sz = sz1; sz <= sz2; sz++) {
						ColoredSection section = ColorLightManager.getSectionDirect(sx, sy, sz);
						int nx = sx == pos.getX() ? x + 1 : x == 0 ? 17 : x == 15 ? 0 : x + 1;
						int ny = sy == pos.getY() ? y + 1 : y == 0 ? 17 : y == 15 ? 0 : y + 1;
						int nz = sz == pos.getZ() ? z + 1 : z == 0 ? 17 : z == 15 ? 0 : z + 1;
						int index2 = getIndex(nx, ny, nz);
						section.red[index2] = red[index];
						section.green[index2] = green[index];
						section.blue[index2] = blue[index];
					}
				}
			}
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
