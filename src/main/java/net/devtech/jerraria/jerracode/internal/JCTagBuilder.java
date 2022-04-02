package net.devtech.jerraria.jerracode.internal;

import java.util.HashMap;
import java.util.Map;

import net.devtech.jerraria.jerracode.JCTagView;
import net.devtech.jerraria.jerracode.JCType;
import net.devtech.jerraria.jerracode.NativeJCType;

public class JCTagBuilder extends JCTagViewImpl implements JCTagView.Builder {
	public JCTagBuilder() {super(new HashMap<>());}

	@Override
	public Builder putInt(String key, int value) {
		var entry = new Entry<>(NativeJCType.INT, value);
		this.entries.put(key, entry);
		return this;
	}

	@Override
	public <T> Builder put(String key, JCType<T, ?> type, T value) {
		var entry = new Entry<>(type, value);
		this.entries.put(key, entry);
		return this;
	}

	@Override
	public JCTagView build() {
		return new JCTagViewImpl(Map.copyOf(this.entries));
	}

	@Override
	public void forceImmutable() {
		this.entries = Map.copyOf(this.entries);
	}
}
