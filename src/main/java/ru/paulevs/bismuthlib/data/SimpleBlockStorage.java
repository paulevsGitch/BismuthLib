package ru.paulevs.bismuthlib.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class SimpleBlockStorage implements BlockGetter {
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private MutableBlockPos pos = new MutableBlockPos();
	private BlockState[] storage = new BlockState[110592];
	
	public void fill(Level level, int x1, int y1, int z1) {
		int index = 0;
		for (byte dx = 0; dx < 48; dx++) {
			pos.setX(x1 + dx);
			for (byte dy = 0; dy < 48; dy++) {
				pos.setY(y1 + dy);
				for (byte dz = 0; dz < 48; dz++) {
					pos.setZ(z1 + dz);
					storage[index++] = level.getBlockState(pos);
				}
			}
		}
		pos.set(x1, y1, z1);
	}
	
	private int getIndex(int x, int y, int z) {
		return  x * 2304 + y * 48 + z;
	}
	
	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}
	
	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int px = blockPos.getX() - pos.getX();
		int py = blockPos.getY() - pos.getY();
		int pz = blockPos.getZ() - pos.getZ();
		if (px < 0 || px > 47 || py < 0 || py > 47 || pz < 0 || pz > 47) return AIR;
		return storage[getIndex(px, py, pz)];
	}
	
	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return null;
	}
	
	@Override
	public int getHeight() {
		return 0;
	}
	
	@Override
	public int getMinBuildHeight() {
		return 0;
	}
}
