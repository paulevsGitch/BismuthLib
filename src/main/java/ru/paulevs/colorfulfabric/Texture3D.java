package ru.paulevs.colorfulfabric;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.TextureUtil;
import ru.paulevs.colorfulfabric.storage.Constants;

public class Texture3D {
	private static final byte[] EMPY = new byte[Constants.VOLUME];
	private final ByteBuffer pixels = BufferUtils.createByteBuffer(Constants.ALLOCATE_SIZE);
	private final int textureID;
	
	public Texture3D() {
		this(EMPY, EMPY, EMPY);
	}
	
	public Texture3D(byte[] red, byte[] green, byte[] blue) {
		textureID = TextureUtil.generateId();
		fillTexture(red, green, blue);
	}
	
	public Texture3D fillTexture(byte[] red, byte[] green, byte[] blue) {
		initTexture();
		for (int i = 0; i < Constants.VOLUME; i++) {
			pixels.put(red[i]);
			pixels.put(green[i]);
			pixels.put(blue[i]);
		}
		pixels.flip();
		GL13.glTexImage3D(GL13.GL_TEXTURE_3D, 0, GL11.GL_RGB8, Constants.SIDE, Constants.SIDE, Constants.SIDE, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glBindTexture(GL13.GL_TEXTURE_3D, 0);
		return this;
	}
	
	private void initTexture() {
		GL11.glBindTexture(GL13.GL_TEXTURE_3D, textureID);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_3D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
	}
	
	public void bind() {
		GL13.glActiveTexture(GL13.GL_TEXTURE6);
		GL11.glBindTexture(GL13.GL_TEXTURE_3D, textureID);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void delete() {
		RenderSystem.deleteTexture(textureID);
	}
}
