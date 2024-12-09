package dev.lvstrng.argon.module.modules.combat;

import dev.lvstrng.argon.event.events.HudListener;
import dev.lvstrng.argon.event.events.MouseMoveListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.module.setting.MinMaxSetting;
import dev.lvstrng.argon.module.setting.ModeSetting;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.*;
import dev.lvstrng.argon.utils.rotation.Rotation;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class AimAssist extends Module implements HudListener, MouseMoveListener {
	private final BooleanSetting stickyAim = new BooleanSetting(EncryptedString.of("Sticky Aim"), false)
			.setDescription(EncryptedString.of("Aims at the last attacked player"));

	private final BooleanSetting onlyWeapon = new BooleanSetting(EncryptedString.of("Only Weapon"), true);

	private final BooleanSetting onLeftClick = new BooleanSetting(EncryptedString.of("On Left Click"), false)
			.setDescription(EncryptedString.of("Only gets triggered if holding down left click"));
	private final ModeSetting<AimMode> aimAt = new ModeSetting<>(EncryptedString.of("Aim At"), AimMode.Head, AimMode.class);

	private final BooleanSetting stopAtTargetVertical = new BooleanSetting(EncryptedString.of("Stop at Target Vert"), true)
			.setDescription(EncryptedString.of("Stops vertically assisting if already aiming at the entity, helps bypass anti-cheat"));

	private final BooleanSetting stopAtTargetHorizontal = new BooleanSetting(EncryptedString.of("Stop at Target Horiz"), false)
			.setDescription(EncryptedString.of("Stops horizontally assisting if already aiming at the entity, helps bypass anti-cheat"));

	private final NumberSetting radius = new NumberSetting(EncryptedString.of("Radius"), 0.1, 6, 5, 0.1);

	private final BooleanSetting seeOnly = new BooleanSetting(EncryptedString.of("See Only"), true);
	private final BooleanSetting lookAtNearest = new BooleanSetting(EncryptedString.of("Look at Nearest"), false);

	private final NumberSetting fov = new NumberSetting(EncryptedString.of("FOV"), 5, 360, 180, 1);

	private final MinMaxSetting pitchSpeed = new MinMaxSetting(EncryptedString.of("Vertical Speed"), 0, 10, 0.1, 2, 4);
	private final MinMaxSetting yawSpeed = new MinMaxSetting(EncryptedString.of("Horizontal Speed"), 0, 10, 0.1, 2, 4);

	private final NumberSetting speedChange = new NumberSetting(EncryptedString.of("Speed Delay"), 0, 1000, 250, 1)
			.setDescription(EncryptedString.of("Time in milliseconds to wait after resetting random speed"));

	private final NumberSetting randomization = new NumberSetting(EncryptedString.of("Chance"), 0, 100, 50, 1);

	private final BooleanSetting yawAssist = new BooleanSetting(EncryptedString.of("Horizontal"), true);
	private final BooleanSetting pitchAssist = new BooleanSetting(EncryptedString.of("Vertical"), true);

	private final NumberSetting waitFor = new NumberSetting(EncryptedString.of("Wait on Move"), 0, 1000, 0, 1)
			.setDescription(EncryptedString.of("After you move your mouse aim assist will stop working for the selected amount of time"));

	private final ModeSetting<LerpMode> lerp = new ModeSetting<>(EncryptedString.of("Lerp"), LerpMode.Normal, LerpMode.class)
			.setDescription(EncryptedString.of("Linear interpolation to use to rotate"));

	private final ModeSetting<PosMode> posMode = new ModeSetting<>(EncryptedString.of("Pos mode"), PosMode.Normal, PosMode.class)
			.setDescription(EncryptedString.of("Precision of the target position"));

	private final TimerUtils timer = new TimerUtils();
	private final TimerUtils resetSpeed = new TimerUtils();
	private boolean move;
	private float pitch, yaw;

	@SuppressWarnings("unused")
	public enum PosMode {
		Normal, Lerped
	}

	public enum AimMode {
		Head, Chest, Legs
	}

	public enum LerpMode {
		Normal, Smoothstep, EaseOut
	}

	public AimAssist() {
		super(EncryptedString.of("Aim Assist"),
				EncryptedString.of("Automatically aims at players for you"),
				-1,
				Category.COMBAT);

		addSettings(stickyAim, onlyWeapon, onLeftClick, aimAt, stopAtTargetVertical, stopAtTargetHorizontal, radius, seeOnly, lookAtNearest, fov, pitchSpeed, yawSpeed, speedChange, randomization, yawAssist, pitchAssist, waitFor, lerp, posMode);
	}

	@Override
	public void onEnable() {
		move = true;
		pitch = pitchSpeed.getRandomValueFloat();
		yaw = yawSpeed.getRandomValueFloat();

		eventManager.add(HudListener.class, this);
		eventManager.add(MouseMoveListener.class, this);

		timer.reset();
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(HudListener.class, this);
		eventManager.remove(MouseMoveListener.class, this);
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (timer.delay(waitFor.getValueFloat()) && !move) {
			move = true;
			timer.reset();
		}

		if (mc.player == null || mc.currentScreen != null)
			return;

		if (onlyWeapon.getValue() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem))
			return;

		if (onLeftClick.getValue() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
			return;

		PlayerEntity target = WorldUtils.findNearestPlayer(mc.player, radius.getValueFloat(), seeOnly.getValue(), true);
		if (stickyAim.getValue() && mc.player.getAttacking() instanceof PlayerEntity player && player.distanceTo(mc.player) < radius.getValue())
			target = player;

		if (target == null)
			return;

		if(resetSpeed.delay(speedChange.getValueFloat())) {
			pitch = pitchSpeed.getRandomValueFloat();
			yaw = yawSpeed.getRandomValueFloat();
			resetSpeed.reset();
		}

		Vec3d targetPos = posMode.isMode(PosMode.Normal) ? target.getPos() : target.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true));

		if (aimAt.isMode(AimMode.Chest))
			targetPos = targetPos.add(0, -0.5, 0);
		else if (aimAt.isMode(AimMode.Legs))
			targetPos = targetPos.add(0, -1.2, 0);

		if (lookAtNearest.getValue()) {
			double offsetX = mc.player.getX() - target.getX() > 0 ? 0.29 : -0.29;
			double offsetZ = mc.player.getZ() - target.getZ() > 0 ? 0.29 : -0.29;
			targetPos = targetPos.add(offsetX, 0, offsetZ);
		}

		Rotation rotation = RotationUtils.getDirection(mc.player, targetPos);

		double angleToRotation = RotationUtils.getAngleToRotation(rotation);
		if (angleToRotation > (double) fov.getValueInt() / 2)
			return;

		float yawStrength = yaw / 50;
		float pitchStrength = pitch / 50;

		float yaw = mc.player.getYaw();
		float pitch = mc.player.getPitch();

		if (lerp.isMode(LerpMode.Smoothstep)) {
			yaw = (float) smoothStepLerp(yawStrength, mc.player.getYaw(), (float) rotation.yaw());
			pitch = (float) smoothStepLerp(pitchStrength, mc.player.getPitch(), (float) rotation.pitch());
		}

		if (lerp.isMode(LerpMode.Normal)) {
			yaw = lerp(yawStrength, mc.player.getYaw(), (float) (rotation.yaw()));
			pitch = lerp(pitchStrength, mc.player.getPitch(), (float) (rotation.pitch()));
		}

		if (lerp.isMode(LerpMode.EaseOut)) {
			yaw = (float) easeOutBackDegrees(mc.player.getYaw(), rotation.yaw(), yawStrength * RenderTickCounter.ONE.getLastFrameDuration());
			pitch = (float) easeOutBackDegrees(mc.player.getPitch(), rotation.pitch(), pitchStrength * RenderTickCounter.ONE.getLastFrameDuration());
		}

		if (MathUtils.randomInt(1, 100) <= randomization.getValueInt()) {
			if (move) {
				if (yawAssist.getValue()) {
					if(stopAtTargetHorizontal.getValue() && WorldUtils.getHitResult(radius.getValue()) instanceof EntityHitResult hitResult && hitResult.getEntity() == target)
						return;

					mc.player.setYaw(yaw);
				}

				if (pitchAssist.getValue()) {
					if(stopAtTargetVertical.getValue() && WorldUtils.getHitResult(radius.getValue()) instanceof EntityHitResult hitResult && hitResult.getEntity() == target)
						return;

					mc.player.setPitch(pitch);
				}
			}
		}
	}

	public float lerp(float delta, float start, float end) {
		return start + (MathHelper.wrapDegrees(end - start) * delta);
	}

	public static double easeOutBackDegrees(double start, double end, float speed) {
		double c1 = 1.70158;
		double c3 = 2.70158;
		double x = 1 - Math.pow(1 - (double) speed, 3);

		return start + MathHelper.wrapDegrees(end - start) * (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
	}

	public double smoothStepLerp(double delta, double start, double end) {
		double value;
		delta = Math.max(0, Math.min(1, delta));

		double t = delta * delta * (3 - 2 * delta);

		value = start + MathHelper.wrapDegrees(end - start) * t;
		return value;
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		move = false;
		timer.reset();
	}
}
