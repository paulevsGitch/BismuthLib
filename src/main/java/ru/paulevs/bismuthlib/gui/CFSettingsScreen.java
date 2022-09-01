package ru.paulevs.bismuthlib.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class CFSettingsScreen extends OptionsSubScreen {
	private OptionsList list;
	
	public CFSettingsScreen(Screen screen, Options options) {
		super(screen, options, Component.translatable("bismuthlib.options.settings.title"));
	}
	
	@Override
	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		this.list.addBig(CFOptions.MAP_RADIUS_XZ);
		this.list.addBig(CFOptions.MAP_RADIUS_Y);
		this.list.addSmall(CFOptions.OPTIONS);
		this.addWidget(this.list);
		
		Window window = this.minecraft.getWindow();
		this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, button -> {
			CFOptions.save();
			ru.paulevs.bismuthlib.BismuthLibClient.initData();
			window.changeFullscreenVideoMode();
			this.minecraft.setScreen(this.lastScreen);
		}));
	}
	
	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.list.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 5, 0xFFFFFF);
		super.render(poseStack, i, j, f);
		List<FormattedCharSequence> list = tooltipAt(this.list, i, j);
		this.renderTooltip(poseStack, list, i, j);
	}
}
