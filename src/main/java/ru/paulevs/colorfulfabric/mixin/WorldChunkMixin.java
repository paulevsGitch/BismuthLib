package ru.paulevs.colorfulfabric.mixin;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import ru.paulevs.colorfulfabric.storage.ColoredStorage;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	private static final Mutable START = new Mutable();
	private static final Mutable POS = new Mutable();
	
	@Final
	@Shadow
	private ChunkSection[] sections;
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void cj_onChunkInit(World world, ChunkPos pos, BiomeArray biomes, UpgradeData upgradeData, TickScheduler<Block> blockTickScheduler, TickScheduler<Fluid> fluidTickScheduler, long inhabitedTime, @Nullable ChunkSection[] sections, @Nullable Consumer<WorldChunk> loadToWorldConsumer, CallbackInfo info) {
		if (this.sections != null) {
			START.set(pos.getStartX(), 0, pos.getStartZ());
			for (ChunkSection section: this.sections) {
				if (section != null) {
					START.setY(section.getYOffset());
					for (int i = 0; i < 4096; i++) {
						int x = i & 15;
						int y = (i >> 4) & 15;
						int z = i >> 8;
						BlockState state = section.getBlockState(x, y, z);
						if (state.getLuminance() > 0) {
							cf_setSource(POS.set(START).move(x, y, z), state);
						}
					}
				}
			}
		}
	}
	
	@Inject(method = "setBlockState", at = @At("RETURN"))
	private void cf_setBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> info) {
		if (info.getReturnValue() != null) {
			int light = state.getLuminance();
			if (light > 0) {
				cf_setSource(pos, state);
			}
			else {
				BlockState stateOld = info.getReturnValue();
				light = stateOld.getLuminance();
				if (light > 0) {
					System.out.println("Recalculate " + pos);
					ColoredStorage.removeLight(pos.getX(), pos.getY(), pos.getZ(), 2);
				}
			}
		}
	}
	
	private void cf_setSource(BlockPos pos, BlockState state) {
		ColoredStorage.addLightSource(pos.getX(), pos.getY(), pos.getZ(), state);
	}
}
