package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.AttackListener;
import dev.lvstrng.muam3r.event.events.BlockBreakingListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.HitResult;

public final class NoMissDelay extends Module implements AttackListener, BlockBreakingListener {
	private final BooleanSetting onlyWeapon = new BooleanSetting(EncryptedString.of("Only weapon"), true);
	private final BooleanSetting air = new BooleanSetting(EncryptedString.of("Air"), true)
			.setDescription(EncryptedString.of("Whether to stop hits directed to the air"));
	private final BooleanSetting blocks = new BooleanSetting(EncryptedString.of("Blocks"), false)
			.setDescription(EncryptedString.of("Whether to stop hits directed to blocks"));

	public NoMissDelay() {
		super(EncryptedString.of("No Miss Delay"),
				EncryptedString.of("Doesn't let you miss your sword/axe hits"),
				-1,
				Category.COMBAT);
		addSettings(onlyWeapon, air, blocks);
	}

	@Override
	public void onEnable() {
		eventManager.add(AttackListener.class, this);
		eventManager.add(BlockBreakingListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(AttackListener.class, this);
		eventManager.remove(BlockBreakingListener.class, this);
		super.onDisable();
	}

	@Override
	public void onAttack(AttackEvent event) {
		if (onlyWeapon.getValue()
				&& !(mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem))
			return;

		switch (mc.crosshairTarget.getType()) {
			case MISS -> {
				if (air.getValue()) event.cancel();
			}
			case BLOCK -> {
				if (blocks.getValue()) event.cancel();
			}
		}
	}

	@Override
	public void onBlockBreaking(BlockBreakingEvent event) {
		if (onlyWeapon.getValue()
				&& !(mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem))
			return;

		if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
			if (blocks.getValue()) event.cancel();
		}
	}
}
