package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import ru.paulevs.colorfulfabric.ColorLightManager;
import ru.paulevs.colorfulfabric.Texture3D;

@Mixin(BuiltChunk.class)
public class BuiltChunkMixin {
	@Shadow
	private boolean needsRebuild;
	
	@Shadow
	public BlockPos getOrigin() {
		return null;
	}
	
	@Inject(method = "getBuffer", at = @At("TAIL"))
	private void cf_getBuffer(RenderLayer layer, CallbackInfoReturnable<VertexBuffer> info) {
		if (!needsRebuild && RenderSystem.isOnRenderThread()) {
			BlockPos pos = getOrigin();
			Texture3D tex = ColorLightManager.getTexture3D(pos, null);
			tex.bind();
		}
	}
}
