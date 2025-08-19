package dev.lvstrng.muam3r.mixin;

import dev.lvstrng.muam3r.event.EventManager;
import dev.lvstrng.muam3r.event.events.PacketReceiveListener;
import dev.lvstrng.muam3r.event.events.PacketSendListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

	@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
	private static <T extends PacketListener> void onPacketReceive(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
		PacketReceiveListener.PacketReceiveEvent event = new PacketReceiveListener.PacketReceiveEvent(packet);

		EventManager.fire(event);
		if (event.isCancelled()) ci.cancel();
	}

	@Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
	private void onPacketSend(Packet<?> packet, CallbackInfo ci) {
		PacketSendListener.PacketSendEvent event = new PacketSendListener.PacketSendEvent(packet);

		EventManager.fire(event);
		if (event.isCancelled()) ci.cancel();
	}
}
