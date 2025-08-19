package dev.lvstrng.muam3r.module.modules.misc;

import dev.lvstrng.muam3r.event.events.PacketReceiveListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class PackSpoof extends Module implements PacketReceiveListener {
    public PackSpoof() {
        super(EncryptedString.of("Pack Spoof"), EncryptedString.of("Ignores custom resource packs"), -1, Category.MISC);
    }

    @Override
    public void onEnable() {
        eventManager.add(PacketReceiveListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(PacketReceiveListener.class, this);
        super.onDisable();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(mc.getNetworkHandler() != null) {
            Packet<?> packet = event.packet;
            if (packet instanceof ResourcePackSendS2CPacket) {
                event.cancel();

                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            }
        }
    }
}
