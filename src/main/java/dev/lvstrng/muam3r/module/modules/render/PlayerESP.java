package dev.lvstrng.muam3r.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lvstrng.muam3r.event.events.GameRenderListener;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.modules.client.ClickGUI;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.ModeSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.ColorUtils;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.RenderUtils;
import dev.lvstrng.muam3r.utils.Utils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;

public final class PlayerESP extends Module implements GameRenderListener {
	public enum Mode {
		TwoD, ThreeD
	}

	public final ModeSetting<Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), Mode.ThreeD, Mode.class);
	private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 0, 255, 100, 1);
	private final NumberSetting width = new NumberSetting(EncryptedString.of("Line width"), 1, 10, 1, 1);
	private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false)
			.setDescription(EncryptedString.of("Draws a line from your player to the other"));

	public PlayerESP() {
		super(EncryptedString.of("Player ESP"),
				EncryptedString.of("Renders players through walls"),
				-1,
				Category.RENDER);
		addSettings(alpha, mode, width, tracers);
	}

	@Override
	public void onEnable() {
		eventManager.add(GameRenderListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(GameRenderListener.class, this);
		super.onDisable();
	}

	@Override
	public void onGameRender(GameRenderEvent event) {
		for (PlayerEntity player : mc.world.getPlayers()) {
			if (mode.isMode(Mode.ThreeD)) {
				if (player != mc.player) {
					Camera cam = mc.getBlockEntityRenderDispatcher().camera;
					if (cam != null) {
						MatrixStack matrices = event.matrices;
						matrices.push();
						Vec3d vec = cam.getPos();
						matrices.translate(-vec.x, -vec.y, -vec.z);
					}

					double xPos = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevX, player.getX());
					double yPos = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevY, player.getY());
					double zPos = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevZ, player.getZ());

					RenderUtils.renderFilledBox(
							event.matrices,
							(float) xPos - player.getWidth() / 2,
							(float) yPos,
							(float) zPos - player.getWidth() / 2,
							(float) xPos + player.getWidth() / 2,
							(float) yPos + player.getHeight(),
							(float) zPos + player.getWidth() / 2,
							Utils.getMainColor(alpha.getValueInt(), 1).brighter());

					if (tracers.getValue())
						RenderUtils.renderLine(event.matrices, Utils.getMainColor(255, 1), mc.crosshairTarget.getPos(), player.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true)));

					event.matrices.pop();
				}
			} else if (mode.isMode(Mode.TwoD)) {
				if (player != mc.player) {
					renderOutline(player, getColor(alpha.getValueInt()), event.matrices);

					Camera cam = mc.getBlockEntityRenderDispatcher().camera;
					if (cam != null) {
						MatrixStack matrices = event.matrices;
						matrices.push();
						Vec3d vec = cam.getPos();
						matrices.translate(-vec.x, -vec.y, -vec.z);
					}

					if (tracers.getValue())
						RenderUtils.renderLine(event.matrices, Utils.getMainColor(255, 1), mc.crosshairTarget.getPos(), player.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true)));

					event.matrices.pop();
				}
			}
		}
	}

	private void renderOutline(PlayerEntity e, Color color, MatrixStack stack) {
		float red = color.brighter().getRed() / 255f;
		float green = color.brighter().getGreen() / 255f;
		float blue = color.brighter().getBlue() / 255f;
		float alpha = color.brighter().getAlpha() / 255f;

		Camera c = mc.gameRenderer.getCamera();
		Vec3d camPos = c.getPos();
		Vec3d start = e.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true)).subtract(camPos);
		float x = (float) start.x;
		float y = (float) start.y;
		float z = (float) start.z;

		double r = Math.toRadians(-c.getYaw() + 90);
		float sin = (float) (Math.sin(r) * (e.getWidth() / 1.7));
		float cos = (float) (Math.cos(r) * (e.getWidth() / 1.7));
		stack.push();

		Matrix4f matrix = stack.peek().getPositionMatrix();

		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		if (ClickGUI.antiAliasing.getValue()) {
			GL11.glEnable(GL13.GL_MULTISAMPLE);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		}
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableBlend();

		GL11.glLineWidth(width.getValueInt());
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES,
				VertexFormats.POSITION_COLOR);

		buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha);
		buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha);

		BufferRenderer.drawWithGlobalProgram(buffer.end());
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glLineWidth(1f);
		RenderSystem.disableBlend();
		if (ClickGUI.antiAliasing.getValue()) {
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL13.GL_MULTISAMPLE);
		}
		stack.pop();
	}

	private Color getColor(int alpha) {
		int red = ClickGUI.red.getValueInt();
		int green = ClickGUI.green.getValueInt();
		int blue = ClickGUI.blue.getValueInt();

		if (ClickGUI.rainbow.getValue())
			return ColorUtils.getBreathingRGBColor(1, alpha);
		else
			return new Color(red, green, blue, alpha);
	}
}