package ru.paulevs.bismuthlib.mixin;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.core.BlockPos.MutableBlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.paulevs.bismuthlib.gui.CFOptions;

@Mixin(RenderChunk.class)
public class RenderChunkMixin {
	@Shadow @Final private MutableBlockPos origin;
	
	@Inject(method = "setNotDirty", at = @At("HEAD"))
	private void cf_onSetNotDirty(CallbackInfo info) {
		if (CFOptions.isFastLight()) {
			ru.paulevs.bismuthlib.BismuthLibClient.updateSection(
				this.origin.getX() >> 4,
				this.origin.getY() >> 4,
				this.origin.getZ() >> 4
			);
		}
		else {
			int cx1 = (this.origin.getX() - 16) >> 4;
			int cy1 = (this.origin.getY() - 16) >> 4;
			int cz1 = (this.origin.getZ() - 16) >> 4;
			int cx2 = (this.origin.getX() + 16) >> 4;
			int cy2 = (this.origin.getY() + 16) >> 4;
			int cz2 = (this.origin.getZ() + 16) >> 4;
			for (int x = cx1; x <= cx2; x++) {
				for (int y = cy1; y <= cy2; y++) {
					for (int z = cz1; z <= cz2; z++) {
						ru.paulevs.bismuthlib.BismuthLibClient.updateSection(x, y, z);
					}
				}
			}
		}
	}
}
