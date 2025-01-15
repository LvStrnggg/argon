package dev.lvstrng.argon.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import dev.lvstrng.argon.module.modules.client.ClickGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.lvstrng.argon.Argon.mc;


public final class RenderUtils {
	public static VertexSorter vertexSorter;
	public static boolean rendering3D = true;

	public static Vec3d getCameraPos() {
		return mc.getBlockEntityRenderDispatcher().camera.getPos();
	}

	public static double deltaTime() {
		return mc.getCurrentFps() > 0 ? (1.0000 / mc.getCurrentFps()) : 1;
	}

	public static float fast(float end, float start, float multiple) {
		return (1 - MathHelper.clamp((float) (deltaTime() * multiple), 0, 1)) * end + MathHelper.clamp((float) (deltaTime() * multiple), 0, 1) * start;
	}

	public static Vec3d getPlayerLookVec(PlayerEntity player) {
		float f = 0.017453292F;
		float pi = 3.1415927F;
		float f1 = MathHelper.cos(-player.getYaw() * f - pi);
		float f2 = MathHelper.sin(-player.getYaw() * f - pi);
		float f3 = -MathHelper.cos(-player.getPitch() * f);
		float f4 = MathHelper.sin(-player.getPitch() * f);
		return (new Vec3d((f2 * f3), f4, (f1 * f3))).normalize();
	}

