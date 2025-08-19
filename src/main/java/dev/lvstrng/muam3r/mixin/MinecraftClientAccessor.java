package dev.lvstrng.muam3r.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
	@Accessor
	Mouse getMouse();

	@Invoker
	void invokeDoItemUse();

	@Invoker
	boolean invokeDoAttack();

	@Accessor("itemUseCooldown")
	void setItemUseCooldown(int cooldown);
}
