package ru.paulevs.colorfulfabric.storage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.math.BlockPos;
import ru.paulevs.colorfulfabric.ColorUtil;

public class ColoredSection {
	private final Map<BlockPos, LightSourceInfo> sources = Maps.newHashMap();
	private final int[] data = new int[4096];
	private final BlockPos pos;
	
	public ColoredSection(BlockPos pos) {
		this.pos = pos;
		Arrays.fill(data, ColorUtil.BLACK);
	}
	
	public void setColor(int x, int y, int z, int color) {
		int index = getIndex(x, y, z);
		synchronized (data) {
			data[index] = color;
		}
	}
	
	public void addColor(int x, int y, int z, int color) {
		int index = getIndex(x, y, z);
		synchronized (data) {
			data[index] = ColorUtil.max(color, data[index]);
		}
	}
	
	public int getColor(int x, int y, int z) {
		int index = getIndex(x, y, z);
		int color;
		synchronized (data) {
			color = data[index];
		}
		return color;
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
		return z << 8 | y << 4 | x;
	}
}
