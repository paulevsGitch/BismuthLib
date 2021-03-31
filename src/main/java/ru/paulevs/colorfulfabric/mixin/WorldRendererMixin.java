package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import ru.paulevs.colorfulfabric.ShaderUtil;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
	private void fl_renderLayerStart(RenderLayer renderLayer, MatrixStack matrixStack, double d, double e, double f, CallbackInfo info) {
		ShaderUtil.useProgram();
		//ShaderUtil.bindLightmap();
	}
	
	@Inject(method = "renderLayer", at = @At("TAIL"))
	private void fl_renderLayerEnd(RenderLayer renderLayer, MatrixStack matrixStack, double d, double e, double f, CallbackInfo info) {
		ShaderUtil.unuseProgram();
	}
}
