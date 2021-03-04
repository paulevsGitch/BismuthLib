package ru.paulevs.colorfulfabric;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.TextureUtil;

public class Texture3D {
	// Cube with 16 blocks side and 3 bytes per color
	private static final int SIDE = 18;
	private static final int DATA_SIZE = 3;
	public static final int VOLUME = SIDE * SIDE * SIDE;
	private static final int ALLOCATE_SIZE = VOLUME * DATA_SIZE;
	public static final byte[] EMPY = new byte[VOLUME];
	//private final ByteBuffer pixel = BufferUtils.createByteBuffer(DATA_SIZE);
	private final int textureID;
	
	public Texture3D() {
		this(EMPY, EMPY, EMPY);
	}
	
	public Texture3D(byte[] red, byte[] green, byte[] blue) {
		textureID = TextureUtil.generateId();
		fillTexture(red, green, blue);
	}
	
	public Texture3D fillTexture(byte[] red, byte[] green, byte[] blue) {
		GL20.glActiveTexture(GL20.GL_TEXTURE6);
		initTexture();
		ByteBuffer pixels = BufferUtils.createByteBuffer(ALLOCATE_SIZE);
		GL20.glTexImage3D(GL20.GL_TEXTURE_3D, 0, GL20.GL_RGB8, SIDE, SIDE, SIDE, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);
		for (int i = 0; i < red.length; i++) {
			int x = i % 18;
			int y = (i / 18) % 18;
			int z = i / 324;
			ByteBuffer pixel = BufferUtils.createByteBuffer(DATA_SIZE);
			pixel.put(red[i]);
			pixel.put(green[i]);
			pixel.put(blue[i]);
			//pixel.rewind();
			pixel.flip();
			GL20.glTexSubImage3D(GL20.GL_TEXTURE_3D, 0, x, y, z, 1, 1, 1, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixel);
		}
		GL20.glBindTexture(GL20.GL_TEXTURE_3D, 0);
		GL20.glActiveTexture(GL20.GL_TEXTURE0);
		return this;
	}
	
	private void initTexture() {
		GL20.glBindTexture(GL20.GL_TEXTURE_3D, textureID);
		GL20.glTexParameteri(GL20.GL_TEXTURE_3D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		GL20.glTexParameteri(GL20.GL_TEXTURE_3D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		GL20.glTexParameteri(GL20.GL_TEXTURE_3D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		GL20.glTexParameteri(GL20.GL_TEXTURE_3D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
	}
	
	public void bind() {
		GL20.glActiveTexture(GL20.GL_TEXTURE6);
		GL20.glBindTexture(GL20.GL_TEXTURE_3D, textureID);
		GL20.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void delete() {
		RenderSystem.deleteTexture(textureID);
	}
}
