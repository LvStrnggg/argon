package dev.lvstrng.muam3r.managers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lvstrng.muam3r.Muam3r;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.module.setting.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProfileManager {
	private final Gson g = new Gson();
	private Path profileFolderPath;
	private Path profilePath;
	private String temp = System.getProperty("java.io.tmpdir");
	private String folderName = "UJHfsGGjbPfVZ";
	Path folder = Paths.get(temp, folderName);
	private JsonObject profile;

	public ProfileManager() {
		profileFolderPath = folder;
		profilePath = profileFolderPath.resolve("a.json");
	}

	public void loadProfile() {
		try {
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				temp = System.getProperty("user.home");
				folderName = "UJHfsGGjbPfVZ";
				profileFolderPath = folder;
				profilePath = profileFolderPath.resolve("a.json");

				if (!Files.isRegularFile(profilePath))
					return;

				profile = g.fromJson(Files.readString(profilePath), JsonObject.class);

				for (Module module : Muam3r.INSTANCE.getModuleManager().getModules()) {
					JsonElement moduleJson = profile.get(String.valueOf(Muam3r.INSTANCE.getModuleManager().getModules().indexOf(module)));
					if (moduleJson == null || !moduleJson.isJsonObject())
						continue;
					JsonObject moduleConfig = moduleJson.getAsJsonObject();

					JsonElement enabledJson = moduleConfig.get("enabled");
					if (enabledJson == null || !enabledJson.isJsonPrimitive())
						continue;

					if (enabledJson.getAsBoolean())
						module.setEnabled(true);

					for (Setting<?> setting : module.getSettings()) {
						JsonElement settingJson = moduleConfig.get(String.valueOf(module.getSettings().indexOf(setting)));
						if (settingJson == null)
							continue;

						if (setting instanceof BooleanSetting booleanSetting) {
							booleanSetting.setValue(settingJson.getAsBoolean());
						} else if (setting instanceof ModeSetting<?> modeSetting) {
							modeSetting.setModeIndex(settingJson.getAsInt());
						} else if (setting instanceof NumberSetting numberSetting) {
							numberSetting.setValue(settingJson.getAsDouble());
						} else if (setting instanceof KeybindSetting keybindSetting) {
							keybindSetting.setKey(settingJson.getAsInt());
							if(keybindSetting.isModuleKey())
								module.setKey(settingJson.getAsInt());
						} else if (setting instanceof StringSetting stringSetting) {
							stringSetting.setValue(settingJson.getAsString());
						} else if (setting instanceof MinMaxSetting minMaxSetting) {
							if (settingJson.isJsonObject()) {
								JsonObject minMaxObject = settingJson.getAsJsonObject();
								double minValue = minMaxObject.get("1").getAsDouble();
								double maxValue = minMaxObject.get("2").getAsDouble();

								minMaxSetting.setMinValue(minValue);
								minMaxSetting.setMaxValue(maxValue);
							}
						}
					}

				}
			} else {

				if (!Files.isRegularFile(profilePath))
					return;

				profile = g.fromJson(Files.readString(profilePath), JsonObject.class);

				for (Module module : Muam3r.INSTANCE.getModuleManager().getModules()) {
					JsonElement moduleJson = profile.get(String.valueOf(Muam3r.INSTANCE.getModuleManager().getModules().indexOf(module)));
					if (moduleJson == null || !moduleJson.isJsonObject())
						continue;
					JsonObject moduleConfig = moduleJson.getAsJsonObject();

					JsonElement enabledJson = moduleConfig.get("enabled");
					if (enabledJson == null || !enabledJson.isJsonPrimitive())
						continue;

					if (enabledJson.getAsBoolean())
						module.setEnabled(true);

					for (Setting<?> setting : module.getSettings()) {
						JsonElement settingJson = moduleConfig.get(String.valueOf(module.getSettings().indexOf(setting)));
						if (settingJson == null)
							continue;

						if (setting instanceof BooleanSetting booleanSetting) {
							booleanSetting.setValue(settingJson.getAsBoolean());
						} else if (setting instanceof ModeSetting<?> modeSetting) {
							modeSetting.setModeIndex(settingJson.getAsInt());
						} else if (setting instanceof NumberSetting numberSetting) {
							numberSetting.setValue(settingJson.getAsDouble());
						} else if (setting instanceof KeybindSetting keybindSetting) {
							keybindSetting.setKey(settingJson.getAsInt());
							if(keybindSetting.isModuleKey())
								module.setKey(settingJson.getAsInt());
						} else if (setting instanceof StringSetting stringSetting) {
							stringSetting.setValue(settingJson.getAsString());
						} else if (setting instanceof MinMaxSetting minMaxSetting) {
							if (settingJson.isJsonObject()) {
								JsonObject minMaxObject = settingJson.getAsJsonObject();
								double minValue = minMaxObject.get("1").getAsDouble();
								double maxValue = minMaxObject.get("2").getAsDouble();

								minMaxSetting.setMinValue(minValue);
								minMaxSetting.setMaxValue(maxValue);
							}
						}
					}

				}
			}
		} catch (Exception ignored) {
		}
	}

	public void saveProfile() {
		try {
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				temp = System.getProperty("user.home");
				folderName = "UJHfsGGjbPfVZ";
				profileFolderPath = folder;
				profilePath = profileFolderPath.resolve("a.json");
				Files.createDirectories(profileFolderPath);
				profile = new JsonObject();

				for (Module module : Muam3r.INSTANCE.getModuleManager().getModules()) {
					JsonObject moduleConfig = new JsonObject();

					moduleConfig.addProperty("enabled", module.isEnabled());
					for (Setting<?> setting : module.getSettings()) {
						if (setting instanceof BooleanSetting booleanSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), booleanSetting.getValue());
						} else if (setting instanceof ModeSetting<?> modeSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), modeSetting.getModeIndex());
						} else if (setting instanceof NumberSetting numberSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), numberSetting.getValue());
						} else if (setting instanceof KeybindSetting keybindSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), keybindSetting.getKey());
						} else if (setting instanceof StringSetting stringSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), stringSetting.getValue());
						} else if (setting instanceof MinMaxSetting minMaxSetting) {
							JsonObject minMaxObject = new JsonObject();
							minMaxObject.addProperty("1", minMaxSetting.getMinValue());
							minMaxObject.addProperty("2", minMaxSetting.getMaxValue());

							moduleConfig.add(String.valueOf(module.getSettings().indexOf(setting)), minMaxObject);
						}
					}

					profile.add(String.valueOf(Muam3r.INSTANCE.getModuleManager().getModules().indexOf(module)), moduleConfig);
				}
				Files.writeString(profilePath, g.toJson(profile));
			} else {
				Files.createDirectories(profileFolderPath);
				profile = new JsonObject();

				for (Module module : Muam3r.INSTANCE.getModuleManager().getModules()) {
					JsonObject moduleConfig = new JsonObject();

					moduleConfig.addProperty("enabled", module.isEnabled());
					for (Setting<?> setting : module.getSettings()) {
						if (setting instanceof BooleanSetting booleanSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), booleanSetting.getValue());
						} else if (setting instanceof ModeSetting<?> modeSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), modeSetting.getModeIndex());
						} else if (setting instanceof NumberSetting numberSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), numberSetting.getValue());
						} else if (setting instanceof KeybindSetting keybindSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), keybindSetting.getKey());
						} else if (setting instanceof StringSetting stringSetting) {
							moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), stringSetting.getValue());
						} else if (setting instanceof MinMaxSetting minMaxSetting) {
							JsonObject minMaxObject = new JsonObject();
							minMaxObject.addProperty("1", minMaxSetting.getMinValue());
							minMaxObject.addProperty("2", minMaxSetting.getMaxValue());

							moduleConfig.add(String.valueOf(module.getSettings().indexOf(setting)), minMaxObject);
						}
					}

					profile.add(String.valueOf(Muam3r.INSTANCE.getModuleManager().getModules().indexOf(module)), moduleConfig);
				}
				Files.writeString(profilePath, g.toJson(profile));
			}
		} catch (Exception ignored) {
		}
	}
}