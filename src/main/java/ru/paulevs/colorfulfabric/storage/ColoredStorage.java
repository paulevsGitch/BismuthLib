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
	//private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	//private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static final Mutable POS_BLOCK = new Mutable();
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
	
	public static void addColor(int x, int y, int z, int r, int g, int b) {
		getSection(x, y, z).addColor(x & 15, y & 15, z & 15, r, g, b);
	}
	
	public static void addLightSource(BlockPos pos, BlockState state) {
		ColoredSection section = getSection(pos.getX(), pos.getY(), pos.getZ());
		Color color = section.addLightSource(pos.getX(), pos.getY(), pos.getZ(), state);
		if (color != null) {
			placeFastLight(pos.getX(), pos.getY(), pos.getZ(), color);
			updateSections(pos.getX(), pos.getY(), pos.getZ(), 2);
		}
	}
	
	public static void addLightSourceDirectly(BlockPos pos, BlockState state) {
		ColoredSection section = getSection(pos.getX(), pos.getY(), pos.getZ());
		Color color = section.addLightSource(pos.getX(), pos.getY(), pos.getZ(), state);
		if (color != null) {
			placeFastLight(pos.getX(), pos.getY(), pos.getZ(), color);
		}
	}
	
	public static void removeLight(BlockPos pos, int radius) {
		getSection(pos.getX(), pos.getY(), pos.getZ()).removeLightSource(pos.getX(), pos.getY(), pos.getZ());
		
		for (int i = -radius; i <= radius; i++) {
			int px = pos.getX() + i;
			for (int j = -radius; j <= radius; j++) {
				int py = pos.getY() + j;
				for (int k = -radius; k <= radius; k++) {
					int pz = pos.getZ() + k;
					ColoredSection section = getSection(px, py, pz);
					section.setColor(px & 15, py & 15, pz & 15, 0, 0, 0);
				}
			}
		}
		
		int x1 = (pos.getX() - radius - 2) >> 4;
		int y1 = (pos.getY() - radius - 2) >> 4;
		int z1 = (pos.getZ() - radius - 2) >> 4;
		int x2 = (pos.getX() + radius + 2) >> 4;
		int y2 = (pos.getY() + radius + 2) >> 4;
		int z2 = (pos.getZ() + radius + 2) >> 4;
		
		for (int i = x1; i <= x2; i++) {
			for (int j = y1; j <= y2; j++) {
				for (int k = z1; k <= z2; k++) {
					ColoredSection section = getSectionDirect(i, j, k);
					section.getLightSources().forEach((source) -> {
						BlockPos p = source.getPos();
						placeFastLight(p.getX(), p.getY(), p.getZ(), source.getColor());
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
	
	private static void updateSections(int x, int y, int z, int radius) {
		int x1 = (x - radius) >> 4;
		int y1 = (y - radius) >> 4;
		int z1 = (z - radius) >> 4;
		int x2 = (x + radius) >> 4;
		int y2 = (y + radius) >> 4;
		int z2 = (z + radius) >> 4;
		for (int i = x1; i <= x2; i++) {
			for (int j = y1; j <= y2; j++) {
				for (int k = z1; k <= z2; k++) {
					ColorfulFabricClient.UPDATE.add(new BlockPos(i << 4, j << 4, k << 4));
				}
			}
		}
	}
	
	private static void placeFastLight(int x, int y, int z, Color color) {
		for (int i = -1; i <= 1; i++) {
			POS_BLOCK.setX(x + i);
			for (int j = -1; j <= 1; j++) {
				POS_BLOCK.setY(y + j);
				for (int k = -1; k <= 1; k++) {
					POS_BLOCK.setZ(z + k);
					//boolean central = i == 0 && j == 0 && k == 0;
					//BlockState state = (CLIENT.world == null) ? AIR : CLIENT.world.getBlockState(POS_BLOCK);
					//if (central || !state.isOpaque()) {
						int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
						boolean isBright = d <= 1;
						int red = isBright ? color.getRed() : color.getRed() / 3;
						int green = isBright ? color.getGreen() : color.getGreen() / 3;
						int blue = isBright ? color.getBlue() : color.getBlue() / 3;
						addColor(POS_BLOCK.getX(), POS_BLOCK.getY(), POS_BLOCK.getZ(), red, green, blue);
					//}
				}
			}
		}
	}
}
