package net.devtech.jerraria.util.data.internal;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.data.JCType;
import net.devtech.jerraria.util.data.NativeJCType;

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
