package net.devtech.jerraria.world.tile;

import java.util.List;
import java.util.Objects;

import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.util.data.NativeJCType;

public class EnumProperty<E extends Enum<E>> implements Property<E, String> {
	final Class<E> type;
	final List<E> values;
	final E defaultValue;
	final String name;

	EnumProperty(String name, Class<E> type, E defaultValue) {
		this.defaultValue = Objects.requireNonNull(defaultValue, "cannot have null default value for enum");
		this.type = type;
		this.values = List.of(type.getEnumConstants());
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<E> values() {
		return this.values;
	}

	@Override
	public int defaultIndex() {
		return this.defaultValue.ordinal();
	}

	@Override
	public E defaultValue() {
		return this.defaultValue;
	}

	@Override
	public int indexOfValue(E value) {
		return value.ordinal();
	}

	@Override
	public JCElement<String> convert(E value) {
		return JCElement.create(NativeJCType.STRING, value.name());
	}

	@Override
	public E readFrom(JCElement<String> element) {
		String value = element.value();
		return Enum.valueOf(this.type, value);
	}

	public Class<E> getType() {
		return this.type;
	}

	public List<E> getValues() {
		return this.values;
	}

	public E getDefaultValue() {
		return this.defaultValue;
	}
}
