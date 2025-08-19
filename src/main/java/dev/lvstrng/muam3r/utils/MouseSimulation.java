package dev.lvstrng.muam3r.utils;

import dev.lvstrng.muam3r.mixin.MinecraftClientAccessor;
import dev.lvstrng.muam3r.mixin.MouseHandlerAccessor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.lvstrng.muam3r.Muam3r.mc;


public final class MouseSimulation {
	public static HashMap<Integer, Boolean> mouseButtons = new HashMap<>();
	public static ExecutorService clickExecutor = Executors.newFixedThreadPool(100);

	public static MouseHandlerAccessor getMouseHandler() {
		return (MouseHandlerAccessor) ((MinecraftClientAccessor) mc).getMouse();
	}

	public static boolean isMouseButtonPressed(int keyCode) {
		Boolean key = mouseButtons.get(keyCode);
		return key != null ? key : false;
	}

	public static void mousePress(int keyCode) {
		mouseButtons.put(keyCode, true);
		getMouseHandler().press(mc.getWindow().getHandle(), keyCode, GLFW.GLFW_PRESS, 0);
	}

	public static void mouseRelease(int keyCode) {
		getMouseHandler().press(mc.getWindow().getHandle(), keyCode, GLFW.GLFW_RELEASE, 0);
	}

	public static void mouseClick(int keyCode, int millis) {
		clickExecutor.submit(() -> {
			try {
				MouseSimulation.mousePress(keyCode);
				Thread.sleep(millis);
				MouseSimulation.mouseRelease(keyCode);
			} catch (InterruptedException ignored) {

			}
		});
	}

	public static void mouseClick(int keyCode) {
		mouseClick(keyCode, 35);
	}
}
