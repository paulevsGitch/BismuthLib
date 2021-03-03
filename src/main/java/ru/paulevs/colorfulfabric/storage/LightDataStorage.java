package ru.paulevs.colorfulfabric.storage;

import ru.paulevs.colorfulfabric.Texture3D;

public class LightDataStorage {
	private final byte[] red = new byte[ColoredSection.CAPACITY];
	private final byte[] green = new byte[ColoredSection.CAPACITY];
	private final byte[] blue = new byte[ColoredSection.CAPACITY];
	
	public LightDataStorage(byte[] red, byte[] green, byte[] blue) {
		System.arraycopy(red, 0, this.red, 0, red.length);
		System.arraycopy(green, 0, this.green, 0, green.length);
		System.arraycopy(blue, 0, this.blue, 0, blue.length);
	}
	
	public Texture3D makeTexture(Texture3D src) {
		return src == null ? new Texture3D(red, green, blue) : src.fillTexture(red, green, blue);
	}
}
