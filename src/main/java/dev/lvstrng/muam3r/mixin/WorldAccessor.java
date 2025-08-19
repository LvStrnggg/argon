package dev.lvstrng.muam3r.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = {World.class})
public interface WorldAccessor {
	@Accessor("blockEntityTickers")
	List<BlockEntityTickInvoker> getBlockEntityTickers();
}
