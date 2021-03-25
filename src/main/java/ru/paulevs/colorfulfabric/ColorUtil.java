package ru.paulevs.colorfulfabric;

import net.minecraft.util.math.MathHelper;

public class ColorUtil {
	public static final int BLACK = 0xFF000000;
	public static final int WHITE = 0xFFFFFFFF;
	
	public static int max(int color1, int color2) {
		int r = MHelper.max(getRed(color1), getRed(color2));
		int g = MHelper.max(getGreen(color1), getGreen(color2));
		int b = MHelper.max(getBlue(color1), getBlue(color2));
		return color(r, g, b);
	}
	
	public static int lerp(int color1, int color2, float mix) {
		int r = MathHelper.floor(MathHelper.lerp(mix, getRed(color1), getRed(color2)) + 0.5F);
		int g = MathHelper.floor(MathHelper.lerp(mix, getGreen(color1), getGreen(color2)) + 0.5F);
		int b = MathHelper.floor(MathHelper.lerp(mix, getBlue(color1), getBlue(color2)) + 0.5F);
		return color(r, g, b);
	}
	
	public static int color(int r, int g, int b) {
		return BLACK | r << 16 | g << 8 | b;
	}
	
	public static int getRed(int rgb) {
		return (rgb >> 16) & 255;
	}
	
	public static int getGreen(int rgb) {
		return (rgb >> 8) & 255;
	}
	
	public static int getBlue(int rgb) {
		return rgb & 255;
	}
}
