package dev.lvstrng.muam3r.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor
	boolean getJumping();

	@Accessor("lastDamageSource")
	DamageSource getLastDamageSource();

	@Accessor("lastDamageSource")
	void setLastDamageSource(DamageSource source);

	@Accessor("lastDamageTime")
	long getLastDamageTime();

	@Accessor("lastDamageTime")
	void setLastDamageTime(long time);

	@Accessor("lastBlockPos")
	BlockPos getLastBlockPos();

	@Accessor("lastBlockPos")
	void setLastBlockPos(BlockPos pos);

	@Accessor("attacking")
	void setAttacking(LivingEntity attacking);
}
