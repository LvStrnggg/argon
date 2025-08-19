package dev.lvstrng.muam3r.gui.components.settings;

import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.module.setting.Setting;
import dev.lvstrng.muam3r.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class Slider extends RenderableSetting {
	public boolean dragging;
	public double offsetX;
	public double lerpedOffsetX = 0;

	private final NumberSetting setting;

	public Color currentColor1;
	public Color currentColor2;
	private Color currentAlpha;

    public Slider(ModuleButton parent, Setting<?> setting, int offset) {
		super(parent, setting, offset);
		this.setting = (NumberSetting) setting;
	}

	@Override
	public void onUpdate() {
        Color clr = Utils.getMainColor(0, parent.settings.indexOf(this)).darker();
        Color clr2 = Utils.getMainColor(0, parent.settings.indexOf(this) + 1).darker();

		if (currentColor1 == null)
			currentColor1 = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
		else currentColor1 = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), currentColor1.getAlpha());

		if (currentColor2 == null)
			currentColor2 = new Color(clr2.getRed(), clr2.getGreen(), clr2.getBlue(), 0);
		else currentColor2 = new Color(clr2.getRed(), clr2.getGreen(), clr2.getBlue(), currentColor2.getAlpha());

		int toAlpha = 255;

		if (currentColor1.getAlpha() != toAlpha)
			currentColor1 = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor1);

		if (currentColor2.getAlpha() != toAlpha)
			currentColor2 = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor2);

		super.onUpdate();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		offsetX = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()) * parentWidth();
		lerpedOffsetX = MathUtils.goodLerp((float) (0.5 * delta), lerpedOffsetX, offsetX);
		context.fillGradient(parentX(), parentY() + offset + parentOffset() + 25, (int) (parentX() + lerpedOffsetX), parentY() + offset + parentOffset() + parentHeight(), currentColor1.getRGB(), currentColor2.getRGB());

		TextRenderer.drawString(setting.getName() + ": " + setting.getValue(), context, parentX() + 5, (parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());

		if (!parent.parent.dragging) {
			int toHoverAlpha = isHovered(mouseX, mouseY) ? 15 : 0;

			if (currentAlpha == null)
				currentAlpha = new Color(255, 255, 255, toHoverAlpha);
			else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());

			if (currentAlpha.getAlpha() != toHoverAlpha)
				currentAlpha = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, currentAlpha);

			context.fill(parentX(), parentY() + parentOffset() + offset, parentX() + parentWidth(), parentY() + parentOffset() + offset + parentHeight(), currentAlpha.getRGB());
		}
	}

	@Override
	public void onGuiClose() {
		this.currentColor1 = null;
		this.currentColor2 = null;
		super.onGuiClose();
	}

	private void slide(double mouseX) {
		double a = mouseX - parentX();
		double b = MathHelper.clamp(a / parentWidth(), 0, 1);
		setting.setValue(MathUtils.roundToDecimal(b * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getIncrement()));
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if(mouseOver && parent.extended) {
			if(keyCode == GLFW.GLFW_KEY_BACKSPACE)
				setting.setValue(setting.getOriginalValue());
		}
		super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY) && button == 0) {
			dragging = true;
			slide(mouseX);
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (dragging && button == 0)
			dragging = false;

		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging)
			slide(mouseX);

		super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
}
