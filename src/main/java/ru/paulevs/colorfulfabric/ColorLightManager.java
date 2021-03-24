package ru.paulevs.colorfulfabric;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.MathHelper;
import ru.paulevs.colorfulfabric.storage.BlockStateColors;
import ru.paulevs.colorfulfabric.storage.ColoredSection;
import ru.paulevs.colorfulfabric.storage.LightSourceInfo;

public class ColorLightManager {
	//private static final ConcurrentLinkedQueue<LightSourceInfo> LIGHT_QUEUE = new ConcurrentLinkedQueue<LightSourceInfo>();
	//private static final ConcurrentLinkedQueue<BlockPos> REMOVE_QUEUE = new ConcurrentLinkedQueue<BlockPos>();
	private static final Map<BlockPos, ColoredSection> SECTIONS = Maps.newHashMap();
	private static final Mutable POS_BLOCK = new Mutable();
	private static final Mutable POS = new Mutable();
	//private static Thread lightUpdater;
	//private static boolean run;
	
	public static void start() {
		/*run = true;
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
					}
				}
			}
		};
		lightUpdater.start();*/
	}
	
	public static void stop() {
		//run = false;
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
			//LIGHT_QUEUE.add(new LightSourceInfo(pos.toImmutable(), state, color));
			LightSourceInfo info = new LightSourceInfo(pos.toImmutable(), state, color);
			getSection(pos.getX(), pos.getY(), pos.getZ()).addLightSource(info);
			placeFastLight(pos.getX(), pos.getY(), pos.getZ(), info.getColor());
		}
	}
	
	public static void removeLight(BlockPos pos) {
		//REMOVE_QUEUE.add(pos.toImmutable());
		removeLight(pos, 2);
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
					section.setColor(px & 15, py & 15, pz & 15, ColorUtil.BLACK);
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
	}
	
	/*private static void updateArea(int x, int y, int z, int radius) {
		int x1 = (x - radius) >> 4;
		int y1 = (y - radius) >> 4;
		int z1 = (z - radius) >> 4;
		int x2 = (x + radius) >> 4;
		int y2 = (y + radius) >> 4;
		int z2 = (z + radius) >> 4;
		for (int i = x1; i <= x2; i++) {
			for (int j = y1; j <= y2; j++) {
				for (int k = z1; k <= z2; k++) {
					
				}
			}
		}
	}*/
	
	private static void addColor(int x, int y, int z, int color) {
		getSection(x, y, z).addColor(x & 15, y & 15, z & 15, color);
	}
	
	private static void placeFastLight(int x, int y, int z, Color color) {
		int colorBright = color.getRGB();
		int colorDark = ColorUtil.color(color.getRed() / 3, color.getGreen() / 3, color.getBlue() / 3);
		for (int i = -1; i <= 1; i++) {
			POS_BLOCK.setX(x + i);
			for (int j = -1; j <= 1; j++) {
				POS_BLOCK.setY(y + j);
				for (int k = -1; k <= 1; k++) {
					POS_BLOCK.setZ(z + k);
					int d = Math.abs(i) + Math.abs(j) + Math.abs(k);
					addColor(POS_BLOCK.getX(), POS_BLOCK.getY(), POS_BLOCK.getZ(), d <= 1 ? colorBright : colorDark);
				}
			}
		}
	}
	
	private static int getColor(int x, int y, int z) {
		ColoredSection section = getSection(x, y, z);
		return section.getColor(x & 15, y & 15, z & 15);
	}
	
	public static int getColor(double x, double y, double z) {
		int x1 = MathHelper.floor(x - 0.5);
		int y1 = MathHelper.floor(y - 0.5);
		int z1 = MathHelper.floor(z - 0.5);
		
		int x2 = x1 + 1;
		int y2 = y1 + 1;
		int z2 = z1 + 1;
		
		float dx = (float) (x - x1) - 0.5F;
		float dy = (float) (y - y1) - 0.5F;
		float dz = (float) (z - z1) - 0.5F;
		
		int a = getColor(x1, y1, z1);
		int b = getColor(x2, y1, z1);
		int c = getColor(x1, y2, z1);
		int d = getColor(x2, y2, z1);
		int e = getColor(x1, y1, z2);
		int f = getColor(x2, y1, z2);
		int g = getColor(x1, y2, z2);
		int h = getColor(x2, y2, z2);
		
		a = ColorUtil.lerp(a, b, dx);
		b = ColorUtil.lerp(c, d, dx);
		c = ColorUtil.lerp(e, f, dx);
		d = ColorUtil.lerp(g, h, dx);
		
		a = ColorUtil.lerp(a, b, dy);
		b = ColorUtil.lerp(c, d, dy);
		
		a = ColorUtil.lerp(a, b, dz);
		float mix = MHelper.max(ColorUtil.getRed(a), ColorUtil.getGreen(a), ColorUtil.getBlue(a)) / 255F;
		return ColorUtil.lerp(ColorUtil.WHITE, a, mix);
		
		/*int ix = MathHelper.floor(x + 0.5);
		int iy = MathHelper.floor(y + 0.5);
		int iz = MathHelper.floor(z + 0.5);
		return getColor(ix, iy, iz);*/
	}
}
