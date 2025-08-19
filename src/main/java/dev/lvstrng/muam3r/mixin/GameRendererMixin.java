package dev.lvstrng.muam3r.mixin;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.event.EventManager;
import dev.lvstrng.muam3r.event.events.GameRenderListener;
import dev.lvstrng.muam3r.module.modules.misc.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow public abstract Matrix4f getBasicProjectionMatrix(double fov);

	@Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

	@Shadow @Final private Camera camera;

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1))
	private void onWorldRender(RenderTickCounter tickCounter, CallbackInfo ci) {
		double d = getFov(camera, tickCounter.getTickDelta(true), true);
		Matrix4f matrix4f = getBasicProjectionMatrix(d);
		MatrixStack matrixStack = new MatrixStack();
		EventManager.fire(new GameRenderListener.GameRenderEvent(matrixStack, tickCounter.getTickDelta(true)));
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
		if (Muam3r.INSTANCE.getModuleManager().getModule(Freecam.class).isEnabled())
			cir.setReturnValue(false);
	}
}