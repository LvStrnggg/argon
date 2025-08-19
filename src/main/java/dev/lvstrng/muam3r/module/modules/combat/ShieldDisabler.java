package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.AttackListener;
import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.KeybindSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.InventoryUtils;
import dev.lvstrng.muam3r.utils.MouseSimulation;
import dev.lvstrng.muam3r.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class ShieldDisabler extends Module implements TickListener, AttackListener {
	private final NumberSetting hitDelay = new NumberSetting(EncryptedString.of("Hit Delay"), 0, 20, 0, 1);
	private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0, 20, 0, 1);
	private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
	private final BooleanSetting stun = new BooleanSetting(EncryptedString.of("Stun"), false);
	private final BooleanSetting clickSimulate = new BooleanSetting(EncryptedString.of("Click Simulation"), false);
	private final BooleanSetting requireHoldAxe = new BooleanSetting(EncryptedString.of("Hold Axe"), false);

	int previousSlot, hitClock, switchClock;

	public ShieldDisabler() {
		super(EncryptedString.of("Shield Disabler"),
				EncryptedString.of("Automatically disables your opponents shield"),
				-1,
				Category.COMBAT);

		addSettings(switchDelay, hitDelay, switchBack, stun, clickSimulate, requireHoldAxe);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		eventManager.add(AttackListener.class, this);

		hitClock = hitDelay.getValueInt();
		switchClock = switchDelay.getValueInt();
		previousSlot = -1;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(AttackListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null)
			return;

		if(requireHoldAxe.getValue() && !(mc.player.getMainHandStack().getItem() instanceof AxeItem))
			return;

		if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
			Entity entity = entityHit.getEntity();

			if (mc.player.isUsingItem())
				return;

			if (entity instanceof PlayerEntity player) {
				if (WorldUtils.isShieldFacingAway(player))
					return;

				if (player.isHolding(Items.SHIELD) && player.isBlocking()) {
					if (switchClock > 0) {
						if (previousSlot == -1)
							previousSlot = mc.player.getInventory().selectedSlot;

						switchClock--;
						return;
					}

					if (InventoryUtils.selectAxe()) {
						if (hitClock > 0) {
							hitClock--;
						} else {
							if (clickSimulate.getValue())
								MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

							WorldUtils.hitEntity(player, true);

							if (stun.getValue()) {
								if (clickSimulate.getValue())
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

								WorldUtils.hitEntity(player, true);
							}

							hitClock = hitDelay.getValueInt();
							switchClock = switchDelay.getValueInt();
						}
					}
				} else if (previousSlot != -1) {
					if (switchBack.getValue())
						InventoryUtils.setInvSlot(previousSlot);

					previousSlot = -1;
				}
			}
		}
	}

	@Override
	public void onAttack(AttackListener.AttackEvent event) {
		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
			event.cancel();
	}
}
