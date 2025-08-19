package dev.lvstrng.muam3r.gui;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.modules.client.ClickGUI;
import dev.lvstrng.muam3r.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Window {
	public List<ModuleButton> moduleButtons = new ArrayList<>();
	public int x;
	public int y;
	private final int width, height;
	public Color currentColor;
	private final Category category;
	public boolean dragging, extended;
	private int dragX, dragY;
	private int prevX, prevY;
	public ClickGui parent;

	public Window(int x, int y, int width, int height, Category category, ClickGui parent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.dragging = false;
		this.extended = true;
		this.height = height;
		this.category = category;
		this.parent = parent;

		this.prevX = x;
		this.prevY = y;

		int offset = height;
		List<Module> sortedModules = new ArrayList<>(Muam3r.INSTANCE.getModuleManager().getModulesInCategory(category));

		for (Module module : sortedModules) {
			moduleButtons.add(new ModuleButton(this, module, offset));
			offset += height;
		}
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int toAlpha = ClickGUI.alphaWindow.getValueInt();

		if (currentColor == null)
			currentColor = new Color(0, 0, 0, 0);
		else currentColor = new Color(0, 0, 0, currentColor.getAlpha());

		if (currentColor.getAlpha() != toAlpha)
			currentColor = ColorUtils.smoothAlphaTransition(0.05F, toAlpha, currentColor);

		RenderUtils.renderRoundedQuad(context.getMatrices(), currentColor, prevX, prevY, prevX + width, prevY + height, ClickGUI.roundQuads.getValueInt(), ClickGUI.roundQuads.getValueInt(), 0, 0, 50);
		context.fill(prevX, prevY + (height - 2), prevX + width, prevY + height, Utils.getMainColor(255, moduleButtons.indexOf(moduleButtons.get(0))).getRGB());

		int charOffset = (prevX + (width / 2));
		int totalWidth = TextRenderer.getWidth(category.name);
		int startX = charOffset - (totalWidth / 2);

		TextRenderer.drawString(category.name, context, startX, prevY + 6, Color.WHITE.getRGB());

		updateButtons(delta);

		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.render(context, mouseX, mouseY, delta);
	}


	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.keyPressed(keyCode, scanCode, modifiers);
	}

	public void onGuiClose() {
		currentColor = null;

		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.onGuiClose();

		dragging = false;
	}


	public boolean isDraggingAlready() {
		for(Window window : parent.windows)
			if(window.dragging)
				return true;

		return false;
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (isHovered(mouseX, mouseY)) {
			switch (button) {
				case 0: {
					if(!parent.isDraggingAlready()) {
						dragging = true;
						dragX = (int) (mouseX - x);
						dragY = (int) (mouseY - y);
					}
					break;
				}
				case 1: {
					if (!dragging) {
						//extended = !extended;
					}
					break;
				}
			}
		}
		if (extended)
			for (ModuleButton moduleButton : moduleButtons)
				moduleButton.mouseClicked(mouseX, mouseY, button);
	}

	public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (extended) {
			for (ModuleButton moduleButton : moduleButtons) {
				moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
			}
		}
	}

	public void updateButtons(float delta) {
		int offset = height;

		for(ModuleButton moduleButton : moduleButtons) {
			moduleButton.animation.animate(0.5 * delta, moduleButton.extended ? height * (moduleButton.settings.size() + 1) : height);

			double supHeight = moduleButton.animation.getValue();
			moduleButton.offset = offset;

			offset += (int) supHeight;
		}
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && dragging)
			dragging = false;

		for (ModuleButton moduleButton : moduleButtons)
			moduleButton.mouseReleased(mouseX, mouseY, button);
	}

	public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		prevX = x;
		prevY = y;

		this.prevY = (int) (prevY + (verticalAmount * 20));
		this.setY((int) (y + (verticalAmount * 20)));
	}

	public int getX() {
		return prevX;
	}

	public int getY() {
		return prevY;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isHovered(double mouseX, double mouseY) {
		return ((mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height));
	}

	public boolean isPrevHovered(double mouseX, double mouseY) {
		return ((mouseX > prevX && mouseX < prevX + width) && (mouseY > prevY && mouseY < prevY + height));
	}

	public void updatePosition(double mouseX, double mouseY, float delta) {
		prevX = x;
		prevY = y;

		if (dragging) {
			x = (int) MathUtils.goodLerp((float) 0.3 * delta, isHovered(mouseX, mouseY) ? x : prevX, mouseX - dragX);
			y = (int) MathUtils.goodLerp((float) 0.3 * delta, isHovered(mouseX, mouseY) ? y : prevY, mouseY - dragY);
        }
	}
}
