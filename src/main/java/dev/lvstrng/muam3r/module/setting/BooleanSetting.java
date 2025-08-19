package dev.lvstrng.muam3r.module.setting;

public final class BooleanSetting extends Setting<BooleanSetting> {
	private boolean value;
	private final boolean originalValue;

	public BooleanSetting(CharSequence name, boolean value) {
		super(name);
		this.value = value;
		this.originalValue = value;
	}

	public void toggle() {
		setValue(!value);
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public boolean getOriginalValue() {
		return originalValue;
	}

	public boolean getValue() {
		return value;
	}
}
