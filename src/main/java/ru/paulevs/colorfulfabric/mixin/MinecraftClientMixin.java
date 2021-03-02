package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import ru.paulevs.colorfulfabric.ShaderUtil;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void cf_onMCInit(RunArgs args, CallbackInfo info) {
		ShaderUtil.init();
	}
}
