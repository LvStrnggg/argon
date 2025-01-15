package dev.lvstrng.argon.module.modules.combat;

import dev.lvstrng.argon.event.events.HudListener;
import dev.lvstrng.argon.event.events.PacketSendListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.module.setting.MinMaxSetting;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.EncryptedString;
import dev.lvstrng.argon.utils.TimerUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public final class AutoWTap extends Module implements PacketSendListener, HudListener {
	private final MinMaxSetting delay = new MinMaxSetting(EncryptedString.of("Delay"), 0, 1000, 1, 230, 270);
	private final BooleanSetting inAir = new BooleanSetting(EncryptedString.of("In Air"), false)
			.setDescription(EncryptedString.of("Whether it should W tap in air"));
	private final BooleanSetting targetSelection = new BooleanSetting(EncryptedString.of("Target Selection"), false)
			.setDescription(EncryptedString.of("Optimizes tapping based on target distance"));
	private final TimerUtils sprintTimer = new TimerUtils();
	private final TimerUtils tapTimer = new TimerUtils();
	private boolean holdingForward;
	private boolean sprinting;
	private int currentDelay;
	private boolean jumpedWhileHitting;
	private Entity targetEntity;
	private boolean shouldSTap;
	private boolean isSTapping;
	private boolean wasHoldingForward;
	private boolean completedSTap = false;
	private boolean wasMovingForward;
	private boolean wasInAir = false;
	private boolean wtapCancelled = false;

	public AutoWTap() {
		super(EncryptedString.of("Auto WTap"),
				EncryptedString.of("Automatically W Taps for you so the opponent takes more knockback"),
				-1,
				Category.COMBAT);
		addSettings(delay, inAir, targetSelection);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketSendListener.class, this);
		eventManager.add(HudListener.class, this);
		currentDelay = delay.getRandomValueInt();
		jumpedWhileHitting = false;
		wtapCancelled = false;
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketSendListener.class, this);
		eventManager.remove(HudListener.class, this);
		if (isSTapping) {
			mc.options.backKey.setPressed(false);
			isSTapping = false;
		}
		if (wasHoldingForward) {
			mc.options.forwardKey.setPressed(true);
			wasHoldingForward = false;
		}
		wasMovingForward = false;
		completedSTap = false;
		wasInAir = false;
		wtapCancelled = false;
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		// Handle normal W-tap if target selection is disabled
		if (!targetSelection.getValue()) {
			handleNormalWTap();
			return;
		}

		// Check if forward key is not pressed - immediately stop all movement
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) != 1) {
			resetMovementState();
			wasMovingForward = false;
			completedSTap = false;
			targetEntity = null;
			return;
		}

		// Target Selection logic
		if ((isSTapping && GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == 1) ||
				(wasInAir && mc.player.isOnGround())) {
			resetMovementState();
			completedSTap = true;
			wasInAir = false;
			return;
		}

		if (!mc.player.isOnGround()) {
			wasInAir = true;
		}

		if (jumpedWhileHitting) {
			if (!mc.player.isOnGround()) {
				resetMovementState();
				return;
			} else {
				jumpedWhileHitting = false;
			}
		}

		if (targetEntity == null || !wasMovingForward || (!inAir.getValue() && !mc.player.isOnGround())) {
			resetMovementState();
			return;
		}

		double distance = mc.player.squaredDistanceTo(targetEntity);

		if (!completedSTap && distance >= 2.25 && distance <= 7.29) {
			if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) == 1) {
				wasHoldingForward = true;
				mc.options.forwardKey.setPressed(false);
			}
			mc.options.backKey.setPressed(true);
			isSTapping = true;
		}
		else if (distance > 7.29) {
			if (isSTapping) {
				resetMovementState();
				completedSTap = true;
			}
			if (distance > 9.0) {
				targetEntity = null;
				completedSTap = false;
			}
		}
	}

	private void handleNormalWTap() {
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) != 1) {
			sprinting = false;
			holdingForward = false;
			wtapCancelled = false;
			return;
		}

		if (!inAir.getValue() && !mc.player.isOnGround()) {
			return;
		}

		// Cancel W-tap if space is pressed
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == 1) {
			if (holdingForward || sprinting) {
				mc.options.forwardKey.setPressed(true);
				holdingForward = false;
				sprinting = false;
				wtapCancelled = true;
				return;
			}
		}

		if (wtapCancelled) {
			return;
		}

		if (holdingForward && tapTimer.delay(1)) {
			mc.options.forwardKey.setPressed(false);
			sprintTimer.reset();
			sprinting = true;
			holdingForward = false;
		}

		if (sprinting && sprintTimer.delay(currentDelay)) {
			mc.options.forwardKey.setPressed(true);
			sprinting = false;
			currentDelay = delay.getRandomValueInt();
		}
	}

	private void resetMovementState() {
		if (isSTapping) {
			mc.options.backKey.setPressed(false);
			isSTapping = false;
		}
		if (wasHoldingForward) {
			mc.options.forwardKey.setPressed(true);
			wasHoldingForward = false;
		}
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (!(event.packet instanceof PlayerInteractEntityC2SPacket packet))
			return;

		packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
			@Override
			public void interact(Hand hand) {}

			@Override
			public void interactAt(Hand hand, Vec3d pos) {}

			@Override
			public void attack() {
				if (!inAir.getValue() && !mc.player.isOnGround())
					return;

				if (targetSelection.getValue()) {
					wasMovingForward = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) == 1 && mc.player.isSprinting();
					if (wasMovingForward) {
						targetEntity = mc.targetedEntity;
						completedSTap = false;
						if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_W) == 1) {
							wasHoldingForward = true;
							mc.options.forwardKey.setPressed(false);
						}
					}
				} else {
					if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == 1) {
						wtapCancelled = true;
						return;
					}

					if (mc.options.forwardKey.isPressed() && mc.player.isSprinting()) {
						sprintTimer.reset();
						holdingForward = true;
						wtapCancelled = false;
					}
				}
			}
		});
	}
}