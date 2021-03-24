package ru.paulevs.colorfulfabric;

public class MHelper {
	public static int max(int a, int b) {
		return a > b ? a : b;
	}
	
	public static int max(int a, int b, int c) {
		return max(a, max(b, c));
	}
}