	public static void unscaledProjection() {
		vertexSorter = RenderSystem.getVertexSorting();
		RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), 0, 1000, 21000), VertexSorter.BY_Z);
		rendering3D = false;
	}

	public static void scaledProjection() {
		RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (mc.getWindow().getFramebufferWidth() / mc.getWindow().getScaleFactor()), (float) (mc.getWindow().getFramebufferHeight() / mc.getWindow().getScaleFactor()), 0, 1000, 21000), vertexSorter);
		rendering3D = true;
	}

	public static void renderRoundedQuad(MatrixStack matrices, Color c, double x, double y, double x2, double y2, double corner1, double corner2, double corner3, double corner4, double samples) {
		int color = c.getRGB();
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		float f = (float) (color >> 24 & 255) / 255.0F;
		float g = (float) (color >> 16 & 255) / 255.0F;
		float h = (float) (color >> 8 & 255) / 255.0F;
		float k = (float) (color & 255) / 255.0F;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		renderRoundedQuadInternal(matrix, g, h, k, f, x, y, x2, y2, corner1, corner2, corner3, corner4, samples);
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	private static void setup() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}

	private static void cleanup() {
		RenderSystem.enableCull();
		RenderSystem.disableBlend();
	}

	public static void renderRoundedQuad(MatrixStack matrices, Color c, double x, double y, double x1, double y1, double rad, double samples) {
		renderRoundedQuad(matrices, c, x, y, x1, y1, rad, rad, rad, rad, samples);
	}

	public static void renderRoundedOutlineInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double width, double samples) {
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

		double[][] map = new double[][]{new double[]{toX - radC4, toY - radC4, radC4}, new double[]{toX - radC2, fromY + radC2, radC2},
				new double[]{fromX + radC1, fromY + radC1, radC1}, new double[]{fromX + radC3, toY - radC3, radC3}};
		for (int i = 0; i < 4; i++) {
			double[] current = map[i];
			double rad = current[2];
			for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
				float rad1 = (float) Math.toRadians(r);
				double sin1 = Math.sin(rad1);
				float sin = (float) (sin1 * rad);
				double cos1 = Math.cos(rad1);
				float cos = (float) (cos1 * rad);
				bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
				bufferBuilder.vertex(matrix, (float) (current[0] + sin + sin1 * width), (float) (current[1] + cos + cos1 * width), 0.0F).color(cr, cg, cb, ca);
			}
			float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
			double sin1 = Math.sin(rad1);
			float sin = (float) (sin1 * rad);
			double cos1 = Math.cos(rad1);
			float cos = (float) (cos1 * rad);
			bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
			bufferBuilder.vertex(matrix, (float) (current[0] + sin + sin1 * width), (float) (current[1] + cos + cos1 * width), 0.0F).color(cr, cg, cb, ca);
		}
		int i = 0;
		double[] current = map[i];
		double rad = current[2];
		float cos = (float) (rad);
		bufferBuilder.vertex(matrix, (float) current[0], (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
		bufferBuilder.vertex(matrix, (float) (current[0] + cos), (float) (current[1] + cos + width), 0.0F).color(cr, cg, cb, ca);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	public static void setScissorRegion(int x, int y, int width, int height) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		int screenHeight;
		if (currentScreen == null)
			screenHeight = 0;
		else
			screenHeight = currentScreen.height - height;

		double scaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		GL11.glScissor((int) (x * scaleFactor), (int) (screenHeight * scaleFactor), (int) ((width - x) * scaleFactor), (int) ((height - y) * scaleFactor));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
	}

	public static void renderCircle(MatrixStack matrices, Color c, double originX, double originY, double rad, int segments) {
		int segments1 = MathHelper.clamp(segments, 4, 360);
		int color = c.getRGB();

		Matrix4f matrix = matrices.peek().getPositionMatrix();
		float f = (float) (color >> 24 & 255) / 255.0F;
		float g = (float) (color >> 16 & 255) / 255.0F;
		float h = (float) (color >> 8 & 255) / 255.0F;
		float k = (float) (color & 255) / 255.0F;
		setup();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		for (int i = 0; i < 360; i += Math.min(360 / segments1, 360 - i)) {
			double radians = Math.toRadians(i);
			double sin = Math.sin(radians) * rad;
			double cos = Math.cos(radians) * rad;
			bufferBuilder.vertex(matrix, (float) (originX + sin), (float) (originY + cos), 0).color(g, h, k, f);
		}
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		cleanup();
	}

	public static void renderShaderRect(MatrixStack matrixStack, Color color, Color color2, Color color3, Color color4, float f, float f2, float f3, float f4, float f5, float f6) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), f - 10, f2 - 10, 0);
		bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), f - 10, f2 + f4 + 20, 0);
		bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), f + f3 + 20, f2 + f4 + 20, 0);
		bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), f + f3 + 20, f2 - 10, 0);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}

	public static void renderRoundedOutline(DrawContext poses, Color c, double fromX, double fromY, double toX, double toY, double rad1, double rad2, double rad3, double rad4, double width, double samples) {
		int color = c.getRGB();
		Matrix4f matrix = poses.getMatrices().peek().getPositionMatrix();
		float f = (float) (color >> 24 & 255) / 255.0F;
		float g = (float) (color >> 16 & 255) / 255.0F;
		float h = (float) (color >> 8 & 255) / 255.0F;
		float k = (float) (color & 255) / 255.0F;
		setup();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		renderRoundedOutlineInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad1, rad2, rad3, rad4, width, samples);
		cleanup();
	}

	public static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

		return matrices;
	}

	/**
	 * Renders a colored quad (rectangle) on the screen.
	 *
	 * @param matrices The matrix stack to use for rendering.
	 * @param x        The x-coordinate of the top-left corner of the quad.
	 * @param y        The y-coordinate of the top-left corner of the quad.
	 * @param width    The width of the quad.
	 * @param height   The height of the quad.
	 * @param color    The color of the quad in RGBA format (e.g., 0xFF00FF00 for green).
	 */
	public static void renderQuad(MatrixStack matrices, float x, float y, float width, float height, int color) {
		float alpha = ((color >> 24) & 0xFF) / 255f;
		float red = ((color >> 16) & 0xFF) / 255f;
		float green = ((color >> 8) & 0xFF) / 255f;
		float blue = (color & 0xFF) / 255f;

		matrices.push();
		matrices.scale(0.5f, 0.5f, 0.5f); // Adjust scale if necessary
		matrices.translate(x, y, 0.0);

		Tessellator tessellator = Tessellator.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(0.0F, 0.0F, 0.0F).color(red, green, blue, alpha);
		bufferBuilder.vertex(0.0F, height, 0.0F).color(red, green, blue, alpha);
		bufferBuilder.vertex(width, height, 0.0F).color(red, green, blue, alpha);
		bufferBuilder.vertex(width, 0.0F, 0.0F).color(red, green, blue, alpha);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

		RenderSystem.disableBlend();
		matrices.pop();
	}

	public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double corner1, double corner2, double corner3, double corner4, double samples) {
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

		double[][] map = new double[][]{new double[]{toX - corner4, toY - corner4, corner4}, new double[]{toX - corner2, fromY + corner2, corner2},
				new double[]{fromX + corner1, fromY + corner1, corner1}, new double[]{fromX + corner3, toY - corner3, corner3}};
		for (int i = 0; i < 4; i++) {
			double[] current = map[i];
			double rad = current[2];
			for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
				float rad1 = (float) Math.toRadians(r);
				float sin = (float) (Math.sin(rad1) * rad);
				float cos = (float) (Math.cos(rad1) * rad);
				bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
			}
			float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
			float sin = (float) (Math.sin(rad1) * rad);
			float cos = (float) (Math.cos(rad1) * rad);
			bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
		}
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
	}

	public static void renderFilledBox(MatrixStack matrices, float f, float f2, float f3, float f4, float f5, float f6, Color color) {
		RenderSystem.enableBlend();
		RenderSystem.disableDepthTest();
		RenderSystem.setShaderColor((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f2, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f3);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f6);
		bufferBuilder.vertex(matrices.peek().getPositionMatrix(), f4, f5, f6);
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
	}

	interface RenderAction {
		void run(BufferBuilder buffer, float x, float y, float z, float x1, float y1, float z1, float red, float green, float blue, float alpha, Matrix4f matrix);
	}

	public static void renderLine(MatrixStack matrices, Color color, Vec3d start, Vec3d end) {
		matrices.push();
		Matrix4f s = matrices.peek().getPositionMatrix();
		if (ClickGUI.antiAliasing.getValue()) {
			GL11.glEnable(GL13.GL_MULTISAMPLE);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
		}
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableBlend();

		genericAABBRender(
				VertexFormat.DrawMode.DEBUG_LINES,
				VertexFormats.POSITION_COLOR,
				GameRenderer::getPositionColorProgram,
				s,
				start,
				end.subtract(start),
				color,
				(buffer, x, y, z, x1, y1, z1, red, green, blue, alpha, matrix) -> {
					buffer.vertex(matrix, x, y, z).color(red, green, blue, alpha);
					buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
				}
		);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		RenderSystem.disableBlend();
		if (ClickGUI.antiAliasing.getValue()) {
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(GL13.GL_MULTISAMPLE);
		}
		matrices.pop();
	}

	private static void genericAABBRender(VertexFormat.DrawMode mode, VertexFormat format, Supplier<ShaderProgram> shader, Matrix4f stack, Vec3d start, Vec3d dimensions, Color color, RenderAction action) {
		float red = color.getRed() / 255f;
		float green = color.getGreen() / 255f;
		float blue = color.getBlue() / 255f;
		float alpha = color.getAlpha() / 255f;
		Vec3d end = start.add(dimensions);
		float x1 = (float) start.x;
		float y1 = (float) start.y;
		float z1 = (float) start.z;
		float x2 = (float) end.x;
		float y2 = (float) end.y;
		float z2 = (float) end.z;
		useBuffer(mode, format, shader, bufferBuilder -> action.run(bufferBuilder, x1, y1, z1, x2, y2, z2, red, green, blue, alpha, stack));
	}

	private static void useBuffer(VertexFormat.DrawMode mode, VertexFormat format, Supplier<ShaderProgram> shader, Consumer<BufferBuilder> runner) {
		Tessellator t = Tessellator.getInstance();
		BufferBuilder bb = t.begin(mode, format);

		runner.accept(bb);

		setup();
		RenderSystem.setShader(shader);
		BufferRenderer.drawWithGlobalProgram(bb.end());
		cleanup();
	}

	public static void renderBlurredBox(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, Color color) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		// Render blur
		for (int i = 0; i < 3; i++) {
			float spread = (blurRadius * 0.5f) * (i + 1);
			Color blurColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / (i + 2));
			renderRoundedQuad(matrices, blurColor, x - spread, y - spread, x + width + spread, y + height + spread, 8, 8, 8, 8, 15);
		}
		
		RenderSystem.disableBlend();
	}

	public static void renderDropShadow(MatrixStack matrices, float x, float y, float width, float height, float shadowRadius, Color shadowColor) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		// Enhanced Gaussian-like drop shadow with more visible layers
		float[] alphaSteps = {0.32f, 0.24f, 0.16f, 0.08f, 0.04f};
		float[] spreadSteps = {0.6f, 1.2f, 1.8f, 2.4f, 3f};
		
		for (int i = 0; i < 5; i++) {
			float spread = shadowRadius * spreadSteps[i];
			Color currentColor = new Color(0, 0, 0, (int)(160 * alphaSteps[i]));
			renderRoundedQuad(matrices, currentColor, 
				x + spread * 0.25f,
				y + spread * 0.75f, // More vertical offset for depth
				x + width + spread * 0.25f,
				y + height + spread * 0.75f,
				8, 8, 8, 8, 15);
		}
		
		RenderSystem.disableBlend();
	}

	public static void renderBlurredBackground(MatrixStack matrices, float x, float y, float width, float height, Color color) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		// Multi-layer blur effect
		float[] alphaSteps = {0.6f, 0.4f, 0.2f, 0.1f};
		float[] spreadSteps = {1f, 2f, 3f, 4f};
		
		// Base dark background
		Color bgColor = new Color(0, 0, 0, 180);
		renderRoundedQuad(matrices, bgColor, x, y, x + width, y + height, 8, 8, 8, 8, 15);
		
		// Blur layers
		for (int i = 0; i < 4; i++) {
			float spread = spreadSteps[i];
			Color blurColor = new Color(
				color.getRed(),
				color.getGreen(),
				color.getBlue(),
				(int)(80 * alphaSteps[i])
			);
			renderRoundedQuad(matrices, blurColor, 
				x - spread,
				y - spread,
				x + width + spread,
				y + height + spread,
				8, 8, 8, 8, 15);
		}
		
		RenderSystem.disableBlend();
	}
}