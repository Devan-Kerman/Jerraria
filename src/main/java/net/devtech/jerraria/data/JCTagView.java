package net.devtech.jerraria.data;

import java.util.List;
import java.util.Set;

import net.devtech.jerraria.data.internal.JCTagBuilder;
import org.jetbrains.annotations.Nullable;

public interface JCTagView {
	static Builder builder() {
		return new JCTagBuilder();
	}

	Set<String> getKeys();

	void forEach(ValuesConsumer consumer);

	interface ValuesConsumer {
		<T, N> void accept(String key, JCType<T, N> type, T value) throws Throwable;
	}

	int getInt(String key, int defaultValue);

	<T> T get(String key, JCType<T, ?> type, T defaultValue);

	default JCTagView getView(String key) {
		return this.get(key, NativeJCType.TAG);
	}

	default int getInt(String key) {
		return this.getInt(key, 0);
	}

	@Nullable
	default <T> T get(String key, JCType<T, ?> type) {
		return this.get(key, type, null);
	}

	interface Builder extends JCTagView {
		Builder putInt(String key, int value);

		<T> Builder put(String key, JCType<T, ?> type, T value);

		default Builder put(String key, JCTagView view) {
			return this.put(key, NativeJCType.TAG, view);
		}

		JCTagView build();
	}
}
