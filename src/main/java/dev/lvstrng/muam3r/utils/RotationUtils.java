package dev.lvstrng.muam3r.utils;

import dev.lvstrng.muam3r.utils.rotation.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class RotationUtils {

	public static Vec3d getEyesPos(PlayerEntity player) {
		return RenderUtils.getCameraPos();
	}

	public static BlockPos getCameraBlockPos() {
		return mc.getBlockEntityRenderDispatcher().camera.getBlockPos();
	}

	public static BlockPos getEyesBlockPos() {
		return new BlockPos((int) getEyesPos(mc.player).x, (int) getEyesPos(mc.player).y, (int) getEyesPos(mc.player).z);
	}

	public static Vec3d getPlayerLookVec(float yaw, float pitch) {
		float f = pitch * 0.017453292F;
		float g = -yaw * 0.017453292F;

		float h = MathHelper.cos(g);
		float i = MathHelper.sin(g);
		float j = MathHelper.cos(f);
		float k = MathHelper.sin(f);

		return new Vec3d((i * j), (-k), (h * j));
	}

	public static Vec3d getPlayerLookVec(PlayerEntity player) {
		return getPlayerLookVec(player.getYaw(), player.getPitch());
	}

	public static Rotation getDiff(Rotation rotation1, Rotation rotation2) {
		double yaw = Math.abs(Math.max(rotation1.yaw(), rotation2.yaw()) - Math.min(rotation1.yaw(), rotation2.yaw()));
		double pitch = Math.abs(Math.max(rotation1.pitch(), rotation2.pitch()) - Math.min(rotation1.pitch(), rotation2.pitch()));

		return new Rotation(yaw, pitch);
	}

	public static Rotation getSmoothRotation(Rotation from, Rotation to, double speed) {
		return new Rotation(
				MathHelper.lerpAngleDegrees((float) speed, (float) from.yaw(), (float) to.yaw()),
				MathHelper.lerpAngleDegrees((float) speed, (float) from.pitch(), (float) to.pitch())
		);
	}

	public static double getTotalDiff(Rotation rotation1, Rotation rotation2) {
		Rotation diff = getDiff(rotation1, rotation2);

		return diff.yaw() + diff.pitch();
	}

	public static Vec3d getClientLookVec() {
		return getPlayerLookVec(mc.player);
	}

	public static Rotation getDirection(Entity entity, Vec3d vec) {
		double dx = vec.x - entity.getX(),
				dy = vec.y - entity.getY(),
				dz = vec.z - entity.getZ(),
				dist = MathHelper.sqrt((float) (dx * dx + dz * dz));

		return new Rotation(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0), -MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dy, dist))));
	}

	public static double getAngleToRotation(Rotation rotation) {
		double currentYaw = MathHelper.wrapDegrees(mc.player.getYaw());
		double currentPitch = MathHelper.wrapDegrees(mc.player.getPitch());

		double diffYaw = MathHelper.wrapDegrees(currentYaw - rotation.yaw());
		double diffPitch = MathHelper.wrapDegrees(currentPitch - rotation.pitch());

		return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
	}
}