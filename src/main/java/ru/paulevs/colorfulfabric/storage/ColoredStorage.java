package ru.paulevs.colorfulfabric.storage;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import ru.paulevs.colorfulfabric.ColorfulFabricClient;
import ru.paulevs.colorfulfabric.Texture3D;

public class ColoredStorage {
	private static final Map<BlockPos, ColoredSection> SECTIONS = Maps.newHashMap();
	private static final Mutable POS = new Mutable();
	
	protected static ColoredSection getSection(int x, int y, int z) {
		POS.set(x >> 4, y >> 4, z >> 4);
		return getSection(POS);
	}
	
	private static ColoredSection getSectionDirect(int x, int y, int z) {
		POS.set(x, y, z);
		return getSection(POS);
	}
	
	private static ColoredSection getSection(BlockPos pos) {
		ColoredSection section = SECTIONS.get(pos);
		if (section == null) {
			BlockPos p = pos.toImmutable();
			section = new ColoredSection(p);
			SECTIONS.put(p, section);
		}
		return section;
	}
	
	public static void setColor(int x, int y, int z, int r, int g, int b) {
		getSection(x, y, z).setColor(x & 15, y & 15, z & 15, r, g, b);
	}
	
	public static void addColor(int x, int y, int z, int r, int g, int b) {
		getSection(x, y, z).addColor(x & 15, y & 15, z & 15, r, g, b);
	}
	
	public static void addLightSource(int x, int y, int z, BlockState state) {
		ColoredSection section = getSection(x, y, z);
		Color color = section.addLightSource(x, y, z, state);
		if (color != null) {
			placeFastLight(x, y, z, color, true);
		}
	}
	
	public static void removeLight(int x, int y, int z, int radius) {
		getSection(x, y, z).removeLightSource(x, y, z);
		
		for (int i = -radius; i <= radius; i++) {
			int px = x + i;
			for (int j = -radius; j <= radius; j++) {
				int py = y + j;
				for (int k = -radius; k <= radius; k++) {
					int pz = z + k;
					ColoredSection section = getSection(px, py, pz);
					section.setColor(px & 15, py & 15, pz & 15, 0, 0, 0);
				}
			}
		}
		
		int x1 = (x - radius - 2) >> 4;
		int y1 = (y - radius - 2) >> 4;
		int z1 = (z - radius - 2) >> 4;
		int x2 = (x + radius + 2) >> 4;
		int y2 = (y + radius + 2) >> 4;
		int z2 = (z + radius + 2) >> 4;
		
		for (int i = x1; i <= x2; i++) {
			for (int j = y1; j <= y2; j++) {
				for (int k = z1; k <= z2; k++) {
					ColoredSection section = getSectionDirect(i, j, k);
					section.getLightSources().forEach((source) -> {
						BlockPos pos = source.getPos();
						placeFastLight(pos.getX(), pos.getY(), pos.getZ(), source.getColor(), false);
					});
				}
			}
		}
		
		x1 --;
		y1 --;
		z1 --;
		x2 ++;
		y2 ++;
		z2 ++;
		
		for (int i = x1; i <= x2; i++) {
			for (int j = y1; j <= y2; j++) {
				for (int k = z1; k <= z2; k++) {
					BlockPos p = new BlockPos(i << 4, j << 4, k << 4);
					ColorfulFabricClient.UPDATE.add(p);
				}
			}
		}
	}
	
	public static Texture3D getTexture3D(BlockPos pos, Texture3D src) {
		return getSection(pos.getX(), pos.getY(), pos.getZ()).makeTexture(pos, src);
	}
	
	private static void placeFastLight(int x, int y, int z, Color color, boolean update) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
					boolean isBright = d <= 1;
					int red = isBright ? color.getRed() : color.getRed() / 3;
					int green = isBright ? color.getGreen() : color.getGreen() / 3;
					int blue = isBright ? color.getBlue() : color.getBlue() / 3;
					addColor(x + i, y + j, z + k, red, green, blue);
					if (update) {
						int px = ((x + i) >> 4) << 4;
						int py = ((y + j) >> 4) << 4;
						int pz = ((z + k) >> 4) << 4;
						BlockPos p = new BlockPos(px, py, pz);
						ColorfulFabricClient.UPDATE.add(p);
					}
				}
			}
		}
	}
}
