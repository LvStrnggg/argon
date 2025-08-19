package dev.lvstrng.muam3r.gui.components.settings;

import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.setting.Setting;
import dev.lvstrng.muam3r.utils.ColorUtils;
import dev.lvstrng.muam3r.utils.RenderUtils;
import dev.lvstrng.muam3r.utils.TextRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class RenderableSetting {
	public MinecraftClient mc = MinecraftClient.getInstance();
	public ModuleButton parent;
	public Setting<?> setting;
	public int offset;
	public Color currentColor;
	public boolean mouseOver;
	int x;
	int y;
	int width;
	int height;

	public RenderableSetting(ModuleButton parent, Setting<?> setting, int offset) {
		this.parent = parent;
		this.setting = setting;
		this.offset = offset;

		this.x = parentX();
		this.y = parentY() + parentOffset() + offset;
		this.width = parentX() + parentWidth();
		this.height = parentY() + parentOffset() + offset + parentHeight();
	}

	public int parentX() {
		return parent.parent.getX();
	}

	public int parentY() {
		return parent.parent.getY();
	}

	public int parentWidth() {
		return parent.parent.getWidth();
	}

	public int parentHeight() {
		return parent.parent.getHeight();
	}

	public int parentOffset() {
		return parent.offset;
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		updateMouseOver(mouseX, mouseY);
		this.x = parentX();
		this.y = parentY() + parentOffset() + offset;
		this.width = parentX() + parentWidth();
		this.height = parentY() + parentOffset() + offset + parentHeight();

		context.fill(x, y, width, height, currentColor.getRGB());
	}

	private void updateMouseOver(double mouseX, double mouseY) {
		this.mouseOver = isHovered(mouseX, mouseY);
	}

	public void renderDescription(DrawContext context, int mouseX, int mouseY, float delta) {
		if (isHovered(mouseX, mouseY) && setting.getDescription() != null && !parent.parent.dragging) {
			CharSequence chars = setting.getDescription();

			int tw = TextRenderer.getWidth(chars);

			int parentCenter = mc.getWindow().getWidth() / 2;
			int textCenter = parentCenter - tw / 2;

			RenderUtils.renderRoundedQuad(
					context.getMatrices(),
					new Color(100, 100, 100, 100),
					textCenter - 5,
					(mc.getWindow().getHeight() / 2) + 294,
					textCenter + tw + 5,
					(mc.getWindow().getHeight() / 2) + 318,
					3,
					10
			);

			TextRenderer.drawString(chars, context, textCenter, (mc.getWindow().getHeight() / 2) + 300, Color.WHITE.getRGB());
		}
	}

	public void onGuiClose() {
		this.currentColor = null;
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return mouseX > parentX()
				&& mouseX < parentX() + parentWidth()
				&& mouseY > offset + parentOffset() + parentY()
				&& mouseY < offset + parentOffset() + parentY() + parentHeight();
	}

	public void onUpdate() {
		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		int toAlpha = 120;

		if (currentColor.getAlpha() != toAlpha)
			currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
	}
}