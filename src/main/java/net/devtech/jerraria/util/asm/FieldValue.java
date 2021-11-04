package net.devtech.jerraria.util.asm;

public record FieldValue(String name, Object value) {
	public static FieldValue of(String name, Object value) {
		return new FieldValue(name, value);
	}
}
