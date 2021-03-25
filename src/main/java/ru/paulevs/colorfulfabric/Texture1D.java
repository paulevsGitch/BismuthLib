package ru.paulevs.colorfulfabric;

import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.TextureUtil;

public class Texture1D {
	private static final IntBuffer PIXELS = BufferUtils.createIntBuffer(2048);
	private static final int[] EMPY = new int[2048];
	private final int textureID;
	
	public Texture1D() {
		this(EMPY);
	}
	
	public Texture1D(int[] data) {
		textureID = TextureUtil.generateId();
		fillTexture(data);
	}
	
	public Texture1D fillTexture(int[] data) {
		/*Random r = new Random();
		for (int i = 0; i < 1458; i++) {
			data[i] = r.nextInt();
		}*/
		initTexture();
		PIXELS.rewind();
		PIXELS.put(data);
		PIXELS.flip();
		GL20.glTexImage1D(GL20.GL_TEXTURE_1D, 0, GL20.GL_RGBA8, 2048, 0, GL20.GL_RGBA, GL20.GL_INT, PIXELS);
		GL20.glBindTexture(GL20.GL_TEXTURE_1D, 0);
		return this;
	}
	
	private void initTexture() {
		GL20.glBindTexture(GL20.GL_TEXTURE_1D, textureID);
		GL20.glTexParameteri(GL20.GL_TEXTURE_1D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
		GL20.glTexParameteri(GL20.GL_TEXTURE_1D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
		GL20.glTexParameteri(GL20.GL_TEXTURE_1D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		GL20.glTexParameteri(GL20.GL_TEXTURE_1D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
	}
	
	public void bind() {
		GL20.glActiveTexture(GL20.GL_TEXTURE6);
		GL20.glBindTexture(GL20.GL_TEXTURE_1D, textureID);
		GL20.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void delete() {
		RenderSystem.deleteTexture(textureID);
	}
}
