package dev.lvstrng.muam3r.mixin;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.module.modules.render.NoBounce;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.lvstrng.muam3r.Muam3r.mc;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getBobbingAnimationTime", at = @At("HEAD"), cancellable = true)
	private void removeBounceAnimation(CallbackInfoReturnable<Integer> cir) {
		if (mc.player == null) return;

		NoBounce noBounce = Muam3r.INSTANCE.getModuleManager().getModule(NoBounce.class);
		if (Muam3r.INSTANCE != null && mc.player != null && noBounce.isEnabled()) {
			ItemStack mainHandStack = mc.player.getMainHandStack();
			if (mainHandStack.isOf(Items.END_CRYSTAL)) {
				cir.setReturnValue(0);
			}
		}
	}
}
