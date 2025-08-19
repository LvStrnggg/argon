package dev.lvstrng.muam3r.mixin;

import dev.lvstrng.muam3r.gui.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow
	@Nullable
	protected MinecraftClient client;

	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void dontRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.client.currentScreen instanceof ClickGui) {
			ci.cancel();
		}
	}
}
