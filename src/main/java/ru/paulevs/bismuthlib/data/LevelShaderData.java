package ru.paulevs.bismuthlib.data;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import ru.paulevs.bismuthlib.LightPropagator;
import ru.paulevs.bismuthlib.gui.CFOptions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

@Environment(EnvType.CLIENT)
public class LevelShaderData {
	private final Set<BlockPos> updateSections = new HashSet<>();
	private final Set<BlockPos> delayedSections = new HashSet<>();
	private final MutableBlockPos lastCenter = new MutableBlockPos();
	private final ShaderSectionData[][][] data;
	private final DynamicTexture texture;
	private final int textureSide;
	private final int dataHeight;
	private final int halfHeight;
	private final int dataWidth;
	private final int halfWidth;
	private final int dataSide;
	private Level level;
	
	private final ArrayBlockingQueue<BlockPos>[] innerUpdates;
	private final Thread[] threads;
	private byte updateTicks = 0;
	private byte mapUpdate = 0;
	private boolean upload;
	private boolean run;
	
	public LevelShaderData(int dataWidth, int dataHeight, int threadCount) {
		this.data = new ShaderSectionData[dataWidth][dataHeight][dataWidth];
		this.halfHeight = dataHeight >> 1;
		this.halfWidth = dataWidth >> 1;
		
		for (int x = 0; x < dataWidth; x++) {
			for (int y = 0; y < dataHeight; y++) {
				for (int z = 0; z < dataWidth; z++) {
					data[x][y][z] = new ShaderSectionData();
				}
			}
		}
		
		this.dataSide = (int) Math.ceil(Mth.sqrt(dataWidth * dataWidth * dataHeight));
		textureSide = getClosestPowerOfTwo(dataSide << 6);
		texture = new DynamicTexture(textureSide, textureSide, false);
		this.dataHeight = dataHeight;
		this.dataWidth = dataWidth;
		
		Thread main = Thread.currentThread();
		
		run = true;
		innerUpdates = new ArrayBlockingQueue[threadCount];
		LightPropagator[] propagators = new LightPropagator[threadCount];
		threads = new Thread[threadCount];
		for (byte i = 0; i < threadCount; i++) {
			final ArrayBlockingQueue<BlockPos> updates = new ArrayBlockingQueue<>(4096);
			final LightPropagator propagator = new LightPropagator();
			innerUpdates[i] = updates;
			propagators[i] = propagator;
			threads[i] = new Thread(() -> {
				while (run && main.isAlive()) {
					BlockPos pos = updates.poll();
					if (pos != null) {
						updateSection(level, pos.getX(), pos.getY(), pos.getZ(), true, propagator);
					}
				}
			});
			threads[i].setName("colored_lights_" + i);
			threads[i].start();
		}
	}
	
	public void dispose() {
		run = false;
		for (Thread t: threads) {
			while (t.isAlive());
		}
		texture.close();
	}
	
	public void resetAll() {
		if (level == null) return;
		short hmin = (short) level.getMinSection();
		short hmax = (short) level.getMaxSection();
		for (int i = 0; i < dataWidth; i++) {
			int px = lastCenter.getX() - halfWidth + i;
			for (int j = 0; j < dataWidth; j++) {
				int pz = lastCenter.getZ() - halfWidth + j;
				for (int k = 0; k < dataHeight; k++) {
					int py = lastCenter.getY() - halfHeight + k;
					if (py < hmin || py > hmax) continue;
					markToUpdate(px, py, pz);
				}
			}
		}
	}
	
	public DynamicTexture getTexture() {
		return texture;
	}
	
	public int getDataWidth() {
		return dataWidth;
	}
	
	public int getDataHeight() {
		return dataHeight;
	}
	
	public int getDataSide() {
		return dataSide;
	}
	
	public void markToUpdate(int x, int y, int z) {
		updateSections.add(new BlockPos(x, y, z));
	}
	
