package ru.paulevs.bismuthlib.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.paulevs.bismuthlib.BismuthLibClient;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow private @Nullable ClientLevel level;
	
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void cf_onRenderLevel(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
		if (this.level != null) {
			BismuthLibClient.update(
				this.level,
				Mth.floor(camera.getPosition().x / 16.0),
				Mth.floor(camera.getPosition().y / 16.0),
				Mth.floor(camera.getPosition().z / 16.0)
			);
			BismuthLibClient.bindWithUniforms();
		}
	}
	
	@Inject(method = "renderChunkLayer", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/ShaderInstance;apply()V",
		shift = Shift.BEFORE
	))
	private void cf_onRenderChunkLayer(RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f, CallbackInfo info) {
		BismuthLibClient.bindWithUniforms();
	}
}
