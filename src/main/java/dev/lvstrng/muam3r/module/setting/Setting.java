package dev.lvstrng.muam3r.module.setting;

public abstract class Setting<T extends Setting<T>> {
	private CharSequence name;
	public CharSequence description;

	public Setting(CharSequence name) {
		this.name = name;
	}

	public void setName(CharSequence name) {
		this.name = name;
	}

	public CharSequence getName() {
		return name;
	}

	public CharSequence getDescription() {
		return description;
	}

	public T setDescription(CharSequence desc) {
		this.description = desc;
		//noinspection unchecked
		return (T) this;
	}
}
