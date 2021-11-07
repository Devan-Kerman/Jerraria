package net.devtech.jerraria.world.tile;

import java.util.List;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import net.devtech.jerraria.util.data.JCElement;
import net.devtech.jerraria.util.data.NativeJCType;

public class IntRangeProperty extends AbstractIntList implements Property<Integer, Integer> {
	final int from, length, defaultValue;
	final String name;

	/**
	 * @param from inclusive
	 * @param to exclusive
	 */
	IntRangeProperty(String name, int from, int to, int defaultValue) {
		this.name = name;
		if(defaultValue >= to) {
			throw new IllegalArgumentException(defaultValue + " >= " + to);
		} else if(defaultValue < from) {
			throw new IllegalArgumentException(defaultValue + " < " + from);
		}
		this.from = from;
		this.length = to - from;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<Integer> values() {
		return this;
	}

	@Override
	public int defaultIndex() {
		return this.indexOf(this.defaultValue);
	}

	@Override
	public int indexOfValue(Integer value) {
		return Property.super.indexOfValue(value);
	}

	@Override
	public JCElement<Integer> convert(Integer value) {
		return new JCElement<>(NativeJCType.INT, value);
	}

	@Override
	public Integer readFrom(JCElement<Integer> element) {
		return element.value();
	}

	@Override
	public int getInt(int index) {
		int i = this.from + index;
		if(index >= this.length || index < 0) {
			throw new IndexOutOfBoundsException(index);
		}
		return i;
	}

	@Override
	public int indexOf(int k) {
		int index = k - this.from;
		if(index >= this.length || k < this.from) {
			return -1;
		}
		return index;
	}

	@Override
	public int size() {
		return this.length;
	}

	public int getFrom() {
		return this.from;
	}

	public int getLength() {
		return this.length;
	}

	public int getDefaultValue() {
		return this.defaultValue;
	}
}
