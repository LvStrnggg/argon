package dev.lvstrng.muam3r;

import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.net.URISyntaxException;

public final class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		try {
			new Muam3r();
		} catch (InterruptedException | IOException ignored) {}
	}
}
