package ru.paulevs.bismuthlib.data;

import net.minecraft.core.BlockPos.MutableBlockPos;

public class ShaderSectionData {
	private final MutableBlockPos position = new MutableBlockPos(0, -1000, 0);
	private final int data[] = new int[4096];
	
	public boolean hasCorrectPosition(int x, int y, int z) {
		return x == position.getX() && y == position.getY() && z == position.getZ();
	}
	
	public void setPosition(int x, int y, int z) {
		position.set(x, y, z);
	}
	
	public int[] getData() {
		return data;
	}
}
