package dev.lvstrng.muam3r.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.gui.Window;
import dev.lvstrng.muam3r.gui.components.settings.*;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.modules.client.ClickGUI;
import dev.lvstrng.muam3r.module.setting.*;
import dev.lvstrng.muam3r.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class ModuleButton {
	public List<RenderableSetting> settings = new ArrayList<>();
	public Window parent;
	public Module module;
	public int offset;
	public boolean extended;
	public int settingOffset;
	public Color currentColor;
	public Color defaultColor = Color.WHITE;
	public Color currentAlpha;
	public AnimationUtils animation = new AnimationUtils(0);

	public ModuleButton(Window parent, Module module, int offset) {
		this.parent = parent;
		this.module = module;
		this.offset = offset;
		this.extended = false;

		settingOffset = parent.getHeight();
		for (Setting<?> setting : module.getSettings()) {
			if (setting instanceof BooleanSetting booleanSetting)
				settings.add(new CheckBox(this, booleanSetting, settingOffset));
			else if (setting instanceof NumberSetting numberSetting)
				settings.add(new Slider(this, numberSetting, settingOffset));
			else if (setting instanceof ModeSetting<?> modeSetting)
				settings.add(new ModeBox(this, modeSetting, settingOffset));
			else if (setting instanceof KeybindSetting keybindSetting)
				settings.add(new KeybindBox(this, keybindSetting, settingOffset));
			else if (setting instanceof StringSetting stringSetting)
				settings.add(new StringBox(this, stringSetting, settingOffset));
			else if (setting instanceof MinMaxSetting minMaxSetting)
				settings.add(new MinMaxSlider(this, minMaxSetting, settingOffset));

			settingOffset += parent.getHeight();
		}
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (parent.getY() + offset > MinecraftClient.getInstance().getWindow().getHeight())
			return;

		for (RenderableSetting renderableSetting : settings)
			renderableSetting.onUpdate();

		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		int toAlpha = 170;

		currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);

		Color toColor = module.isEnabled() ? Utils.getMainColor(255, Muam3r.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module)) : Color.WHITE;

		if (defaultColor != toColor)
			defaultColor = ColorUtils.smoothColorTransition(0.1F, toColor, defaultColor);

		if (parent.moduleButtons.get(parent.moduleButtons.size() - 1) != this) {
			context.fill(parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset, currentColor.getRGB());
			context.fillGradient(parent.getX(), parent.getY() + offset, parent.getX() + 2, parent.getY() + parent.getHeight() + offset, Utils.getMainColor(255, Muam3r.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module)).getRGB(), Utils.getMainColor(255, Muam3r.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module) + 1).getRGB());
		} else {
			RenderUtils.renderRoundedQuad(context.getMatrices(), currentColor, parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset, 0, 0, 3, animation.getValue() > 30 ? 0 : ClickGUI.roundQuads.getValueInt(), 50);
			RenderUtils.renderRoundedQuad(context.getMatrices(), Utils.getMainColor(255, Muam3r.INSTANCE.getModuleManager().getModulesInCategory(module.getCategory()).indexOf(module)), parent.getX(), parent.getY() + offset, parent.getX() + 2, parent.getY() + (parent.getHeight() - 1) + offset, 0, 0, extended ? 0 : 2, 0, 50);
		}

		CharSequence nameChars = module.getName();

		int totalWidth = TextRenderer.getWidth(nameChars);

		int parentCenterX = parent.getX() + parent.getWidth() / 2;
		int textCenterX = parentCenterX - totalWidth / 2;

		TextRenderer.drawString(nameChars, context, textCenterX, parent.getY() + offset + 8, defaultColor.getRGB());

		renderHover(context, mouseX, mouseY, delta);
		renderSettings(context, mouseX, mouseY, delta);

		for(RenderableSetting renderableSetting : settings)
			if(extended) renderableSetting.renderDescription(context, mouseX, mouseY, delta);

		if (isHovered(mouseX, mouseY) && !parent.dragging) {
			CharSequence chars = module.getDescription();

			int tw = TextRenderer.getWidth(chars);

			int parentCenter = mc.getWindow().getFramebufferWidth() / 2;
			int textCenter = parentCenter - tw / 2;

			RenderUtils.renderRoundedQuad(
					context.getMatrices(),
					new Color(100, 100, 100, 100),
					textCenter - 5,
					((double) mc.getWindow().getFramebufferHeight() / 2) + 294,
					textCenter + tw + 5,
					((double) mc.getWindow().getFramebufferHeight() / 2) + 318,
					3,
					10
			);

			TextRenderer.drawString(chars, context, textCenter, (mc.getWindow().getFramebufferHeight() / 2) + 300, Color.WHITE.getRGB());
		}
	}

	private void renderHover(DrawContext context, int mouseX, int mouseY, float delta) {
		if (!parent.dragging) {
			int toHoverAlpha = isHovered(mouseX, mouseY) ? 15 : 0;

			if (currentAlpha == null)
				currentAlpha = new Color(255, 255, 255, toHoverAlpha);
			else currentAlpha = new Color(255, 255, 255, currentAlpha.getAlpha());

			if (currentAlpha.getAlpha() != toHoverAlpha)
				currentAlpha = ColorUtils.smoothAlphaTransition(0.05F, toHoverAlpha, currentAlpha);

			context.fill(parent.getX(), parent.getY() + offset, parent.getX() + parent.getWidth(), parent.getY() + parent.getHeight() + offset, currentAlpha.getRGB());
		}
	}

	private void renderSettings(DrawContext context, int mouseX, int mouseY, float delta) {
		int scissorX = parent.getX();
		int scissorY = (int) (mc.getWindow().getHeight() - (parent.getY() + offset + animation.getValue()));
		int scissorWidth = parent.getWidth();
		int scissorHeight = (int) animation.getValue();

		RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);

		for (RenderableSetting renderableSetting : settings)
			if(animation.getValue() > parent.getHeight())
				renderableSetting.render(context, mouseX, mouseY, delta);

		for (RenderableSetting renderableSetting : settings) {
			if(animation.getValue() > parent.getHeight()) {
				if (renderableSetting instanceof Slider slider) {
					RenderUtils.renderCircle(context.getMatrices(), new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context.getMatrices(), slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetX, 2.5))) , slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);

				} else if (renderableSetting instanceof MinMaxSlider slider) {
					RenderUtils.renderCircle(context.getMatrices(), new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetMinX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context.getMatrices(), slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetMinX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);

					RenderUtils.renderCircle(context.getMatrices(), new Color(0, 0, 0, 170), (slider.parentX() + (Math.max(slider.lerpedOffsetMaxX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 6, 15);
					RenderUtils.renderCircle(context.getMatrices(), slider.currentColor1.brighter(), (slider.parentX() + (Math.max(slider.lerpedOffsetMaxX, 2.5))), slider.parentY() + slider.offset + slider.parentOffset() + 27.5, 5, 15);
				}
			}
		}

		RenderSystem.disableScissor();
	}

	public void onExtend() {
		for(ModuleButton moduleButton : parent.moduleButtons) {
			moduleButton.extended = false;
		}
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		for (RenderableSetting setting : settings)
			setting.keyPressed(keyCode, scanCode, modifiers);
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (extended)
			for (RenderableSetting renderableSetting : settings)
				renderableSetting.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY)) {
			if (button == 0)
				module.toggle();

			if (button == 1) {
				if (module.getSettings().isEmpty()) return;
				if (!extended)
					onExtend();

				extended = !extended;
			}
		}
		if (extended) {
			for (RenderableSetting renderableSetting : settings) {
				renderableSetting.mouseClicked(mouseX, mouseY, button);
			}
		}
	}

	public void onGuiClose() {
		this.currentAlpha = null;
		this.currentColor = null;

		for (RenderableSetting renderableSetting : settings)
			renderableSetting.onGuiClose();
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		for (RenderableSetting renderableSetting : settings)
			renderableSetting.mouseReleased(mouseX, mouseY, button);
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return mouseX > parent.getX()
				&& mouseX < parent.getX() + parent.getWidth()
				&& mouseY > parent.getY() + offset
				&& mouseY < parent.getY() + offset + parent.getHeight();
	}
}