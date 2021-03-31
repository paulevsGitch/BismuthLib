package ru.paulevs.colorfulfabric.storage;

import java.nio.ByteBuffer;

import ru.paulevs.colorfulfabric.Texture2D;

public class LightDataStorage {
	private final int[] data = new int[2048];
	private final ByteBuffer datab = ByteBuffer.allocate(5832);
	
	public LightDataStorage(byte[] red, byte[] green, byte[] blue) {
		datab.rewind();
		for (int i = 0; i < red.length; i++) {
			datab.put(getIndex(red[i], green[i], blue[i]));
		}
		datab.rewind();
		for (int i = 0; i < 1458; i++) {
			data[i] = datab.getInt();
		}
	}
	
	public Texture2D makeTexture(Texture2D src) {
		return src == null ? new Texture2D(data, data.length) : src.fillTexture(data, data.length);
	}
	
	private byte getIndex(int r, int g, int b) {
		return (byte) (r > 0 || g > 0 || b > 0 ? 255 : 0);
	}
}
