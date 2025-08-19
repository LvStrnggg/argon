package dev.lvstrng.muam3r.utils;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import static dev.lvstrng.muam3r.Muam3r.mc;

public final class KeyUtils {

	public static CharSequence getKey(int key) {
		switch (key) {
			case GLFW.GLFW_MOUSE_BUTTON_3 -> {
				return EncryptedString.of("MMB");
			}
			case GLFW.GLFW_KEY_UNKNOWN -> {
				return EncryptedString.of("Unknown");
			}
			case GLFW.GLFW_KEY_ESCAPE -> {
				return EncryptedString.of("Esc");
			}
			case GLFW.GLFW_KEY_GRAVE_ACCENT -> {
				return EncryptedString.of("Grave Accent");
			}
			case GLFW.GLFW_KEY_WORLD_1 -> {
				return EncryptedString.of("World 1");
			}
			case GLFW.GLFW_KEY_WORLD_2 -> {
				return EncryptedString.of("World 2");
			}
			case GLFW.GLFW_KEY_PRINT_SCREEN -> {
				return EncryptedString.of("Print Screen");
			}
			case GLFW.GLFW_KEY_PAUSE -> {
				return EncryptedString.of("Pause");
			}
			case GLFW.GLFW_KEY_INSERT -> {
				return EncryptedString.of("Insert");
			}
			case GLFW.GLFW_KEY_DELETE -> {
				return EncryptedString.of("Delete");
			}
			case GLFW.GLFW_KEY_HOME -> {
				return EncryptedString.of("Home");
			}
			case GLFW.GLFW_KEY_PAGE_UP -> {
				return EncryptedString.of("Page Up");
			}
			case GLFW.GLFW_KEY_PAGE_DOWN -> {
				return EncryptedString.of("Page Down");
			}
			case GLFW.GLFW_KEY_END -> {
				return EncryptedString.of("End");
			}
			case GLFW.GLFW_KEY_TAB -> {
				return EncryptedString.of("Tab");
			}
			case GLFW.GLFW_KEY_LEFT_CONTROL -> {
				return EncryptedString.of("Left Control");
			}
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> {
				return EncryptedString.of("Right Control");
			}
			case GLFW.GLFW_KEY_LEFT_ALT -> {
				return EncryptedString.of("Left Alt");
			}
			case GLFW.GLFW_KEY_RIGHT_ALT -> {
				return EncryptedString.of("Right Alt");
			}
			case GLFW.GLFW_KEY_LEFT_SHIFT -> {
				return EncryptedString.of("Left Shift");
			}
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> {
				return EncryptedString.of("Right Shift");
			}
			case GLFW.GLFW_KEY_UP -> {
				return EncryptedString.of("Arrow Up");
			}
			case GLFW.GLFW_KEY_DOWN -> {
				return EncryptedString.of("Arrow Down");
			}
			case GLFW.GLFW_KEY_LEFT -> {
				return EncryptedString.of("Arrow Left");
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				return EncryptedString.of("Arrow Right");
			}
			case GLFW.GLFW_KEY_APOSTROPHE -> {
				return EncryptedString.of("Apostrophe");
			}
			case GLFW.GLFW_KEY_BACKSPACE -> {
				return EncryptedString.of("Backspace");
			}
			case GLFW.GLFW_KEY_CAPS_LOCK -> {
				return EncryptedString.of("Caps Lock");
			}
			case GLFW.GLFW_KEY_MENU -> {
				return EncryptedString.of("Menu");
			}
			case GLFW.GLFW_KEY_LEFT_SUPER -> {
				return EncryptedString.of("Left Super");
			}
			case GLFW.GLFW_KEY_RIGHT_SUPER -> {
				return EncryptedString.of("Right Super");
			}
			case GLFW.GLFW_KEY_ENTER -> {
				return EncryptedString.of("Enter");
			}
			case GLFW.GLFW_KEY_KP_ENTER -> {
				return EncryptedString.of("Numpad Enter");
			}
			case GLFW.GLFW_KEY_NUM_LOCK -> {
				return EncryptedString.of("Num Lock");
			}
			case GLFW.GLFW_KEY_SPACE -> {
				return EncryptedString.of("Space");
			}
			case GLFW.GLFW_KEY_F1 -> {
				return EncryptedString.of("F1");
			}
			case GLFW.GLFW_KEY_F2 -> {
				return EncryptedString.of("F2");
			}
			case GLFW.GLFW_KEY_F3 -> {
				return EncryptedString.of("F3");
			}
			case GLFW.GLFW_KEY_F4 -> {
				return EncryptedString.of("F4");
			}
			case GLFW.GLFW_KEY_F5 -> {
				return EncryptedString.of("F5");
			}
			case GLFW.GLFW_KEY_F6 -> {
				return EncryptedString.of("F6");
			}
			case GLFW.GLFW_KEY_F7 -> {
				return EncryptedString.of("F7");
			}
			case GLFW.GLFW_KEY_F8 -> {
				return EncryptedString.of("F8");
			}
			case GLFW.GLFW_KEY_F9 -> {
				return EncryptedString.of("F9");
			}
			case GLFW.GLFW_KEY_F10 -> {
				return EncryptedString.of("F10");
			}
			case GLFW.GLFW_KEY_F11 -> {
				return EncryptedString.of("F11");
			}
			case GLFW.GLFW_KEY_F12 -> {
				return EncryptedString.of("F12");
			}
			case GLFW.GLFW_KEY_F13 -> {
				return EncryptedString.of("F13");
			}
			case GLFW.GLFW_KEY_F14 -> {
				return EncryptedString.of("F14");
			}
			case GLFW.GLFW_KEY_F15 -> {
				return EncryptedString.of("F15");
			}
			case GLFW.GLFW_KEY_F16 -> {
				return EncryptedString.of("F16");
			}
			case GLFW.GLFW_KEY_F17 -> {
				return EncryptedString.of("F17");
			}
			case GLFW.GLFW_KEY_F18 -> {
				return EncryptedString.of("F18");
			}
			case GLFW.GLFW_KEY_F19 -> {
				return EncryptedString.of("F19");
			}
			case GLFW.GLFW_KEY_F20 -> {
				return EncryptedString.of("F20");
			}
			case GLFW.GLFW_KEY_F21 -> {
				return EncryptedString.of("F21");
			}
			case GLFW.GLFW_KEY_F22 -> {
				return EncryptedString.of("F22");
			}
			case GLFW.GLFW_KEY_F23 -> {
				return EncryptedString.of("F23");
			}
			case GLFW.GLFW_KEY_F24 -> {
				return EncryptedString.of("F24");
			}
			case GLFW.GLFW_KEY_F25 -> {
				return EncryptedString.of("F25");
			}
			case GLFW.GLFW_KEY_SCROLL_LOCK -> {
				return EncryptedString.of("Scroll Lock");
			}
			case GLFW.GLFW_KEY_LEFT_BRACKET -> {
				return EncryptedString.of("Left Bracket");
			}
			case GLFW.GLFW_KEY_RIGHT_BRACKET -> {
				return EncryptedString.of("Right Bracket");
			}
			case GLFW.GLFW_KEY_SEMICOLON -> {
				return EncryptedString.of("Semicolon");
			}
			case GLFW.GLFW_KEY_EQUAL -> {
				return EncryptedString.of("Equals");
			}
			case GLFW.GLFW_KEY_BACKSLASH -> {
				return EncryptedString.of("Backslash");
			}
			case GLFW.GLFW_KEY_COMMA -> {
				return EncryptedString.of("Comma");
			}
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
				return EncryptedString.of("LMB");
			}
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
				return EncryptedString.of("RMB");
			}
			default -> {
				String keyName = GLFW.glfwGetKeyName(key, 0);
				if (keyName == null) return EncryptedString.of("None");
				return StringUtils.capitalize(keyName);
			}
		}
	}

	public static boolean isKeyPressed(int keyCode) {
		if (keyCode <= 8)
			return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;

		return GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
	}
}
