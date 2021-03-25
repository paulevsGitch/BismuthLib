package ru.paulevs.colorfulfabric.storage;

import java.nio.ByteBuffer;

import net.minecraft.util.math.MathHelper;
import ru.paulevs.colorfulfabric.Texture1D;

public class LightDataStorage {
	private final int[] data = new int[2048];
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
	
	public LightDataStorage(byte[] red, byte[] green, byte[] blue) {
		for (int i = 0; i < 1458; i++) {
			buffer.rewind();
			for (int j = 0; j < 4; j++) {
				int index = (i << 2) | j;
				byte merged = (byte) (getScaled(red[index]) * 36 + getScaled(green[index]) * 6 + getScaled(blue[index]));
				buffer.put(merged);
			}
			buffer.rewind();
			data[i] = buffer.getInt();
		}
		System.arraycopy(data, 0, this.data, 0, 1458);
	}
	
	public Texture1D makeTexture(Texture1D src) {
		return src == null ? new Texture1D(data) : src.fillTexture(data);
	}
	
	private int getScaled(byte value) {
		return MathHelper.floor(value / 255F * 6F);
	}
}
