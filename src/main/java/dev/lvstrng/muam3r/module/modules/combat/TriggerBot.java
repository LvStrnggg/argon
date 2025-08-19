package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.event.events.AttackListener;
import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.modules.client.Friends;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.MinMaxSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.MouseSimulation;
import dev.lvstrng.muam3r.utils.TimerUtils;
import dev.lvstrng.muam3r.utils.WorldUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class TriggerBot extends Module implements TickListener, AttackListener {
	private final BooleanSetting inScreen = new BooleanSetting(EncryptedString.of("Work In Screen"), false)
			.setDescription(EncryptedString.of("Will trigger even if youre inside a screen"));
	private final BooleanSetting whileUse = new BooleanSetting(EncryptedString.of("While Use"), false)
			.setDescription(EncryptedString.of("Will hit the player no matter if you're eating or blocking with a shield"));
	private final BooleanSetting onLeftClick = new BooleanSetting(EncryptedString.of("On Left Click"), false)
			.setDescription(EncryptedString.of("Only gets triggered if holding down left click"));
	private final BooleanSetting allItems = new BooleanSetting(EncryptedString.of("All Items"), false)
			.setDescription(EncryptedString.of("Works with all Items /THIS USES SWORD DELAY AS THE DELAY/"));
	private final MinMaxSetting swordDelay = new MinMaxSetting(EncryptedString.of("Sword Delay"), 0, 1000, 1, 540, 550)
			.setDescription(EncryptedString.of("Delay for swords"));
	private final MinMaxSetting axeDelay = new MinMaxSetting(EncryptedString.of("Axe Delay"), 0, 1000, 1, 780, 800)
			.setDescription(EncryptedString.of("Delay for axes"));
	/*private final NumberSetting swordDelay = new NumberSetting(EncryptedString.of("Sword Delay"), 0, 1000, 550, 1)
			.setDescription(EncryptedString.of("Delay for swords"));*/
	/*private final NumberSetting axeDelay = new NumberSetting(EncryptedString.of("Axe Delay"), 0, 1000, 800, 1)
			.setDescription(EncryptedString.of("Delay for axes"));*/
	private final BooleanSetting checkShield = new BooleanSetting(EncryptedString.of("Check Shield"), false)
			.setDescription(EncryptedString.of("Checks if the player is blocking your hits with a shield (Recommended with Shield Disabler)"));
	private final BooleanSetting onlyCritSword = new BooleanSetting(EncryptedString.of("Only Crit Sword"), false)
			.setDescription(EncryptedString.of("Only does critical hits with a sword"));
	private final BooleanSetting onlyCritAxe = new BooleanSetting(EncryptedString.of("Only Crit Axe"), false)
			.setDescription(EncryptedString.of("Only does critical hits with an axe"));
	private final BooleanSetting swing = new BooleanSetting(EncryptedString.of("Swing Hand"), true)
			.setDescription(EncryptedString.of("Whether to swing the hand or not"));
	private final BooleanSetting whileAscend = new BooleanSetting(EncryptedString.of("While Ascending"), false)
			.setDescription(EncryptedString.of("Wont hit if you're ascending from a jump, only if on ground or falling"));
	private final BooleanSetting clickSimulation = new BooleanSetting(EncryptedString.of("Click Simulation"), false)
			.setDescription(EncryptedString.of("Makes the CPS hud think you're legit"));
	private final BooleanSetting strayBypass = new BooleanSetting(EncryptedString.of("Stray Bypass"), false)
			.setDescription(EncryptedString.of("Bypasses stray's Anti-TriggerBot"));
	private final BooleanSetting allEntities = new BooleanSetting(EncryptedString.of("All Entities"), false)
			.setDescription(EncryptedString.of("Will attack all entities"));
	private final BooleanSetting useShield = new BooleanSetting(EncryptedString.of("Use Shield"), false)
			.setDescription(EncryptedString.of("Uses shield if it's in your offhand"));
	private final NumberSetting shieldTime = new NumberSetting(EncryptedString.of("Shield Time"), 100, 1000, 350, 1);
	private final BooleanSetting sticky = new BooleanSetting(EncryptedString.of("Same Player"), false)
			.setDescription(EncryptedString.of("Hits the player that was recently attacked, good for FFA"));
	private final TimerUtils timer = new TimerUtils();

	private int currentSwordDelay, currentAxeDelay;

	public TriggerBot() {
		super(EncryptedString.of("Trigger Bot"),
				EncryptedString.of("Automatically hits players for you"),
				-1,
				Category.COMBAT);
		addSettings(inScreen, whileUse, onLeftClick, allItems, swordDelay, axeDelay, checkShield, whileAscend, sticky, onlyCritSword, onlyCritAxe, swing, clickSimulation, strayBypass, allEntities, useShield, shieldTime);
	}

	@Override
	public void onEnable() {
		currentSwordDelay = swordDelay.getRandomValueInt();
		currentAxeDelay = axeDelay.getRandomValueInt();

		eventManager.add(TickListener.class, this);
		eventManager.add(AttackListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(AttackListener.class, this);
		super.onDisable();
	}

	@SuppressWarnings("all")
	@Override
	public void onTick() {
		try {
			if (!inScreen.getValue() && mc.currentScreen != null)
				return;

			if(Muam3r.INSTANCE.getModuleManager().getModule(Friends.class).antiAttack.getValue() && Muam3r.INSTANCE.getFriendManager().isAimingOverFriend())
				return;

			Item item = mc.player.getMainHandStack().getItem();

			if (onLeftClick.getValue() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
				return;

			if (((mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD) || mc.player.getOffHandStack().getItem() instanceof ShieldItem) && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) && !whileUse.getValue())
				return;
			
			if (!whileAscend.getValue() && ((!mc.player.isOnGround() && mc.player.getVelocity().y > 0) || (!mc.player.isOnGround() && mc.player.fallDistance <= 0.0F)))
				return;

			if (!allItems.getValue()) {
				if (item instanceof SwordItem) {
					if (mc.crosshairTarget instanceof EntityHitResult hit) {
						Entity entity = hit.getEntity();

						assert mc.player.getAttacking() != null;
						if (sticky.getValue() && entity != mc.player.getAttacking())
							return;

						if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {

							if (entity instanceof PlayerEntity player) {
								if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
									return;
							}

							if (onlyCritSword.getValue() && mc.player.fallDistance <= 0.0F)
								return;

							if (timer.delay(currentSwordDelay)) {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD && mc.player.isBlocking())
										MouseSimulation.mouseRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
								}

								WorldUtils.hitEntity(entity, swing.getValue());

								if (clickSimulation.getValue())
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

								currentSwordDelay = swordDelay.getRandomValueInt();
								timer.reset();
							} else {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
										int useFor = shieldTime.getValueInt();
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
									}
								}
							}
						}
					}
				} else if (item instanceof AxeItem) {
					if (mc.crosshairTarget instanceof EntityHitResult hit) {
						Entity entity = hit.getEntity();

						if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {
							if (entity instanceof PlayerEntity player) {
								if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
									return;
							}

							if (onlyCritAxe.getValue() && mc.player.fallDistance <= 0.0F)
								return;

							if (timer.delay(currentAxeDelay)) {
								WorldUtils.hitEntity(entity, swing.getValue());

								if (clickSimulation.getValue())
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

								currentAxeDelay = axeDelay.getRandomValueInt();
								timer.reset();
							} else {
								if (useShield.getValue()) {
									if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
										int useFor = shieldTime.getValueInt();
										MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
									}
								}
							}
						}
					}
				}
			} else {
				if (mc.crosshairTarget instanceof EntityHitResult entityHit && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
					Entity entity = entityHit.getEntity();

					assert mc.player.getAttacking() != null;
					if (sticky.getValue() && entity != mc.player.getAttacking())
						return;

					if (entity instanceof PlayerEntity || (strayBypass.getValue() && entity instanceof ZombieEntity) || (allEntities.getValue() && entity != null)) {
						if (entity instanceof PlayerEntity player) {
							if (checkShield.getValue() && player.isBlocking() && !WorldUtils.isShieldFacingAway(player))
								return;
						}

						if (onlyCritSword.getValue() && mc.player.fallDistance <= 0.0F)
							return;

						if (timer.delay(currentSwordDelay)) {
							WorldUtils.hitEntity(entity, swing.getValue());

							if (clickSimulation.getValue())
								MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

							currentSwordDelay = swordDelay.getRandomValueInt();
							timer.reset();
						} else {
							if (useShield.getValue()) {
								if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
									int useFor = shieldTime.getValueInt();
									MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, useFor);
								}
							}
						}
					}
				}
			}
		} catch (Exception ignored) {}
	}

	@Override
	public void onAttack(AttackEvent event) {
		if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
			event.cancel();
	}
}
