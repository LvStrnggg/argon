package dev.lvstrng.argon.module.modules.render;

import dev.lvstrng.argon.event.events.HudListener;
import dev.lvstrng.argon.event.events.PacketSendListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public final class TargetHud extends Module implements HudListener, PacketSendListener {
	private final NumberSetting xCoord = new NumberSetting(EncryptedString.of("X"), 0, 1920, 700, 1);
	private final NumberSetting yCoord = new NumberSetting(EncryptedString.of("Y"), 0, 1080, 600, 1);
	private final BooleanSetting hudTimeout = new BooleanSetting(EncryptedString.of("Timeout"), true)
			.setDescription(EncryptedString.of("Target hud will disappear after 10 seconds"));
	private long lastAttackTime = 0;
	public static float animation;
	private static final long timeout = 10000;
	private float healthAnimation = 0f;
	private float damageAnimation = 0f;
	private long lastDamageTime = 0;
	private float opacity = 0f;
    private PlayerEntity lastTarget = null;
    private float initialAnimation = 0f;
    private boolean isFirstRender = true;
    private float scaleAnimation = 0f;

	public TargetHud() {
		super(EncryptedString.of("Target HUD"),
				EncryptedString.of("Gives you information about the enemy player"),
				-1,
				Category.RENDER);
		addSettings(xCoord, yCoord, hudTimeout);
	}

	@Override
	public void onEnable() {
		// Reset animations when enabled
        opacity = 0f;
        animation = 0f;
        healthAnimation = 0f;
        damageAnimation = 0f;
        initialAnimation = 0f;
        isFirstRender = true;
        lastTarget = null;
        scaleAnimation = 0f;
		eventManager.add(HudListener.class, this);
		eventManager.add(PacketSendListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(HudListener.class, this);
		eventManager.remove(PacketSendListener.class, this);
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		DrawContext context = event.context;
		int x = xCoord.getValueInt();
		int y = yCoord.getValueInt();

		RenderUtils.unscaledProjection();
		if ((!hudTimeout.getValue() || (System.currentTimeMillis() - lastAttackTime <= timeout)) &&
				mc.player.getAttacking() != null && mc.player.getAttacking() instanceof PlayerEntity player && player.isAlive()) {
			
			// Initialize values on first render
            if (isFirstRender) {
                healthAnimation = player.getHealth() / player.getMaxHealth();
                opacity = 0f;
                animation = 0f;
                scaleAnimation = 0f;
                isFirstRender = false;
            }

            // Single fast scale animation
            scaleAnimation = RenderUtils.fast(scaleAnimation, 1f, 40f);
            
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
            MatrixStack matrixStack = context.getMatrices();
            matrixStack.push();
            
            // Center point for scaling
            float centerX = x + 120;
            float centerY = y + 42;
            
            // Apply scale transformation from center
            matrixStack.translate(centerX, centerY, 0);
            matrixStack.scale(scaleAnimation, scaleAnimation, 1f);
            matrixStack.translate(-centerX, -centerY, 0);

			// Calculate dynamic width based on name length and ensure space for ping
			String playerName = player.getName().getString();
			String pingText = entry != null ? entry.getLatency() + "ms" : "0ms";
			int pingWidth = mc.textRenderer.getWidth(pingText) + 32; // Increased padding to 32
			int nameWidth = Math.max(240, Math.min(300, mc.textRenderer.getWidth(playerName) + 150 + pingWidth));
			
			// Colors with opacity matching scale
            Color bgColor = new Color(20, 20, 25, 230);
            Color accentColor = new Color(255, 255, 255, 25);
            Color borderColor = new Color(255, 255, 255, 30);
            Color textColor = new Color(255, 255, 255, (int)(255 * opacity * scaleAnimation));
            
            // Enhanced background blur and shadow
            RenderUtils.renderBlurredBackground(context.getMatrices(), x, y, nameWidth, 85, new Color(20, 20, 25, 100));
            RenderUtils.renderDropShadow(context.getMatrices(), x, y, nameWidth, 85, 6f, new Color(0, 0, 0, 160));
            
            // Background with dynamic width
            RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(20, 20, 25, 200), x, y, x + nameWidth, y + 85, 8, 8, 8, 8, 15);
            RenderUtils.renderRoundedQuad(context.getMatrices(), accentColor, x, y, x + nameWidth, y + 42, 8, 8, 0, 0, 15);

			// Player head with border
			if (entry != null) {
				RenderUtils.renderRoundedQuad(context.getMatrices(), borderColor, x + 8, y + 8, x + 34, y + 34, 5, 5, 5, 5, 10);
				PlayerSkinDrawer.draw(context, entry.getSkinTextures().texture(), x + 9, y + 9, 24);
			}

			// Player name with shadow
            TextRenderer.drawString(playerName, context, x + 42, y + 12, textColor.getRGB());

			// Ping indicator (moved further left)
			int ping = entry != null ? entry.getLatency() : 0;
			Color pingColor = getPingColor(ping);
			TextRenderer.drawString(pingText, context, x + nameWidth - mc.textRenderer.getWidth(pingText) - 32, y + 12, pingColor.getRGB());

			// Health bar container with dynamic width
			RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(0, 0, 0, 100), x + 42, y + 28, x + nameWidth - 8, y + 36, 3, 3, 3, 3, 10);
			
			// Animated health bar
			float targetHealth = (player.getHealth() + player.getAbsorptionAmount()) / player.getMaxHealth();
			healthAnimation = RenderUtils.fast(healthAnimation, targetHealth, 10f);
			int healthWidth = (int)((nameWidth - 50) * healthAnimation);
			
			Color healthColor = getHealthColor(healthAnimation);
			RenderUtils.renderRoundedQuad(context.getMatrices(), healthColor, x + 42, y + 28, x + 42 + healthWidth, y + 36, 3, 3, 3, 3, 10);

			// Health and distance info (adjusted Y position)
			String healthText = String.format("%.1f❤", player.getHealth() + player.getAbsorptionAmount());
			TextRenderer.drawString(healthText, context, x + 42, y + 50, healthColor.getRGB());
			String distanceText = String.format("%.1fm", player.distanceTo(mc.player));
			TextRenderer.drawString(distanceText, context, x + 100, y + 50, Color.WHITE.getRGB());

			// Damage indicator effect with dynamic width
			if (player.hurtTime > 0) {
				damageAnimation = 1f;
				lastDamageTime = System.currentTimeMillis();
			} else if (System.currentTimeMillis() - lastDamageTime > 500) {
				damageAnimation = RenderUtils.fast(damageAnimation, 0f, 5f);
			}
			
			if (damageAnimation > 0) {
				RenderUtils.renderRoundedQuad(context.getMatrices(), 
					new Color(255, 0, 0, (int)(60 * damageAnimation)), 
					x, y, x + nameWidth, y + 85, 8, 8, 8, 8, 15);
			}

			matrixStack.pop();
		} else {
			scaleAnimation = RenderUtils.fast(scaleAnimation, 0f, 40f);
            if (scaleAnimation <= 0.01f) {
                lastTarget = null;
                isFirstRender = true;
            }
		}
		RenderUtils.scaledProjection();
	}

	private Color getHealthColor(float health) {
        return new Color(
            (int)(255 * (1 - health)),
            (int)(255 * (0.8f + health * 0.2f)),
            (int)(255 * (health * 0.3f)),
            255
        );
    }

    private Color getPingColor(int ping) {
        if (ping < 50) return new Color(0, 255, 0);
        if (ping < 100) return new Color(255, 255, 0);
        if (ping < 150) return new Color(255, 150, 0);
        return new Color(255, 0, 0);
    }

	@Override
	public void onPacketSend(PacketSendListener.PacketSendEvent event) {
		if (event.packet instanceof PlayerInteractEntityC2SPacket packet) {
			packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
				@Override
				public void interact(Hand hand) {

				}

				@Override
				public void interactAt(Hand hand, Vec3d pos) {

				}

				@Override
				public void attack() {
					if (mc.targetedEntity instanceof PlayerEntity) {
						lastAttackTime = System.currentTimeMillis();
					}
				}
			});
		}
	}
}