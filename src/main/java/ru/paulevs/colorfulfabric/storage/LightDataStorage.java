package ru.paulevs.colorfulfabric.storage;

import ru.paulevs.colorfulfabric.Texture3D;

public class LightDataStorage {
	private final byte[] red = new byte[Constants.VOLUME];
	private final byte[] green = new byte[Constants.VOLUME];
	private final byte[] blue = new byte[Constants.VOLUME];
	
	public LightDataStorage(byte[] red, byte[] green, byte[] blue) {
		System.arraycopy(red, 0, this.red, 0, Constants.VOLUME);
		System.arraycopy(green, 0, this.green, 0, Constants.VOLUME);
		System.arraycopy(blue, 0, this.blue, 0, Constants.VOLUME);
	}
	
	public Texture3D makeTexture(Texture3D src) {
		return src == null ? new Texture3D(red, green, blue) : src.fillTexture(red, green, blue);
	}
}
