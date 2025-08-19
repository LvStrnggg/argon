package dev.lvstrng.muam3r.module.modules.render;

import dev.lvstrng.muam3r.event.events.HudListener;
import dev.lvstrng.muam3r.event.events.PacketSendListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.*;
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
	private final NumberSetting xCoord = new NumberSetting(EncryptedString.of("X"), 0, 1920, 500, 1);
	private final NumberSetting yCoord = new NumberSetting(EncryptedString.of("Y"), 0, 1080, 500, 1);
	private final BooleanSetting hudTimeout = new BooleanSetting(EncryptedString.of("Timeout"), true)
			.setDescription(EncryptedString.of("Target hud will disappear after 10 seconds"));
	private long lastAttackTime = 0;
	public static float animation;
	private static final long timeout = 10000;

	public TargetHud() {
		super(EncryptedString.of("Target HUD"),
				EncryptedString.of("Gives you information about the enemy player"),
				-1,
				Category.RENDER);
		addSettings(xCoord, yCoord, hudTimeout);
	}

	@Override
	public void onEnable() {
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
			animation = RenderUtils.fast(animation, mc.player.getAttacking() instanceof PlayerEntity player1 && player1.isAlive() ? 0 : 1, 15f);

			PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
			float tx = (float) x;
			float ty = (float) y;
			MatrixStack matrixStack = context.getMatrices();
			float thetaRotation = 90 * animation;
			matrixStack.push();
			matrixStack.translate(tx, ty, 0);

			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(thetaRotation));
			matrixStack.translate(-tx, -ty, 0);

			RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(0, 0, 0, 175), x, y, x + 340, y + 200, 5, 5, 5, 5, 10);
			RenderUtils.renderRoundedQuad(context.getMatrices(), Utils.getMainColor(255, 1), x, y + 27, x + 340, y + 29, 0, 0, 0, 0, 10);

			TextRenderer.drawString(player.getName().getString() + " - " + MathUtils.roundToDecimal(player.distanceTo(mc.player), 0.5) + " blocks", context, x + 23, y + 5, Color.WHITE.getRGB());

			if (entry == null) {
				int charOff1 = x + 5;
				CharSequence chars = "Type: Bot";

				TextRenderer.drawString(chars, context, charOff1, y + 35, new Color(255, 80, 80, 255).getRGB());

				matrixStack.pop();
				RenderUtils.scaledProjection();
				return;
			} else {
				int charOff1 = x + 5;
				CharSequence chars = "Type: Player";

				TextRenderer.drawString(chars, context, charOff1, y + 35, Color.white.getRGB());
			}

			TextRenderer.drawString("Health: " + Math.round((player.getHealth() + player.getAbsorptionAmount())), context, x + 5, y + 65, Color.GREEN.getRGB());
			context.fill(x, y + 200, x + 4, (y + 200) - Math.min(Math.round((player.getHealth() + player.getAbsorptionAmount()) * 5), 171), Color.GREEN.darker().getRGB());
			//RenderUtils.renderRoundedOutline(context, Color.green, x, y + 200, x, (y + 200) - Math.min(Math.round((player.getHealth() + player.getAbsorptionAmount()) * 5), 174), 0, 0, 0, 0, 3, 30);

			TextRenderer.drawString("Invisible: " + (player.isInvisible() ? "Yes" : "No"), context, x + 5, y + 95, Color.WHITE.getRGB());

			TextRenderer.drawString("Ping: " + entry.getLatency(), context, x + 5, y + 125, Color.WHITE.getRGB());

			PlayerSkinDrawer.draw(context, entry.getSkinTextures().texture(), x + 3, y + 3, 20);

			if (player.hurtTime != 0) {
				int charOff1 = x + 125;
				CharSequence chars = ("Damage Tick: " + player.hurtTime);

				TextRenderer.drawString(chars, context, charOff1, y + 65, Color.WHITE.getRGB());
				//TextRenderer.drawString("Damage Tick: " + player.hurtTime, context, x + 125, y + 65, Color.WHITE.getRGB());
				context.fill(x + 125, y + 80, (x + 125) + (player.hurtTime * 15), y + 83, getDamageTickColor(player.hurtTime).getRGB());
			}
			matrixStack.pop();
		} else {
			animation = RenderUtils.fast(animation, 1, 15f);
		}
		RenderUtils.scaledProjection();
	}

	private Color getDamageTickColor(int hurtTime) {
		return switch (hurtTime) {
			case 0 -> null;
			case 10 -> new Color(255, 0, 0, 255);
			case 9 -> new Color(255, 50, 0, 255);
			case 8 -> new Color(255, 100, 0, 255);
			case 7 -> new Color(255, 150, 0, 255);
			case 6 -> new Color(255, 255, 0, 255);
			case 5 -> new Color(200, 255, 0, 255);
			case 4 -> new Color(175, 255, 0, 255);
			case 3 -> new Color(100, 255, 0, 255);
			case 2 -> new Color(50, 255, 0, 255);
			case 1 -> new Color(0, 255, 0, 255);
			default -> throw new IllegalStateException("uv" + hurtTime);
		};
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