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
import ru.paulevs.colorfulfabric.ColoredRenderData;
import ru.paulevs.colorfulfabric.ColorfulFabricClient;
import ru.paulevs.colorfulfabric.Texture3D;
import ru.paulevs.colorfulfabric.storage.ColoredStorage;

@Mixin(BuiltChunk.class)
public class BuiltChunkMixin implements ColoredRenderData {
	//private static final Texture3D EMPTY = new Texture3D();
	private Texture3D texture;
	
	@Shadow
	public BlockPos getOrigin() {
		return null;
	}
	
	@Inject(method = "getBuffer", at = @At("TAIL"))
	private void cf_getBuffer(RenderLayer layer, CallbackInfoReturnable<VertexBuffer> info) {
		if (RenderSystem.isOnRenderThread()) {
			Texture3D tex = getTexture();
			
			BlockPos pos = getOrigin();
			if (tex == null) {
				tex = ColoredStorage.getTexture3D(pos, tex);
				setTexture(tex);
			}
			else if (ColorfulFabricClient.UPDATE.contains(pos)) {
				ColorfulFabricClient.UPDATE.remove(pos);
				tex = ColoredStorage.getTexture3D(pos, tex);
			}
			
			tex.bind();
		}
	}
	
	@Inject(method = "delete", at = @At("TAIL"))
	private void cf_delete(CallbackInfo info) {
		Texture3D tex = getTexture();
		if (tex != null) {
			tex.delete();
		}
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
