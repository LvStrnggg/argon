package dev.lvstrng.muam3r.module.modules.misc;

import dev.lvstrng.muam3r.event.events.PacketReceiveListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.MinMaxSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.MathUtils;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

public final class PingSpoof extends Module implements PacketReceiveListener {
	private final MinMaxSetting ping = new MinMaxSetting(EncryptedString.of("Ping"), 0, 1000, 1, 0, 600)
			.setDescription(EncryptedString.of("The ping you want to achieve"));

	private int delay;
	public PingSpoof() {
		super(EncryptedString.of("Ping Spoof"),
				EncryptedString.of("Holds back packets making the server think your internet connection is bad."), -1, Category.MISC);
		addSettings(ping);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketReceiveListener.class, this);

		delay = ping.getRandomValueInt();
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketReceiveListener.class, this);
		super.onDisable();
	}

	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.packet instanceof KeepAliveS2CPacket packet) {
			new Thread(() -> {
				try {
					Thread.sleep(delay);
					mc.getNetworkHandler().getConnection().send(new KeepAliveC2SPacket(packet.getId()));
					delay = ping.getRandomValueInt();
				} catch (InterruptedException ignored) {}
			}).start();

			event.cancel();
		}
	}
}
