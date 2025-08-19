package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.mixin.HandledScreenMixin;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class HoverTotem extends Module implements TickListener {
	private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0, 20, 0, 1);
	private final BooleanSetting hotbar = new BooleanSetting(EncryptedString.of("Hotbar"), true).setDescription(EncryptedString.of("Puts a totem in your hotbar as well, if enabled (Setting below will work if this is enabled)"));
	private final NumberSetting slot = new NumberSetting(EncryptedString.of("Totem Slot"), 1, 9, 1, 1)
			.setDescription(EncryptedString.of("Your preferred totem slot"));
	private final BooleanSetting autoSwitch = new BooleanSetting(EncryptedString.of("Auto Switch"), false)
			.setDescription(EncryptedString.of("Switches to totem slot when going inside the inventory"));

	private int clock;

	public HoverTotem() {
		super(EncryptedString.of("Hover Totem"),
				EncryptedString.of("Equips a totem in your totem and offhand slots if a totem is hovered"),
				-1,
				Category.COMBAT);
		addSettings(delay, hotbar, slot, autoSwitch);
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
		if (mc.currentScreen instanceof InventoryScreen inv) {
			Slot hoveredSlot = ((HandledScreenMixin) inv).getFocusedSlot();

			if (autoSwitch.getValue())
				mc.player.getInventory().selectedSlot = slot.getValueInt() - 1;

			if (hoveredSlot != null) {
				int slot = hoveredSlot.getIndex();

				if (slot > 35)
					return;

				int totem = this.slot.getValueInt() - 1;

				if (hoveredSlot.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
					if (hotbar.getValue() && mc.player.getInventory().getStack(totem).getItem() != Items.TOTEM_OF_UNDYING) {
						if (clock > 0) {
							clock--;
							return;
						}

						mc.interactionManager.clickSlot(inv.getScreenHandler().syncId, slot, totem, SlotActionType.SWAP, mc.player);
						clock = delay.getValueInt();
					} else if (!mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
						if (clock > 0) {
							clock--;
							return;
						}

						mc.interactionManager.clickSlot(inv.getScreenHandler().syncId, slot, 40, SlotActionType.SWAP, mc.player);
						clock = delay.getValueInt();
					}
				}
			}
		} else clock = delay.getValueInt();
	}
}
