package dev.lvstrng.muam3r.gui.components.settings;

import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.setting.ModeSetting;
import dev.lvstrng.muam3r.module.setting.Setting;
import dev.lvstrng.muam3r.utils.ColorUtils;
import dev.lvstrng.muam3r.utils.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class ModeBox extends RenderableSetting {
	public final ModeSetting<?> setting;
	private Color currentAlpha;

	public ModeBox(ModuleButton parent, Setting<?> setting, int offset) {
		super(parent, setting, offset);
		this.setting = (ModeSetting<?>) setting;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int nameOffset = parentX() + 6;
		TextRenderer.drawString(setting.getName() + ": ", context, nameOffset, (parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());
		nameOffset += TextRenderer.getWidth(setting.getName() + ": ");

		int modeOffset = nameOffset;

		TextRenderer.drawString(setting.getMode().name(), context, modeOffset, (parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());

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
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if(mouseOver && parent.extended) {
			if(keyCode == GLFW.GLFW_KEY_BACKSPACE)
				setting.setModeIndex(setting.getOriginalValue());
		}
		super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
			setting.cycle();

		super.mouseClicked(mouseX, mouseY, button);
	}
}
