package dev.lvstrng.muam3r.mixin;

import dev.lvstrng.muam3r.event.EventManager;
import dev.lvstrng.muam3r.event.events.HudListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void onRenderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		HudListener.HudEvent event = new HudListener.HudEvent(context, tickCounter.getTickDelta(true));

		EventManager.fire(event);
	}
}
