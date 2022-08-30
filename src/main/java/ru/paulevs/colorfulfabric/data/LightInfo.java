package ru.paulevs.colorfulfabric.data;

public class LightInfo {
	private final int[] simple;
	private final int[] advanced;
	
	public LightInfo(int color, int radius) {
		color = reverse(color);
		
		simple = new int[] {
			color,
			multiply(color, 0.75F),
			multiply(color, 0.29F),
		};
		
		advanced = new int[radius];
		advanced[0] = color;
		for (byte i = 1; i < radius; i++) {
			float power = 1.0F - (float) i / radius;
			advanced[i] = multiply(color, power);
		}
	}
	
	private int multiply(int color, float value) {
		int i1 = (int) (((color >> 16) & 255) * value) << 16;
		int i2 = (int) (((color >> 8) & 255) * value) << 8;
		int i3 = (int) ((color & 255) * value);
		return i1 | i2 | i3;
	}
	
	private int reverse(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return b << 16 | g << 8 | r;
	}
	
	public int getSimple(byte i) {
		return i == 0 ? simple[0] : i < 7 ? simple[1] : simple[2];
	}
	
	public int getAdvanced(byte i) {
		return advanced[i];
	}
	
	public int getRadius() {
		return advanced.length;
	}
}
