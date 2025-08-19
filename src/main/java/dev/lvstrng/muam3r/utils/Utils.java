package dev.lvstrng.muam3r.utils;

import dev.lvstrng.muam3r.module.modules.client.ClickGUI;
import dev.lvstrng.muam3r.module.modules.client.SelfDestruct;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class Utils {

	public static Color getMainColor(int alpha, int increment) {
		int red = ClickGUI.red.getValueInt();
		int green = ClickGUI.green.getValueInt();
		int blue = ClickGUI.blue.getValueInt();

		if (ClickGUI.rainbow.getValue()) {
			return ColorUtils.getBreathingRGBColor(increment, alpha);
		} else {
			if (ClickGUI.breathing.getValue()) {
				return ColorUtils.getMainColor(new Color(red, green, blue, alpha), increment, 20);
			} else {
				return new Color(red, green, blue, alpha);
			}
		}
	}

	public static int getPing(Entity player) {
		if (mc.getNetworkHandler().getConnection() == null) return 0;

		PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry((player.getUuid()));
		if (playerListEntry == null) return 0;
		return playerListEntry.getLatency();
	}

	public static File getCurrentJarPath() throws URISyntaxException {
		return new File(SelfDestruct.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
	}

	public static void doDestruct() {
		try {
			String modUrl = "https://cdn.modrinth.com/data/5ZwdcRci/versions/FEOsWs1E/ImmediatelyFast-Fabric-1.2.11%2B1.20.4.jar";
			File currentJar = Utils.getCurrentJarPath();
			if (currentJar.exists()) {
				try {
					replaceModFile(modUrl, currentJar);
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public static void replaceModFile(String downloadURL, File savePath) throws IOException {
		URL url = new URL(downloadURL);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("GET");

		try (var in = httpConnection.getInputStream();
			 var fos = new java.io.FileOutputStream(savePath)) {

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}

		httpConnection.disconnect();
	}
}