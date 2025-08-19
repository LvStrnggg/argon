package dev.lvstrng.muam3r.gui.components.settings;

import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.setting.KeybindSetting;
import dev.lvstrng.muam3r.module.setting.Setting;
import dev.lvstrng.muam3r.utils.ColorUtils;
import dev.lvstrng.muam3r.utils.KeyUtils;
import dev.lvstrng.muam3r.utils.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;


public final class KeybindBox extends RenderableSetting {
	public KeybindSetting keybind;
	private Color currentAlpha;

	public KeybindBox(ModuleButton parent, Setting<?> setting, int offset) {
		super(parent, setting, offset);
		this.keybind = (KeybindSetting) setting;
	}

	@Override
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY)) {
			if(!keybind.isListening()) {
				if(button == 0) {
					keybind.toggleListening();
					keybind.setListening(true);
				}
			} else {
				if(keybind.isModuleKey()) parent.module.setKey(button);

				keybind.setKey(button);
				keybind.setListening(false);
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			if(mouseOver) {
				if (keybind.isModuleKey())
					parent.module.setKey(keybind.getOriginalKey());

				keybind.setKey(keybind.getOriginalKey());
				keybind.setListening(false);
			}
		} else {
			if (keybind.isListening() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
				if (keybind.isModuleKey()) parent.module.setKey(keyCode);

				keybind.setKey(keyCode);
				keybind.setListening(false);
			}

			if (keybind.getKey() == GLFW.GLFW_KEY_ESCAPE) {
				if (keybind.isModuleKey()) parent.module.setKey(parent.module.getKey());

				keybind.setKey(keybind.getKey());
				keybind.setListening(false);
			}
		}
		super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int off = parentX() + 6;
		if (!keybind.isListening())
			TextRenderer.drawString(setting.getName() + ": " + KeyUtils.getKey(keybind.getKey()), context, off, (parentY() + parentOffset() + offset) + 9, new Color(245, 245, 245, 255).getRGB());
		else TextRenderer.drawString("Listening...", context, off, (parentY() + parentOffset() + offset) + 6, new Color(245, 245, 245, 255).getRGB());

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
}
