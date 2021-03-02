package ru.paulevs.colorfulfabric;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;

public class Texture3D {
	// Cube with 16 blocks side and 3 bytes per color
	private static final int SIDE = 18;
	private static final int DATA_SIZE = 3;
	private static final int CAPACITY = SIDE * SIDE * SIDE * DATA_SIZE;
	private final ByteBuffer buffer = BufferUtils.createByteBuffer(CAPACITY);
	private final ByteBuffer pixel = BufferUtils.createByteBuffer(DATA_SIZE);
	private final int textureID;
	
	public Texture3D() {
		textureID = GL20.glGenTextures();
		makeEmpty();
	}
	
	public Texture3D(byte[] red, byte[] green, byte[] blue) {
		textureID = GL20.glGenTextures();
		fillTexture(red, green, blue);
	}
	
	private void makeEmpty() {
		initTexture();
		for (int i = 0; i < CAPACITY; i++) {
			buffer.put((byte) 0);
		}
		buffer.flip();
		GL20.glTexImage3D(GL20.GL_TEXTURE_3D, 0, GL20.GL_RGB8, SIDE, SIDE, SIDE, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, buffer);
	}
	
	// Will not work if texture side is not power of 2
	public Texture3D fillTextureOld(byte[] red, byte[] green, byte[] blue) {
		initTexture();
		buffer.rewind();
		for (int i = 0; i < red.length; i++) {
			buffer.put(red[i]);
			buffer.put(green[i]);
			buffer.put(blue[i]);
		}
		buffer.flip();
		GL20.glTexImage3D(GL20.GL_TEXTURE_3D, 0, GL20.GL_RGB8, SIDE, SIDE, SIDE, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, buffer);
		return this;
	}
	
	public Texture3D fillTexture(byte[] red, byte[] green, byte[] blue) {
		initTexture();
		GL20.glTexImage3D(GL20.GL_TEXTURE_3D, 0, GL20.GL_RGB8, SIDE, SIDE, SIDE, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		for (int i = 0; i < red.length; i++) {
			int x = i % 18;
			int y = (i / 18) % 18;
			int z = i / 324;
			pixel.rewind();
			pixel.put(red[i]);
			pixel.put(green[i]);
			pixel.put(blue[i]);
			pixel.flip();
			GL20.glTexSubImage3D(GL20.GL_TEXTURE_3D, 0, x, y, z, 1, 1, 1, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixel);
		}
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
