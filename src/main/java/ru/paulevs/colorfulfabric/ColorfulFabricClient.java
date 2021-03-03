package ru.paulevs.colorfulfabric;

import net.fabricmc.api.ClientModInitializer;

public class ColorfulFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorLightManager.start();
	}
}
