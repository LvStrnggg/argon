package dev.lvstrng.muam3r.module.modules.misc;

import dev.lvstrng.muam3r.event.events.AttackListener;
import dev.lvstrng.muam3r.event.events.BlockBreakingListener;
import dev.lvstrng.muam3r.event.events.ItemUseListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.utils.BlockUtils;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;


public final class Prevent extends Module implements ItemUseListener, AttackListener, BlockBreakingListener {
	private final BooleanSetting doubleGlowstone = new BooleanSetting(EncryptedString.of("Double Glowstone"), false)
			.setDescription(EncryptedString.of("Makes it so you can't charge the anchor again if it's already charged"));
	private final BooleanSetting glowstoneMisplace = new BooleanSetting(EncryptedString.of("Glowstone Misplace"), false)
			.setDescription(EncryptedString.of("Makes it so you can only right-click with glowstone only when aiming at an anchor"));
	private final BooleanSetting anchorOnAnchor = new BooleanSetting(EncryptedString.of("Anchor on Anchor"), false)
			.setDescription(EncryptedString.of("Makes it so you can't place an anchor on/next to another anchor unless charged"));
	private final BooleanSetting obiPunch = new BooleanSetting(EncryptedString.of("Obi Punch"), false)
			.setDescription(EncryptedString.of("Makes it so you can crystal faster by not letting you left click/start breaking the obsidian"));
	private final BooleanSetting echestClick = new BooleanSetting(EncryptedString.of("E-chest click"), false)
			.setDescription(EncryptedString.of("Makes it so you can't click on e-chests with PvP items"));

	public Prevent() {
		super(EncryptedString.of("Prevent"),
				EncryptedString.of("Prevents you from certain actions"),
				-1,
				Category.MISC);
		addSettings(doubleGlowstone, glowstoneMisplace, anchorOnAnchor, obiPunch, echestClick);
	}

	@Override
	public void onEnable() {
		eventManager.add(BlockBreakingListener.class, this);
		eventManager.add(AttackListener.class, this);
		eventManager.add(ItemUseListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(BlockBreakingListener.class, this);
		eventManager.remove(AttackListener.class, this);
		eventManager.remove(ItemUseListener.class, this);
		super.onDisable();
	}

	@Override
	public void onAttack(AttackEvent event) {
		if (mc.crosshairTarget instanceof BlockHitResult hit) {
			if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.OBSIDIAN) && obiPunch.getValue() && mc.player.isHolding(Items.END_CRYSTAL))
				event.cancel();
		}
	}

	@Override
	public void onBlockBreaking(BlockBreakingEvent event) {
		if (mc.crosshairTarget instanceof BlockHitResult hit) {
			if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.OBSIDIAN) && obiPunch.getValue() && mc.player.isHolding(Items.END_CRYSTAL))
				event.cancel();
		}
	}

	@Override
	public void onItemUse(ItemUseEvent event) {
		if (mc.crosshairTarget instanceof BlockHitResult hit) {
			if (BlockUtils.isAnchorCharged(hit.getBlockPos()) && doubleGlowstone.getValue() && mc.player.isHolding(Items.GLOWSTONE))
				event.cancel();

			if (!BlockUtils.isBlock(hit.getBlockPos(), Blocks.RESPAWN_ANCHOR) && glowstoneMisplace.getValue() && mc.player.isHolding(Items.GLOWSTONE))
				event.cancel();

			if (BlockUtils.isAnchorNotCharged(hit.getBlockPos()) && anchorOnAnchor.getValue() && mc.player.isHolding(Items.RESPAWN_ANCHOR))
				event.cancel();

			if (BlockUtils.isBlock(hit.getBlockPos(), Blocks.ENDER_CHEST) && echestClick.getValue() &&
					(mc.player.getMainHandStack().getItem() instanceof SwordItem
							|| mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
							|| mc.player.getMainHandStack().getItem() == Items.OBSIDIAN
							|| mc.player.getMainHandStack().getItem() == Items.RESPAWN_ANCHOR
							|| mc.player.getMainHandStack().getItem() == Items.GLOWSTONE))
				event.cancel();
		}
	}
}
