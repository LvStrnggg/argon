package dev.lvstrng.muam3r.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface MouseHandlerAccessor {
	@Invoker("onMouseButton")
	void press(long window, int button, int action, int mods);
}
