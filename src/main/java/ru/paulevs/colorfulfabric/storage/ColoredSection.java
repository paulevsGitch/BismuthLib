package ru.paulevs.colorfulfabric.storage;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import ru.paulevs.colorfulfabric.ColorLightManager;

public class ColoredSection {
	private final Map<BlockPos, LightSourceInfo> sources = Maps.newHashMap();
	private final byte[] data = new byte[Constants.ALLOCATE_SIZE];
	private final BlockPos pos;
	
	public ColoredSection(BlockPos pos) {
		this.pos = pos;
	}
	
	public void setColor(int x, int y, int z, int r, int g, int b) {
		int index = getIndex(x + 1, y + 1, z + 1);
		data[index] = (byte) r;
		data[index | 1] = (byte) g;
		data[index | 2] = (byte) b;
		copyValues(index, x, y, z);
	}
	
	public void addColor(int x, int y, int z, int r, int g, int b) {
		int index = getIndex(x + 1, y + 1, z + 1);
		data[index] = (byte) Math.max(r, data[index] & 255);
		data[index | 1] = (byte) Math.max(g, data[index | 1] & 255);
		data[index | 2] = (byte) Math.max(b, data[index | 2] & 255);
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
						section.data[index2] = data[index];
						section.data[index2 | 1] = data[index | 1];
						section.data[index2 | 2] = data[index | 2];
					}
				}
			}
		}
	}
	
	public LightDataStorage makeStorage() {
		return new LightDataStorage(data);
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
		return (z << 10 | y << 5 | x) << 2;
	}
}
