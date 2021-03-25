package ru.paulevs.colorfulfabric;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import ru.paulevs.colorfulfabric.storage.BlockStateColors;
import ru.paulevs.colorfulfabric.storage.ColoredSection;
import ru.paulevs.colorfulfabric.storage.LightDataStorage;
import ru.paulevs.colorfulfabric.storage.LightSourceInfo;

public class ColorLightManager {
	private static final ConcurrentLinkedQueue<LightSourceInfo> LIGHT_QUEUE = new ConcurrentLinkedQueue<LightSourceInfo>();
	private static final ConcurrentLinkedQueue<BlockPos> REMOVE_QUEUE = new ConcurrentLinkedQueue<BlockPos>();
	private static final ConcurrentLinkedQueue<BlockPos> UPDATE_QUEUE = new ConcurrentLinkedQueue<BlockPos>();
	public static final Map<BlockPos, LightDataStorage> LIGHT_DATA = Collections.synchronizedMap(Maps.newHashMap());
	private static final Map<BlockPos, ColoredSection> SECTIONS = Maps.newHashMap();
	private static final Map<BlockPos, SectionTexture> TEXTURES = Maps.newHashMap();
	private static final Mutable POS_BLOCK = new Mutable();
	private static final Mutable POS = new Mutable();
	private static Thread lightUpdater;
	private static SectionTexture empty;
	private static boolean run;
	
	public static void start() {
		run = true;
		lightUpdater = new Thread() {
			@Override
			public void run() {
				while(run) {
					BlockPos rPos = REMOVE_QUEUE.poll();
					if (rPos != null) {
						removeLight(rPos, 2);
					}
					
					LightSourceInfo info = LIGHT_QUEUE.poll();
					if (info != null) {
						BlockPos pos = info.getPos();
						getSection(pos.getX(), pos.getY(), pos.getZ()).addLightSource(info);
						placeFastLight(pos.getX(), pos.getY(), pos.getZ(), info.getColor());
						updateSections(pos.getX(), pos.getY(), pos.getZ(), 2);
					}
					
					BlockPos sPos = UPDATE_QUEUE.poll();
					if (sPos != null) {
						ColoredSection section = getSectionDirect(sPos.getX(), sPos.getY(), sPos.getZ());
						BlockPos updatePos = new BlockPos(sPos.getX() << 4, sPos.getY() << 4, sPos.getZ() << 4);
						LightDataStorage storage = section.makeStorage();
						LIGHT_DATA.put(updatePos, storage);
					}
				}
			}
		};
		lightUpdater.start();
	}
	
	public static void stop() {
		run = false;
	}
	
	public static ColoredSection getSection(int x, int y, int z) {
		POS.set(x >> 4, y >> 4, z >> 4);
		return getSection(POS);
	}
	
	public static ColoredSection getSectionDirect(int x, int y, int z) {
		POS.set(x, y, z);
		return getSection(POS);
	}
	
	public static ColoredSection getSection(BlockPos pos) {
		ColoredSection section = SECTIONS.get(pos);
		if (section == null) {
			BlockPos p = pos.toImmutable();
			section = new ColoredSection(p);
			SECTIONS.put(p, section);
		}
		return section;
	}
	
	public static void addLightSource(BlockPos pos, BlockState state) {
		Color color = BlockStateColors.getColor(state);
		if (color != null) {
			LIGHT_QUEUE.add(new LightSourceInfo(pos.toImmutable(), state, color));
		}
	}
	
	public static void removeLight(BlockPos pos) {
		REMOVE_QUEUE.add(pos.toImmutable());
	}
	
	private static void removeLight(BlockPos pos, int radius) {
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
					UPDATE_QUEUE.add(new BlockPos(i, j, k));
				}
			}
		}
	}
	
	public static SectionTexture getSectionTexture(BlockPos pos) {
		SectionTexture tex = TEXTURES.get(pos);
		
		LightDataStorage data = LIGHT_DATA.get(pos);
		if (data != null) {
			LIGHT_DATA.remove(pos);
			if (tex == null) {
				tex = data.makeTexture(tex);
				TEXTURES.put(pos.toImmutable(), tex);
			}
			else {
				tex = data.makeTexture(tex);
			}
			return tex;
		}
		
		if (tex == null) {
			if (empty == null) {
				empty = new SectionTexture();
			}
			return empty;
		}
		return tex;
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
					UPDATE_QUEUE.add(new BlockPos(i, j, k));
				}
			}
		}
	}
	
	private static void addColor(int x, int y, int z, int r, int g, int b) {
		getSection(x, y, z).addColor(x & 15, y & 15, z & 15, r, g, b);
	}
	
	private static void placeFastLight(int x, int y, int z, Color color) {
		for (int i = -1; i <= 1; i++) {
			POS_BLOCK.setX(x + i);
			for (int j = -1; j <= 1; j++) {
				POS_BLOCK.setY(y + j);
				for (int k = -1; k <= 1; k++) {
					POS_BLOCK.setZ(z + k);
					int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
					boolean isBright = d <= 1;
					int red = isBright ? color.getRed() : color.getRed() / 3;
					int green = isBright ? color.getGreen() : color.getGreen() / 3;
					int blue = isBright ? color.getBlue() : color.getBlue() / 3;
					addColor(POS_BLOCK.getX(), POS_BLOCK.getY(), POS_BLOCK.getZ(), red, green, blue);
				}
			}
		}
	}
}
