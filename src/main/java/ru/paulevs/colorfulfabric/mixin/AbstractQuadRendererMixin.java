package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractQuadRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import ru.paulevs.colorfulfabric.IBRInfo;

@Mixin(value = AbstractQuadRenderer.class, remap = false)
public class AbstractQuadRendererMixin {
	@Final
	@Shadow
	protected BlockRenderInfo blockInfo;
	
	@Inject(method = "colorizeQuad", at = @At("HEAD"), cancellable = true)
	private void cf_colorizeQuad(MutableQuadViewImpl q, int blockColorIndex, CallbackInfo info) {
		for (int i = 0; i < 4; i++) {
			double px = (double) blockInfo.blockPos.getX() + (double) q.x(i);
			double py = (double) blockInfo.blockPos.getY() + (double) q.y(i);
			double pz = (double) blockInfo.blockPos.getZ() + (double) q.z(i);
			int blockColor = ((IBRInfo) blockInfo).getBlockColor(blockColorIndex, px, py, pz);
			blockColor = ColorHelper.multiplyColor(blockColor, q.spriteColor(i, 0));
			q.spriteColor(i, 0, ColorHelper.swapRedBlueIfNeeded(blockColor));
		}
		info.cancel();
	}
}
