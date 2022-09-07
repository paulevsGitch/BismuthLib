package ru.paulevs.bismuthlib;

public class ColorMath {
	public static final int WHITE = 0xFFFFFFFF;
	public static final int ALPHA = 255 << 24;
	
	public static int maxBlend(int a, int b) {
		int i1 = Math.max(a & 0x00FF0000, b & 0x00FF0000);
		int i2 = Math.max(a & 0x0000FF00, b & 0x0000FF00);
		int i3 = Math.max(a & 0x000000FF, b & 0x000000FF);
		return i1 | i2 | i3;
	}
	
	public static int mulBlend(int a, int b) {
		int i1 = (int) ((((a >> 16) & 255) / 255F) * (((b >> 16) & 255) / 255F) * 255);
		int i2 = (int) ((((a >> 8) & 255) / 255F) * (((b >> 8) & 255) / 255F) * 255);
		int i3 = (int) (((a & 255) / 255F) * ((b & 255) / 255F) * 255);
		return i1 << 16 | i2 << 8 | i3;
	}
	
	public static int multiply(int color, float value) {
		int i1 = (int) (((color >> 16) & 255) * value) << 16;
		int i2 = (int) (((color >> 8) & 255) * value) << 8;
		int i3 = (int) ((color & 255) * value);
		return i1 | i2 | i3;
	}
	
	public static int reverse(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return b << 16 | g << 8 | r;
	}
}
