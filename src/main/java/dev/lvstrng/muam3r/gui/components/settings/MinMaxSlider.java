package dev.lvstrng.muam3r.gui.components.settings;

import dev.lvstrng.muam3r.gui.components.ModuleButton;
import dev.lvstrng.muam3r.module.setting.MinMaxSetting;
import dev.lvstrng.muam3r.module.setting.Setting;
import dev.lvstrng.muam3r.utils.ColorUtils;
import dev.lvstrng.muam3r.utils.MathUtils;
import dev.lvstrng.muam3r.utils.TextRenderer;
import dev.lvstrng.muam3r.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class MinMaxSlider extends RenderableSetting {
    public boolean draggingMin;
    public boolean draggingMax;

    public double offsetMinX;
    public double offsetMaxX;

    public double lerpedOffsetMinX = parentX();
    public double lerpedOffsetMaxX = parentX() + parentWidth();

    public MinMaxSetting setting;

    public Color currentColor1;
    public Color currentColor2;
    private Color currentAlpha;

    public MinMaxSlider(ModuleButton parent, Setting<?> setting, int offset) {
        super(parent, setting, offset);
        this.setting = (MinMaxSetting) setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        MatrixStack matrices = context.getMatrices();

        offsetMinX = (setting.getMinValue() - setting.getMin()) / (setting.getMax() - setting.getMin()) * parentWidth();
        offsetMaxX = (setting.getMaxValue() - setting.getMin()) / (setting.getMax() - setting.getMin()) * parentWidth();

        lerpedOffsetMinX = MathUtils.goodLerp((float) (0.5 * delta), lerpedOffsetMinX, offsetMinX);
        lerpedOffsetMaxX = MathUtils.goodLerp((float) (0.5 * delta), lerpedOffsetMaxX, offsetMaxX);

        CharSequence str = setting.getName() + ": " + (setting.getMinValue() == setting.getMaxValue() ? setting.getMinValue() : setting.getMinValue() + " - " + setting.getMaxValue());

        context.fillGradient((int) (parentX() + lerpedOffsetMinX), parentY() + offset + parentOffset() + 25, (int) (parentX() + lerpedOffsetMinX + getLength()), parentY() + offset + parentOffset() + parentHeight(), currentColor1.getRGB(), currentColor2.getRGB());

        float scalable = 0.8F;
        matrices.push();
        matrices.scale(scalable, scalable, 1);
        TextRenderer.drawString(str, context, (int) ((parentX() + 5) / scalable), (int) (((parentY() + parentOffset() + offset) + 9) / scalable), new Color(245, 245, 245, 255).getRGB());
        matrices.scale(1, 1, 1);
        matrices.pop();

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
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            if (isHoveredMin(mouseX, mouseY) || isMouseInMin(mouseX, mouseY)) {
                draggingMin = true;
                slideMin(mouseX);
            } else if(isHoveredMax(mouseX, mouseY) || isMouseInMax(mouseX, mouseY)) {
                draggingMax = true;
                slideMax(mouseX);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if(mouseOver && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            setting.setMaxValue(setting.getOriginalMaxValue());
            setting.setMinValue(setting.getOriginalMinValue());
        }

        super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isHoveredMin(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY)
                && mouseX > parentX() + offsetMinX - 4
                && mouseX < parentX() + offsetMinX + 4;
    }

    public boolean isHoveredMax(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY)
                && mouseX > parentX() + offsetMaxX - 4
                && mouseX < parentX() + offsetMaxX + 4;
    }

    public double getLength() {
        return lerpedOffsetMaxX - lerpedOffsetMinX;
    }

    public boolean isMouseInMin(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY)
                && (mouseX <= parentX() + offsetMinX
                || mouseX < parentX() + offsetMinX + (getLength() / 2));
    }

    public boolean isMouseInMax(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY)
                && (mouseX > parentX() + offsetMaxX
                || mouseX > parentX() + offsetMinX + (getLength() / 2));
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0) {
            if (draggingMin)
                draggingMin = false;

            if(draggingMax)
                draggingMax = false;
        }
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(draggingMin)
            slideMin(mouseX);

        if(draggingMax && !draggingMin)
            slideMax(mouseX);
        super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onGuiClose() {
        this.currentColor1 = null;
        this.currentColor2 = null;
        super.onGuiClose();
    }

    private void slideMin(double mouseX) {
        double a = mouseX - parentX();
        double b = MathHelper.clamp(a / parentWidth(), 0, 1);
        setting.setMinValue(MathUtils.roundToDecimal(b * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getIncrement()));
    }

    private void slideMax(double mouseX) {
        double a = mouseX - parentX();
        double b = MathHelper.clamp(a / parentWidth(), 0, 1);
        setting.setMaxValue(MathUtils.roundToDecimal(b * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getIncrement()));
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

        if(draggingMin)
            draggingMax = false;

        if(setting.getMinValue() > setting.getMaxValue()) {
            setting.setMaxValue(setting.getMinValue());
        }

        if(setting.getMaxValue() < setting.getMinValue()) {
            setting.setMinValue(setting.getMaxValue() - setting.getIncrement());
        }

        super.onUpdate();
    }
}
