package ru.paulevs.colorfulfabric.storage;

import ru.paulevs.colorfulfabric.Texture3D;

public class LightDataStorage {
	private final byte[] data = new byte[Constants.ALLOCATE_SIZE];
	
	public LightDataStorage(byte[] data) {
		System.arraycopy(data, 0, this.data, 0, Constants.ALLOCATE_SIZE);
	}
	
	public Texture3D makeTexture(Texture3D src) {
		return src == null ? new Texture3D(data) : src.fillTexture(data);
	}
}
