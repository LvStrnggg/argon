package dev.lvstrng.muam3r.module.modules.client;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.event.events.PacketReceiveListener;
import dev.lvstrng.muam3r.gui.ClickGui;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.module.setting.MinMaxSetting;
import dev.lvstrng.muam3r.module.setting.ModeSetting;
import dev.lvstrng.muam3r.module.setting.NumberSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.lwjgl.glfw.GLFW;

public final class ClickGUI extends Module implements PacketReceiveListener {
	public static final NumberSetting red = new NumberSetting(EncryptedString.of("Red"), 0, 255, 255, 1);
	public static final NumberSetting green = new NumberSetting(EncryptedString.of("Green"), 0, 255, 0, 1);
	public static final NumberSetting blue = new NumberSetting(EncryptedString.of("Blue"), 0, 255, 50, 1);

	public static final NumberSetting alphaWindow = new NumberSetting(EncryptedString.of("Window Alpha"), 0, 255, 170, 1);

	public static final BooleanSetting breathing = new BooleanSetting(EncryptedString.of("Breathing"), true)
			.setDescription(EncryptedString.of("Color breathing effect (only with rainbow off)"));
	public static final BooleanSetting rainbow = new BooleanSetting(EncryptedString.of("Rainbow"), true)
			.setDescription(EncryptedString.of("Enables LGBTQ mode"));

	public static final BooleanSetting background = new BooleanSetting(EncryptedString.of("Background"), false).setDescription(EncryptedString.of("Renders the background of the Click Gui"));
	public static final BooleanSetting customFont = new BooleanSetting(EncryptedString.of("Custom Font"), true);

	private final BooleanSetting preventClose = new BooleanSetting(EncryptedString.of("Prevent Close"), true)
			.setDescription(EncryptedString.of("For servers with freeze plugins that don't let you open the GUI"));

	public static final NumberSetting roundQuads = new NumberSetting(EncryptedString.of("Roundness"), 1, 10, 5, 1);
	public static final ModeSetting<AnimationMode> animationMode = new ModeSetting<>(EncryptedString.of("Animations"), AnimationMode.Normal, AnimationMode.class);
	public static final BooleanSetting antiAliasing = new BooleanSetting(EncryptedString.of("MSAA"), true)
			.setDescription(EncryptedString.of("Anti Aliasing | This can impact performance if you're using tracers but gives them a smoother look |"));

	public enum AnimationMode {
		Normal, Positive, Off;
	}

	public ClickGUI() {
		super(EncryptedString.of("Muam3r"),
				EncryptedString.of("Settings for the client"),
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				Category.CLIENT);

		addSettings(red, green, blue, alphaWindow, breathing, rainbow, background, preventClose, roundQuads, animationMode, antiAliasing);
	}

	@Override
	public void onEnable() {
		eventManager.add(PacketReceiveListener.class, this);
		Muam3r.INSTANCE.previousScreen = mc.currentScreen;

		if (Muam3r.INSTANCE.clickGui != null) {
			mc.setScreenAndRender(Muam3r.INSTANCE.clickGui);
		} else if (mc.currentScreen instanceof InventoryScreen) {
			Muam3r.INSTANCE.guiInitialized = true;
		}

		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(PacketReceiveListener.class, this);

		if (mc.currentScreen instanceof ClickGui) {
			Muam3r.INSTANCE.clickGui.close();
			mc.setScreenAndRender(Muam3r.INSTANCE.previousScreen);
			Muam3r.INSTANCE.clickGui.onGuiClose();
		} else if (mc.currentScreen instanceof InventoryScreen) {
			Muam3r.INSTANCE.guiInitialized = false;
		}

		super.onDisable();
	}


	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (Muam3r.INSTANCE.guiInitialized) {
			if (event.packet instanceof OpenScreenS2CPacket) {
				if (preventClose.getValue())
					event.cancel();
			}
		}
	}
}