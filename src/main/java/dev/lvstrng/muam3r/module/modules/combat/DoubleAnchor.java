package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.BlockUtils;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class DoubleAnchor extends Module implements TickListener {
	public DoubleAnchor() {
		super(EncryptedString.of("Double Anchor"),
				EncryptedString.of("Helps you do the air place/double anchor"),
				-1,
				Category.COMBAT);
	}

	private BlockPos pos;
	private int count;

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		pos = null;
		count = 0;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen == null) {
			assert mc.player != null;
			if (mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) {
				assert mc.world != null;
				if (mc.crosshairTarget instanceof BlockHitResult h && BlockUtils.isAnchorCharged(h.getBlockPos())) {
					if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) {
						if (h.getBlockPos().equals(pos)) {
							if (count >= 1) return;
						} else {
							pos = h.getBlockPos();
							count = 0;
						}

						mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, h, 0));
						count++;
					}
				}
			}
		}
	}
}
