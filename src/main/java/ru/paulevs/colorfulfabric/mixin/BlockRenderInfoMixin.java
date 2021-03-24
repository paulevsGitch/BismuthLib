package ru.paulevs.colorfulfabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import ru.paulevs.colorfulfabric.ColorLightManager;
import ru.paulevs.colorfulfabric.ColorUtil;
import ru.paulevs.colorfulfabric.IBRInfo;

@Mixin(value = BlockRenderInfo.class, remap = false)
public class BlockRenderInfoMixin implements IBRInfo {
	@Final
	@Shadow
	private BlockColors blockColorMap;
	@Shadow
	public BlockRenderView blockView;
	@Shadow
	public BlockState blockState;
	@Shadow
	public BlockPos blockPos;
	
	@Override
	public int getBlockColor(int index, double x, double y, double z) {
		int color = ColorLightManager.getColor(x, y, z);
		return index == -1 ? color : ColorHelper.multiplyColor(blockColorMap.getColor(blockState, blockView, blockPos, index) | ColorUtil.BLACK, color);
	}
}
