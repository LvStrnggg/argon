package dev.lvstrng.muam3r.module.setting;

public final class NumberSetting extends Setting<NumberSetting> {
	
	private double min;
	
	private double max;
	private double value;
	
	private double increment;
	
	private final double originalValue;

	public NumberSetting(CharSequence name, double min, double max, double value, double increment) {
		super(name);
		this.min = min;
		this.max = max;
		this.value = value;
		this.increment = increment;
		this.originalValue = value;
	}

	public double getValue() {
		return value;
	}

	public double getOriginalValue() {
		return originalValue;
	}

	public double getIncrement() {
		return increment;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public int getValueInt() {
		return (int) value;
	}

	public float getValueFloat() {
		return (float) value;
	}

	public long getValueLong() {
		return (long) value;
	}

	public void setValue(double value) {
		double precision = 1.0D / this.increment;
		this.value = Math.round(Math.max(this.min, Math.min(this.max, value)) * precision) / precision;
	}
}
