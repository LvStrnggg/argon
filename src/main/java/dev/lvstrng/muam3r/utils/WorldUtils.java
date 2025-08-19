package dev.lvstrng.muam3r.utils;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.module.modules.client.Friends;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Objects;
import java.util.stream.Stream;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class WorldUtils {
	public static boolean isDeadBodyNearby() {
		return mc.world.getPlayers().parallelStream()
				.filter(e -> e != mc.player)
				.filter(e -> e.squaredDistanceTo(mc.player) <= 36)
				.anyMatch(LivingEntity::isDead);
	}

	public static Entity findNearestEntity(PlayerEntity toPlayer, float radius, boolean seeOnly) {
		float mr = Float.MAX_VALUE;
		Entity entity = null;

		assert mc.world != null;
		for (Entity e : mc.world.getEntities()) {
			float d = e.distanceTo(toPlayer);

			if (e != toPlayer && d <= radius && mc.player.canSee(e) == seeOnly) {
				if (d < mr) {
					mr = d;
					entity = e;
				}
			}
		}
		return entity;
	}

	public static double distance(Vec3d fromVec, Vec3d toVec) {
		return Math.sqrt(Math.pow(toVec.x - fromVec.x, 2) + Math.pow(toVec.y - fromVec.y, 2) + Math.pow(toVec.z - fromVec.z, 2));
	}

	public static PlayerEntity findNearestPlayer(PlayerEntity toPlayer, float range, boolean seeOnly, boolean excludeFriends) {
		float minRange = Float.MAX_VALUE;
		PlayerEntity minPlayer = null;

		for (PlayerEntity player : mc.world.getPlayers()) {
			float distance = (float) distance(toPlayer.getPos(), player.getPos());

			if(excludeFriends && Muam3r.INSTANCE.getModuleManager().getModule(Friends.class).disableAimAssist.getValue() && Muam3r.INSTANCE.getFriendManager().isFriend(player))
				continue;

			if (player != toPlayer && distance <= range && player.canSee(toPlayer) == seeOnly) {
				if (distance < minRange) {
					minRange = distance;
					minPlayer = player;
				}
			}
		}

		return minPlayer;
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

	public static HitResult getHitResult(double radius) {
		return getHitResult(mc.player, false, mc.player.getYaw(), mc.player.getPitch(), radius);
	}

	public static HitResult getHitResult(PlayerEntity entity, boolean ignoreInvisibles, float yaw, float pitch, double distance) {
		if (entity == null || mc.world == null) return null;

		double d = distance;
		Vec3d cameraPosVec = entity.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true));
		Vec3d rotationVec = getPlayerLookVec(yaw, pitch);
		Vec3d range = cameraPosVec.add(rotationVec.x * d, rotationVec.y * d, rotationVec.z * d);

		HitResult result = mc.world.raycast(new RaycastContext(cameraPosVec, range, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));

		double e = d * d;
		d = distance;

		if (result != null) {
			e = result.getPos().squaredDistanceTo(cameraPosVec);
		}

		Vec3d vec3d3 = cameraPosVec.add(rotationVec.x * d, rotationVec.y * d, rotationVec.z * d);
		Box box = entity.getBoundingBox().stretch(rotationVec.multiply(d)).expand(1.0, 1.0, 1.0);

		EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, cameraPosVec, vec3d3, box, (entityx) ->
				!entityx.isSpectator() && entityx.canHit() && (!entityx.isInvisible() || !ignoreInvisibles), e);

		if (entityHitResult != null) {
			Vec3d vec3d4 = entityHitResult.getPos();
			double g = cameraPosVec.squaredDistanceTo(vec3d4);

			if ((distance > distance && g > Math.pow(distance, 2)) || (g < e || result == null)) {
				result = g > Math.pow(distance, 2)
						? BlockHitResult.createMissed(vec3d4, Direction.getFacing(rotationVec.x, rotationVec.y, rotationVec.z), BlockPos.ofFloored(vec3d4))
						: entityHitResult;
			}
		}

		return result;
	}


	public static void placeBlock(BlockHitResult blockHit, boolean swingHand) {
		ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
		if (result.isAccepted() && result.shouldSwingHand() && swingHand) mc.player.swingHand(Hand.MAIN_HAND);
	}

	public static Stream<WorldChunk> getLoadedChunks() {
		int radius = Math.max(2, mc.options.getClampedViewDistance()) + 3;
		int diameter = radius * 2 + 1;

		ChunkPos center = mc.player.getChunkPos();
		ChunkPos min = new ChunkPos(center.x - radius, center.z - radius);
		ChunkPos max = new ChunkPos(center.x + radius, center.z + radius);

		return Stream.iterate(min, pos -> {
					int x = pos.x;
					int z = pos.z;
					x++;
					if (x > max.x) {
						x = min.x;
						z++;
					}
					if (z > max.z)
						throw new IllegalStateException("Stream limit didn't work.");

					return new ChunkPos(x, z);

				}).limit((long) diameter * diameter)
				.filter(c -> mc.world.isChunkLoaded(c.x, c.z))
				.map(c -> mc.world.getChunk(c.x, c.z)).filter(Objects::nonNull);
	}

    /*
                NORTH

        WEST      +      EAST

                SOUTH
     */

	public static boolean isShieldFacingAway(PlayerEntity player) {
		if (mc.player != null && player != null) {
			Vec3d playerPos = mc.player.getPos();
			Vec3d targetPos = player.getPos();

			Vec3d directionToPlayer = playerPos.subtract(targetPos).normalize();

			float yaw = player.getYaw();
			float pitch = player.getPitch();
			Vec3d facingDirection = new Vec3d(
					-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
					-Math.sin(Math.toRadians(pitch)),
					Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
			).normalize();

			double dotProduct = facingDirection.dotProduct(directionToPlayer);

			return dotProduct < 0;
		}
		return false;
	}

	public static boolean isTool(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ToolItem)) {
			return false;
		}
		ToolMaterial material = ((ToolItem) itemStack.getItem()).getMaterial();
		return material == ToolMaterials.DIAMOND || material == ToolMaterials.NETHERITE;
	}

	public static boolean isCrit(PlayerEntity player, Entity target) {
		return player.getAttackCooldownProgress(0.5F) > 0.9F && player.fallDistance > 0.0F && !player.isOnGround() && !player.isClimbing() && !player.isSubmergedInWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && target instanceof LivingEntity;
	}

	public static void hitEntity(Entity entity, boolean swingHand) {
		mc.interactionManager.attackEntity(mc.player, entity);

		if (swingHand)
			mc.player.swingHand(Hand.MAIN_HAND);
	}
}