	private boolean updateSection(Level level, int x, int y, int z, boolean force, LightPropagator propagator) {
		int indexX = Math.abs(x - lastCenter.getX());
		int indexY = Math.abs(y - lastCenter.getY());
		int indexZ = Math.abs(z - lastCenter.getZ());
		if (indexX > halfWidth || indexZ > halfWidth || indexY > halfHeight) return false;
		indexX = wrap(x, dataWidth);
		indexY = wrap(y, dataHeight);
		indexZ = wrap(z, dataWidth);
		ShaderSectionData section = data[indexX][indexY][indexZ];
		if (force || !section.hasCorrectPosition(x, y, z)) {
			section.setPosition(x, y, z);
			int[] sectionData = section.getData();
			if (CFOptions.isFastLight()) {
				propagator.fastLight(level, new BlockPos(x, y, z), sectionData);
			}
			else {
				propagator.advancedLight(level, new BlockPos(x, y, z), sectionData);
			}
			int index = ((indexX * dataHeight) + indexY) * dataWidth + indexZ;
			int textureX = (index % dataSide) << 6;
			int textureY = (index / dataSide) << 6;
			short dataIndex = 0;
			NativeImage image = texture.getPixels();
			for (byte j = 0; j < 64; j++) {
				for (byte i = 0; i < 64; i++) {
					image.setPixelRGBA(textureX | i, textureY | j, sectionData[dataIndex++]);
				}
			}
			upload = true;
			return true;
		}
		return false;
	}
	
	public void update(Level level, int cx, int cy, int cz) {
		boolean force = level != this.level;
		this.level = level;
		if (lastCenter.getX() != cx || lastCenter.getY() != cy || lastCenter.getZ() != cz) {
			lastCenter.set(cx, cy, cz);
			short hmin = (short) level.getMinSection();
			short hmax = (short) level.getMaxSection();
			for (int i = 0; i < dataWidth; i++) {
				int px = cx - halfWidth + i;
				for (int j = 0; j < dataWidth; j++) {
					int pz = cz - halfWidth + j;
					for (int k = 0; k < dataHeight; k++) {
						int py = cy - halfHeight + k;
						if (py < hmin || py > hmax) continue;
						if (force) {
							markToUpdate(px, py, pz);
							continue;
						}
						int indexX = wrap(px, dataWidth);
						int indexY = wrap(py, dataHeight);
						int indexZ = wrap(pz, dataWidth);
						ShaderSectionData section = data[indexX][indexY][indexZ];
						if (!section.hasCorrectPosition(px, py, pz)) {
							markToUpdate(px, py, pz);
						}
					}
				}
			}
		}
		
		if (updateTicks++ > 16) {
			if (upload) {
				synchronized (texture) {
					upload = false;
					texture.upload();
				}
			}
			
			updateTicks = 0;
			updateSections.forEach(pos -> {
				int index = getMultiIndex(pos);
				if (innerUpdates[index].remainingCapacity() > 0) {
					innerUpdates[index].add(pos);
				}
				else {
					delayedSections.add(pos);
				}
			});
			updateSections.clear();
			updateSections.addAll(delayedSections);
			delayedSections.clear();
		}
	}
	
	private int getMultiIndex(BlockPos pos) {
		if (threads.length == 1) return 0;
		return wrap(pos.getX() + pos.getY() + pos.getZ(), threads.length);
	}
	
	public BlockPos getCenter() {
		return lastCenter;
	}
	
	public int getThreadCount() {
		return threads.length;
	}
	
	private int getClosestPowerOfTwo(int value) {
		if (value <= 0) return 0;
		byte index = 0;
		byte count = 0;
		for (byte i = 0; i < 32; i++) {
			byte bit = (byte) (value & 1);
			if (bit == 1) {
				index = i;
				count++;
			}
			value >>>= 1;
		}
		return count == 1 ? 1 << index : 1 << (index + 1);
	}
	
	private int wrap(int value, int side) {
		int offset = value / side * side;
		if (offset > value) offset -= side;
		float delta = (float) (value - offset) / side;
		return (int) (delta * side);
	}
}
