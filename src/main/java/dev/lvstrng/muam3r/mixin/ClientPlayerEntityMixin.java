package dev.lvstrng.muam3r.mixin;

import com.mojang.authlib.GameProfile;
import dev.lvstrng.muam3r.event.EventManager;
import dev.lvstrng.muam3r.event.events.MovementPacketListener;
import dev.lvstrng.muam3r.event.events.PlayerTickListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

	@Shadow
	@Final
	protected MinecraftClient client;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "sendMovementPackets", at = @At("HEAD"))
	private void onSendMovementPackets(CallbackInfo ci) {
		EventManager.fire(new MovementPacketListener.MovementPacketEvent());
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onPlayerTick(CallbackInfo ci) {
		EventManager.fire(new PlayerTickListener.PlayerTickEvent());
	}
	//@Inject(method = "sendMovementPackets", at = @At("HEAD"))
}
