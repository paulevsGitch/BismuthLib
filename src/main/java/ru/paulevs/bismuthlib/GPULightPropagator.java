package ru.paulevs.bismuthlib;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.Random;

public class GPULightPropagator {
	private static ShaderInstance shader;
	private final RenderTarget[] buffers = new RenderTarget[2];
	private final int textureSide;
	private final int dataSide;
	private byte index;
	
	public GPULightPropagator(int dataWidth, int dataHeight) {
		if (shader == null) {
			try {
				shader = new ShaderInstance(
					Minecraft.getInstance().getClientPackSource().getVanillaPack().asProvider(),
					"bismuthlib_gpu_light",
					DefaultVertexFormat.POSITION_TEX
				);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		dataSide = (int) Math.ceil(Mth.sqrt(dataWidth * dataWidth * dataHeight));
		textureSide = getClosestPowerOfTwo(dataSide << 6);
		buffers[0] = new TextureTarget(textureSide, textureSide, false, Minecraft.ON_OSX);
		buffers[1] = new TextureTarget(textureSide, textureSide, false, Minecraft.ON_OSX);
		
		Random random = new Random();
		NativeImage image = new NativeImage(textureSide, textureSide, false);
		for (int x = 0; x < textureSide; x++) {
			for (int y = 0; y < textureSide; y++) {
				image.setPixelRGBA(x, y, random.nextInt() | (255 << 24));
			}
		}
		
		GlStateManager._bindTexture(buffers[0].getColorTextureId());
		image.upload(0, 0, 0, false);
	}
	
	public void render() {
		int buffer = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		//System.out.println(buffer);
		for (byte i = 0; i < 16; i++) {
			RenderTarget source = buffers[index];
			index = (byte) ((index + 1) & 1);
			RenderTarget target = buffers[index];
			target.bindWrite(true);
			target.clear(Minecraft.ON_OSX);
			
			if (shader != null) RenderSystem.setShader(() -> shader);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			RenderSystem.setShaderTexture(0, source.getColorTextureId());
			
			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			
			bufferBuilder.vertex(0, textureSide, 0).uv(0.0F, 1.0F).endVertex();
			bufferBuilder.vertex(textureSide, textureSide, 0).uv(1.0F, 1.0F).endVertex();
			bufferBuilder.vertex(textureSide, 0, 0).uv(1.0F, 0.0F).endVertex();
			bufferBuilder.vertex(0, 0, 0).uv(0.0F, 0.0F).endVertex();
			
			BufferUploader.drawWithShader(bufferBuilder.end());
			target.unbindWrite();
		}
		GL30.glBindRenderbuffer(GL30.GL_FRAMEBUFFER, buffer);
	}
	
	public void dispose() {
		buffers[0].destroyBuffers();
		buffers[1].destroyBuffers();
	}
	
	private int getClosestPowerOfTwo(int value) {
		if (value <= 0) return 0;
		byte index = 0;
		byte count = 0;
		for (byte i = 0; i < 32; i++) {
			byte bit = (byte) (value & 1);
			if (bit == 1) {
				index = i;
				count++;
			}
			value >>>= 1;
		}
		return count == 1 ? 1 << index : 1 << (index + 1);
	}
	
	public int getTexture() {
		return buffers[index].getColorTextureId();
	}
}
