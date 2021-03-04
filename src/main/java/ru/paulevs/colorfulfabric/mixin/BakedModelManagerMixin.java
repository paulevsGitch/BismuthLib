package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import ru.paulevs.colorfulfabric.storage.BlockStateColors;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
	@Inject(method = "apply", at = @At("TAIL"))
	private void cf_apply(ModelLoader modelLoader, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		BlockStateColors.load();
	}
}
