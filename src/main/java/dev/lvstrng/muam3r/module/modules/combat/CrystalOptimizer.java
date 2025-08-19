package dev.lvstrng.muam3r.module.modules.combat;

import dev.lvstrng.muam3r.event.events.PacketSendListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public final class CrystalOptimizer extends Module implements PacketSendListener {
	public CrystalOptimizer() {
		super(EncryptedString.of("Crystal Optimizer"),
				EncryptedString.of("Makes your crystals disappear faster client-side so you can place crystals faster"),
				-1,
				Category.COMBAT);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketSendListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketSendListener.class, this);
		super.onDisable();
	}

	@Override
	public void onPacketSend(PacketSendEvent event) {
		if (event.packet instanceof PlayerInteractEntityC2SPacket interactPacket) {
			interactPacket.handle(new PlayerInteractEntityC2SPacket.Handler() {
				@Override
				public void interact(Hand hand) {

				}

				@Override
				public void interactAt(Hand hand, Vec3d pos) {

				}

				@Override
				public void attack() {

					if (mc.crosshairTarget == null)
						return;

					if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY && mc.crosshairTarget instanceof EntityHitResult hit) {
						if (hit.getEntity() instanceof EndCrystalEntity) {
							StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
							StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
							if (!(weakness == null || strength != null && strength.getAmplifier() > weakness.getAmplifier() || WorldUtils.isTool(mc.player.getMainHandStack())))
								return;

							hit.getEntity().kill();
							hit.getEntity().setRemoved(Entity.RemovalReason.KILLED);
							hit.getEntity().onRemoved();
						}
					}
				}
			});
		}
	}
}
