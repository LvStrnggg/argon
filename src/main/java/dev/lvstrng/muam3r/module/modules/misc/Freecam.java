package dev.lvstrng.muam3r.module.modules.misc;

import dev.lvstrng.muam3r.event.events.CameraUpdateListener;
import dev.lvstrng.muam3r.event.events.TickListener;
import dev.lvstrng.muam3r.mixin.KeyBindingAccessor;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;


public final class Freecam extends Module implements TickListener, CameraUpdateListener {
	private final NumberSetting speed = new NumberSetting(EncryptedString.of("Speed"), 1, 10, 1, 1);
	public Vec3d oldPos;
	public Vec3d pos;

	public Freecam() {
		super(EncryptedString.of("Freecam"),
				EncryptedString.of("Lets you move freely around the world without actually moving"),
				-1,
				Category.MISC);
		addSettings(speed);

		oldPos = Vec3d.ZERO;
		pos = Vec3d.ZERO;
	}

	@Override
	public void onEnable() {
		eventManager.add(TickListener.class, this);
		eventManager.add(CameraUpdateListener.class, this);
		if (mc.world != null) {
			this.oldPos = this.pos = mc.player.getEyePos();
		}

		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(TickListener.class, this);
		eventManager.remove(CameraUpdateListener.class, this);

		if (mc.world != null) {
			mc.player.setVelocity(Vec3d.ZERO);
			mc.worldRenderer.reload();
		}
		super.onDisable();
	}

	@Override
	public void onTick() {
		if (mc.currentScreen != null)
			return;

		mc.options.useKey.setPressed(false);
		mc.options.attackKey.setPressed(false);
		mc.options.forwardKey.setPressed(false);
		mc.options.backKey.setPressed(false);
		mc.options.leftKey.setPressed(false);
		mc.options.rightKey.setPressed(false);
		mc.options.jumpKey.setPressed(false);
		mc.options.sneakKey.setPressed(false);

		float f = (float) Math.PI / 180;
		float f2 = (float) Math.PI;
		ClientPlayerEntity clientPlayerEntity = mc.player;
		Vec3d vec3d = new Vec3d(-MathHelper.sin(-mc.player.getYaw() * f - f2), 0.0, -MathHelper.cos(-clientPlayerEntity.getYaw() * f - f2));
		Vec3d vec3d2 = new Vec3d(0.0, 1.0, 0.0);
		Vec3d vec3d3 = vec3d2.crossProduct(vec3d);
		Vec3d vec3d4 = vec3d.crossProduct(vec3d2);
		Vec3d vec3d5 = Vec3d.ZERO;
		KeyBinding keyBinding = mc.options.forwardKey;

		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.add(vec3d);
		}

		KeyBinding keyBinding2 = mc.options.backKey;
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding2).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.subtract(vec3d);
		}

		KeyBinding keyBinding3 = mc.options.leftKey;
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding3).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.add(vec3d3);
		}

		KeyBinding keyBinding4 = mc.options.rightKey;
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding4).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.add(vec3d4);
		}

		KeyBinding keyBinding5 = mc.options.jumpKey;
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding5).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.add(0.0, speed.getValue(), 0.0);
		}

		KeyBinding keyBinding6 = mc.options.sneakKey;
		if (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding6).getBoundKey().getCode()) == GLFW.GLFW_PRESS) {
			vec3d5 = vec3d5.add(0.0, -speed.getValue(), 0.0);
		}

		KeyBinding keyBinding7 = mc.options.sprintKey;
		vec3d5 = vec3d5.normalize().multiply(speed.getValue() * (GLFW.glfwGetKey(mc.getWindow().getHandle(), ((KeyBindingAccessor) keyBinding7).getBoundKey().getCode()) == GLFW.GLFW_PRESS ? 2 : 1));

		oldPos = pos;
		pos = pos.add(vec3d5);
	}

	@Override
	public void onCameraUpdate(CameraUpdateEvent event) {
		float tickDelta = RenderTickCounter.ONE.getTickDelta(true);

		if (mc.currentScreen != null)
			return;

		event.setX(MathHelper.lerp(tickDelta, oldPos.x, pos.x));
		event.setY(MathHelper.lerp(tickDelta, oldPos.y, pos.y));
		event.setZ(MathHelper.lerp(tickDelta, oldPos.z, pos.z));
	}
}
