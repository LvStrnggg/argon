package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.ModeSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.FakeInvScreen;
import dev.lvstrng.muam3r.utils.InventoryUtils;
import dev.lvstrng.muam3r.utils.TimerUtils;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;


public final class AutoInventoryTotem extends Module implements TickListener {
	public enum Mode {
		Blatant, Random
	}

	private final ModeSetting<Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), Mode.Blatant, Mode.class)
			.setDescription(EncryptedString.of("Whether to randomize the toteming pattern or no"));
	private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0, 20, 0, 1);
	private final BooleanSetting hotbar = new BooleanSetting(EncryptedString.of("Hotbar"), true).setDescription(EncryptedString.of("Puts a totem in your hotbar as well, if enabled (Setting below will work if this is enabled)"));
	private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1, 9, 1, 1)
			.setDescription(EncryptedString.of("Your preferred totem slot"));
	private final BooleanSetting autoSwitch = new BooleanSetting(EncryptedString.of("Auto Switch"), false)
			.setDescription(EncryptedString.of("Switches to totem slot when going inside the inventory"));
	private final BooleanSetting forceTotem = new BooleanSetting(EncryptedString.of("Force Totem"), false).setDescription(EncryptedString.of("Puts the totem in the slot, regardless if its space is taken up by something else"));
	private final BooleanSetting autoOpen = new BooleanSetting(EncryptedString.of("Auto Open"), false)
			.setDescription(EncryptedString.of("Automatically opens and closes the inventory for you"));
	private final NumberSetting stayOpenFor = new NumberSetting(EncryptedString.of("Stay Open For"), 0, 20, 0, 1);

	int clock = -1;
	int closeClock = -1;

	TimerUtils openTimer = new TimerUtils();
	TimerUtils closeTimer = new TimerUtils();

	public AutoInventoryTotem() {
		super(EncryptedString.of("Auto Inventory Totem"),
				EncryptedString.of("Automatically equips a totem in your offhand and main hand if empty"),
				-1,
				Category.COMBAT);
		addSettings(mode, delay, hotbar, totemSlot, autoSwitch, forceTotem, autoOpen, stayOpenFor);
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		clock = -1;
		closeClock = -1;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (shouldOpenScreen() && autoOpen.getValue())
			mc.setScreen(new FakeInvScreen(mc.player));

		if (!(mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof FakeInvScreen)) {
			clock = -1;
			closeClock = -1;
			return;
		}

		if (clock == -1)
			clock = delay.getValueInt();

		if (closeClock == -1)
			closeClock = stayOpenFor.getValueInt();

		if (clock > 0)
			clock--;

		PlayerInventory inventory = mc.player.getInventory();

		if (autoSwitch.getValue())
			inventory.selectedSlot = totemSlot.getValueInt() - 1;

		if (clock <= 0) {
			if (inventory.offHand.get(0).getItem() != Items.TOTEM_OF_UNDYING) {
				int slot = mode.isMode(Mode.Blatant) ? InventoryUtils.findTotemSlot() : InventoryUtils.findRandomTotemSlot();

				if (slot != -1) {
					mc.interactionManager.clickSlot(((InventoryScreen) mc.currentScreen).getScreenHandler().syncId, slot, 40, SlotActionType.SWAP, mc.player);
					return;
				}
			}

			if(hotbar.getValue()) {
				ItemStack mainHand = mc.player.getMainHandStack();
				if (mainHand.isEmpty() || forceTotem.getValue() && mainHand.getItem() != Items.TOTEM_OF_UNDYING) {
					int slot = mode.isMode(Mode.Blatant) ? InventoryUtils.findTotemSlot() : InventoryUtils.findRandomTotemSlot();

					if (slot != -1) {
						mc.interactionManager.clickSlot(((InventoryScreen) mc.currentScreen).getScreenHandler().syncId, slot, inventory.selectedSlot, SlotActionType.SWAP, mc.player);
						return;
					}
				}
			}


			if (shouldCloseScreen() && autoOpen.getValue()) {
				if (closeClock != 0) {
					closeClock--;
					return;
				}

				mc.currentScreen.close();
				closeClock = stayOpenFor.getValueInt();
			}
		}
	}

	public boolean shouldCloseScreen() {
		if(hotbar.getValue())
			return (mc.player.getInventory().getStack(totemSlot.getValueInt() - 1).getItem() == Items.TOTEM_OF_UNDYING && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && mc.currentScreen instanceof FakeInvScreen;
		else return ( mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && mc.currentScreen instanceof FakeInvScreen;
	}

	public boolean shouldOpenScreen() {
		if(hotbar.getValue())
			return (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || mc.player.getInventory().getStack(totemSlot.getValueInt() - 1).getItem() != Items.TOTEM_OF_UNDYING)
					&& !(mc.currentScreen instanceof FakeInvScreen) && InventoryUtils.countItemExceptHotbar(item -> item == Items.TOTEM_OF_UNDYING) != 0;
		else return (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING && !(mc.currentScreen instanceof FakeInvScreen) && InventoryUtils.countItemExceptHotbar(item -> item == Items.TOTEM_OF_UNDYING) != 0);
	}
}
