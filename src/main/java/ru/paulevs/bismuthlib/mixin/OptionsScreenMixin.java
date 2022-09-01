package ru.paulevs.bismuthlib.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.paulevs.bismuthlib.gui.CFSettingsScreen;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
	@Shadow @Final private Options options;
	
	protected OptionsScreenMixin(Component component) {
		super(component);
	}
	
	@Inject(method = "init()V", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/gui/screens/OptionsScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
		shift = Shift.AFTER
	))
	private void cf_onScreenInit(CallbackInfo info) {
		this.addRenderableWidget(new Button(
			this.width / 2 - 155,
			this.height / 6 + 120 - 6 + 24,
			150,
			20,
			Component.translatable("bismuthlib.options.settings"),
			button -> this.minecraft.setScreen(new CFSettingsScreen(this, this.options))
		));
	}
}
