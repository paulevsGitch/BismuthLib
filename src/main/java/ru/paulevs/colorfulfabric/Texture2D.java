package ru.paulevs.colorfulfabric;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureUtil;

public class Texture2D {
	private static final IntBuffer PIXELS = BufferUtils.createIntBuffer(2048);
	private static final int[] EMPY = new int[2048];
	private final int textureID;
	
	public Texture2D() {
		this(EMPY, EMPY.length);
	}
	
	public Texture2D(int[] data, int width) {
		textureID = TextureUtil.generateId();
		fillTexture(data, width);
	}
	
	public Texture2D(NativeImage img) {
		ByteBuffer pixels = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int abgr = img.getPixelColor(x, y);
				abgr = Integer.rotateRight(abgr, 8);
				pixels.put((byte) NativeImage.getAlpha(abgr));
				pixels.put((byte) NativeImage.getRed(abgr));
				pixels.put((byte) NativeImage.getGreen(abgr));
				pixels.put((byte) NativeImage.getBlue(abgr));
			}
		}
		pixels.flip();
		
		textureID = TextureUtil.generateId();
		initTexture();
		GL20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
	}
	
	public Texture2D fillTexture(int[] data, int width) {
		initTexture();
		PIXELS.rewind();
		PIXELS.put(data);
		PIXELS.rewind();
		GL20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA8, width, data.length / width, 0, GL20.GL_RGBA, GL20.GL_INT, PIXELS);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
		return this;
	}
	
	private void initTexture() {
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureID);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
	}
	
	public void bind7() {
		GL20.glActiveTexture(GL20.GL_TEXTURE7);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureID);
		//GL20.glActiveTexture(GL20.GL_TEXTURE0);
	}
	
	public void bind6() {
		GL20.glActiveTexture(GL20.GL_TEXTURE6);
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureID);
		//GL20.glActiveTexture(GL20.GL_TEXTURE0);
	}

	public void delete() {
		RenderSystem.deleteTexture(textureID);
	}
}
