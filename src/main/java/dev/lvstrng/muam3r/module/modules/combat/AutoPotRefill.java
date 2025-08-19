package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.mixin.HandledScreenMixin;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.ModeSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.InventoryUtils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoPotRefill extends Module implements TickListener {
	public enum Mode {
		Auto, Hover
	}

	private final ModeSetting<Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), Mode.Auto, Mode.class);
	private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0, 10, 0, 1);

	private int clock;

	public AutoPotRefill() {
		super(EncryptedString.of("Auto Pot Refill"),
				EncryptedString.of("Refills your hotbar with potions"),
				-1,
				Category.COMBAT);
		addSettings(mode, delay);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);

		clock = 0;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen instanceof InventoryScreen inventoryScreen) {
			if (mode.isMode(Mode.Hover)) {
				Slot focusedSlot = ((HandledScreenMixin) inventoryScreen).getFocusedSlot();

				if (focusedSlot == null)
					return;

				PlayerInventory inventory = mc.player.getInventory();

				int emptySlot = -1;
				for (int i = 0; i <= 8; i++) {
					if (inventory.getStack(i).isEmpty()) {
						emptySlot = i;
						break;
					}
				}

				if (emptySlot == -1)
					return;

				if (InventoryUtils.isThatSplash(StatusEffects.INSTANT_HEALTH.value(), 1, 1, focusedSlot.getStack())) {
					if (clock < delay.getValueInt()) {
						clock++;
						return;
					}

					mc.interactionManager.clickSlot(
							inventoryScreen.getScreenHandler().syncId,
							focusedSlot.getIndex(),
							emptySlot,
							SlotActionType.SWAP,
							mc.player);

					clock = 0;
				}
			}

			if (mode.isMode(Mode.Auto)) {
				int slot = InventoryUtils.findPot(StatusEffects.INSTANT_HEALTH.value(), 1, 1);

				if (slot != -1) {
					PlayerInventory inventory = mc.player.getInventory();

					int emptySlot = -1;
					for (int i = 0; i <= 8; i++) {
						if (inventory.getStack(i).isEmpty()) {
							emptySlot = i;
							break;
						}
					}

					if (emptySlot == -1) return;

					if (clock < delay.getValueInt()) {
						clock++;
						return;
					}

					mc.interactionManager.clickSlot(
							inventoryScreen.getScreenHandler().syncId,
							slot,
							emptySlot,
							SlotActionType.SWAP,
							mc.player);

					clock = 0;
				}
			}
		}
	}
}
