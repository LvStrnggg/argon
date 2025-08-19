package dev.lvstrng.muam3r.module.modules.render;

import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.event.events.HudListener;
import dev.lvstrng.muam3r.gui.ClickGui;
import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.modules.client.ClickGUI;
import dev.lvstrng.muam3r.module.setting.BooleanSetting;
import dev.lvstrng.muam3r.utils.EncryptedString;
import dev.lvstrng.muam3r.utils.RenderUtils;
import dev.lvstrng.muam3r.utils.TextRenderer;
import dev.lvstrng.muam3r.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.awt.*;
import java.util.List;

public final class HUD extends Module implements HudListener {
	private static final CharSequence argon = EncryptedString.of("Muam3r |");
	private final BooleanSetting info = new BooleanSetting(EncryptedString.of("Info"), true);
	private final BooleanSetting modules = new BooleanSetting("Modules", true)
			.setDescription(EncryptedString.of("Renders module array list"));

	public HUD() {
		super(EncryptedString.of("HUD"),
				EncryptedString.of("Renders the client version and enabled modules on the HUD"),
				-1,
				Category.RENDER);
		addSettings(info, modules);
	}

	@Override
	public void onEnable() {
		eventManager.add(HudListener.class, this);
		super.onEnable();
	}

	@Override
	public void onDisable() {
		eventManager.remove(HudListener.class, this);
		super.onDisable();
	}

	@Override
	public void onRenderHud(HudEvent event) {
		if (mc.currentScreen != Muam3r.INSTANCE.clickGui) {
			final List<Module> enabledModules = Muam3r.INSTANCE.
					getModuleManager().
					getEnabledModules().
					stream().
					sorted((module1, module2) -> {
						CharSequence name1 = module1.getName();
						CharSequence name2 = module2.getName();

						int filteredLength1 = TextRenderer.getWidth(name1);
						int filteredLength2 = TextRenderer.getWidth(name2);

						return Integer.compare(filteredLength2, filteredLength1);
					}).
					toList();

			DrawContext context = event.context;
			boolean customFont = ClickGUI.customFont.getValue();

			if (!(mc.currentScreen instanceof ClickGui)) {

				if (info.getValue() && mc.player != null) {
					RenderUtils.unscaledProjection();
					int argonOffset = 10;
					int argonOffset2 = 10 + TextRenderer.getWidth(argon);

					String ping = "Ping: "; // shrimple null check
					String fps = "FPS: " + mc.getCurrentFps() + " |";
					String server = mc.getCurrentServerEntry() == null ? "None" : mc.getCurrentServerEntry().address;
					if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
						PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
						if (entry != null) {
							ping += entry.getLatency() + " |";
						} else {
							ping += "N/A |";
						}
					} else {
						ping += "N/A |";
					}

					RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(35, 35, 35, 255), 5, 6, argonOffset2 + TextRenderer.getWidth(fps) + TextRenderer.getWidth(ping) + TextRenderer.getWidth(server) + 35, 30, 5, 15);

					TextRenderer.drawString(argon, context, argonOffset, 12, Utils.getMainColor(255, 4).getRGB());
					argonOffset += TextRenderer.getWidth(argon);

					TextRenderer.drawString(fps, context, argonOffset + 10, 12, Utils.getMainColor(255, 3).getRGB());
					TextRenderer.drawString(ping, context, (argonOffset + 10) + TextRenderer.getWidth(fps) + 10, 12, Utils.getMainColor(255, 2).getRGB());
					TextRenderer.drawString(server, context, (argonOffset + 10) + TextRenderer.getWidth(fps) + TextRenderer.getWidth(ping) + 20, 12, Utils.getMainColor(255, 1).getRGB());

					RenderUtils.scaledProjection();
				}
				if (modules.getValue()) {
					int offset = 55;
					for (Module module : enabledModules) {
						RenderUtils.unscaledProjection();
						int charOffset = 6 + TextRenderer.getWidth(module.getName());

						RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(0, 0, 0, 175), 0, offset - 4, (charOffset + 5), offset + (mc.textRenderer.fontHeight * 2) - 1, 0, 0, 0, 5, 10);
						context.fillGradient(0, offset - 4, 2, offset + (mc.textRenderer.fontHeight * 2), Utils.getMainColor(255, (enabledModules.indexOf(module))).getRGB(), Utils.getMainColor(255, (enabledModules.indexOf(module)) + 1).getRGB());

						int charOffset2 = customFont ? 5 : 8;

						TextRenderer.drawString(module.getName(), context, charOffset2, offset + (customFont ? 1 : 0), Utils.getMainColor(255, (enabledModules.indexOf(module))).getRGB());

						offset += (mc.textRenderer.fontHeight * 2) + 3;
						RenderUtils.scaledProjection();
					}
				}
			}
		}
	}
}