package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import ru.paulevs.colorfulfabric.ColorLightManager;
import ru.paulevs.colorfulfabric.ColoredRenderData;
import ru.paulevs.colorfulfabric.Texture3D;

@Mixin(BuiltChunk.class)
public class BuiltChunkMixin implements ColoredRenderData {
	private Texture3D texture;
	
	@Shadow
	public BlockPos getOrigin() {
		return null;
	}
	
	@Inject(method = "getBuffer", at = @At("TAIL"))
	private void cf_getBuffer(RenderLayer layer, CallbackInfoReturnable<VertexBuffer> info) {
		if (RenderSystem.isOnRenderThread()) {
			BlockPos pos = getOrigin();
			Texture3D tex = getTexture();
			tex = ColorLightManager.getTexture3D(pos, tex);
			setTexture(tex);
			tex.bind();
		}
	}
	
	@Inject(method = "delete", at = @At("TAIL"))
	private void cf_delete(CallbackInfo info) {
		/*Texture3D tex = getTexture();
		if (tex != null) {
			tex.delete();
		}*/
	}

	@Override
	public Texture3D getTexture() {
		return texture;
	}

	@Override
	public void setTexture(Texture3D texture) {
		this.texture = texture;
	}
}
