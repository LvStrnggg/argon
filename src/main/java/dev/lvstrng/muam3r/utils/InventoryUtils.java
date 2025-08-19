package dev.lvstrng.muam3r.utils;

import dev.lvstrng.muam3r.mixin.ClientPlayerInteractionManagerAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class InventoryUtils {

	public static void setInvSlot(int slot) {
		mc.player.getInventory().selectedSlot = slot;
		((ClientPlayerInteractionManagerAccessor) mc.interactionManager).syncSlot();
	}

	public static boolean selectItemFromHotbar(Predicate<Item> item) {
		PlayerInventory inv = mc.player.getInventory();

		for (int i = 0; i < 9; i++) {
			ItemStack itemStack = inv.getStack(i);
			if (!item.test(itemStack.getItem()))
				continue;

			inv.selectedSlot = i;
			return true;
		}

		return false;
	}

	public static boolean selectItemFromHotbar(Item item) {
		return selectItemFromHotbar(i -> i == item);
	}

	public static boolean hasItemInHotbar(Predicate<Item> item) {
		PlayerInventory inv = mc.player.getInventory();

		for (int i = 0; i < 9; i++) {
			ItemStack itemStack = inv.getStack(i);
			if (item.test(itemStack.getItem()))
				return true;
		}
		return false;
	}

	public static int countItem(Predicate<Item> item) {
		PlayerInventory inv = mc.player.getInventory();

		int count = 0;

		for (int i = 0; i < 36; i++) {
			ItemStack itemStack = inv.getStack(i);
			if (item.test(itemStack.getItem()))
				count += itemStack.getCount();
		}

		return count;
	}

	public static int countItemExceptHotbar(Predicate<Item> item) {
		PlayerInventory inv = mc.player.getInventory();

		int count = 0;

		for (int i = 9; i < 36; i++) {
			ItemStack itemStack = inv.getStack(i);
			if (item.test(itemStack.getItem()))
				count += itemStack.getCount();
		}

		return count;
	}

	public static int getSwordSlot() {
		Inventory playerInventory = mc.player.getInventory();

		for (int itemIndex = 0; itemIndex < 9; itemIndex++) {
			if (playerInventory.getStack(itemIndex).getItem() instanceof SwordItem)
				return itemIndex;
		}

		return -1;
	}

	public static boolean selectSword() {
		int itemIndex = getSwordSlot();

		if (itemIndex != -1) {
			InventoryUtils.setInvSlot(itemIndex);
			return true;
		} else return false;
	}

	public static int findSplash(StatusEffect type, int duration, int amplifier) {
		PlayerInventory inv = mc.player.getInventory();
		StatusEffectInstance potion = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(type), duration, amplifier);

		for (int i = 0; i < 9; i++) {
			ItemStack itemStack = inv.getStack(i);

			if (!(itemStack.getItem() instanceof SplashPotionItem))
				continue;

			//String s = PotionUtil.getPotion(itemStack).getEffects().toString();
			String s = itemStack.get(DataComponentTypes.POTION_CONTENTS).getEffects().toString();
			if (s.contains(potion.toString())) {
				return i;
			}
		}

		return -1;
	}

	public static boolean isThatSplash(StatusEffect type, int duration, int amplifier, ItemStack itemStack) {
		StatusEffectInstance potion = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(type), duration, amplifier);

		return itemStack.getItem() instanceof SplashPotionItem &&
				itemStack.get(DataComponentTypes.POTION_CONTENTS).getEffects().toString().contains(potion.toString());
	}

	public static int findTotemSlot() {
		assert mc.player != null;
		PlayerInventory inv = mc.player.getInventory();
		for (int index = 9; index < 36; index++) {
			if (inv.main.get(index).getItem() == Items.TOTEM_OF_UNDYING)
				return index;
		}
		return -1;
	}

	public static boolean selectAxe() {
		int itemIndex = getAxeSlot();

		if (itemIndex != -1) {
			mc.player.getInventory().selectedSlot = itemIndex;
			return true;
		} else return false;
	}

	public static int findRandomTotemSlot() {
		PlayerInventory inventory = mc.player.getInventory();
		Random random = new Random();
		List<Integer> totemIndexes = new ArrayList<>();

		for(int i = 9; i < 36; i++) {
			if(inventory.main.get(i).getItem() == Items.TOTEM_OF_UNDYING)
				totemIndexes.add(i);
		}

		if(!totemIndexes.isEmpty()) {
			return totemIndexes.get(random.nextInt(totemIndexes.size()));
		} else return -1;
	}

	//effect.minecraft.instant_health
	//effect.minecraft.strength
	//effect.minecraft.speed
	public static int findRandomPot(String potion) {
		PlayerInventory inventory = mc.player.getInventory();
		Random random = new Random();

		int slotIndex = random.nextInt(27) + 9;
		for (int i = 0; i < 27; i++) {
			int index = (slotIndex + i) % 36;
			ItemStack itemStack = inventory.main.get(index);
			if (itemStack.getItem() instanceof SplashPotionItem && (index != 36 || index != 37 || index != 38 || index != 39)) {
				if (!itemStack.get(DataComponentTypes.POTION_CONTENTS).getEffects().toString().contains(potion.toString()))
					return -1;

				return index;
			}
		}
		return -1;
	}

	public static int findPot(StatusEffect effect, int duration, int amplifier) {
		assert mc.player != null;
		PlayerInventory inv = mc.player.getInventory();
		StatusEffectInstance instance = new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(effect), duration, amplifier);

		for (int index = 9; index < 34; index++)
			if (inv.main.get(index).getItem() instanceof SplashPotionItem)
				if (inv.main.get(index).get(DataComponentTypes.POTION_CONTENTS).getEffects().toString().contains(instance.toString()))
					return index;

		return -1;
	}

	public static List<Integer> getEmptyHotbarSlots() {
		PlayerInventory inventory = mc.player.getInventory();
		List<Integer> slots = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			if (inventory.main.get(i).isEmpty())
				slots.add(i);
			else if (slots.contains(i) && !inventory.main.get(i).isEmpty())
				slots.remove(i);
		}

		return slots;
	}

	public static int getAxeSlot() {
		Inventory playerInventory = mc.player.getInventory();

		for (int itemIndex = 0; itemIndex < 9; itemIndex++) {
			if (playerInventory.getStack(itemIndex).getItem() instanceof AxeItem)
				return itemIndex;
		}

		return -1;
	}

	public static int countItem(Item item) {
		return countItem(i -> i == item);
	}
}