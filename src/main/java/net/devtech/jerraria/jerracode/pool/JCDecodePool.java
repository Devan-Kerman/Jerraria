package net.devtech.jerraria.jerracode.pool;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.AbstractJCElementImpl;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.jerracode.JCIO;

public class JCDecodePool {
	final List<JCElement<?>> elements = new ArrayList<>();

	public JCElement<?> getElement(int poolId) {
		return this.elements.get(poolId);
	}

	public void read(DataInput input) throws IOException {
		int elements = input.readInt();
		for(int i = 0; i < elements; i++) {
			this.elements.add(new UnreadJCElement<>());
		}

		for(int i = 0; i < elements; i++) {
			int index = input.readInt();
			JCElement read = JCIO.read(this, input);
			UnreadJCElement element = (UnreadJCElement<?>) this.elements.get(index);
			element.type = read.type();
			element.value = read.value();
		}
	}

	static final class UnreadJCElement<T> extends AbstractJCElementImpl<T> {
		NativeJCType<T> type;
		T value;

		@Override
		public NativeJCType<T> type() {
			return Objects.requireNonNull(this.type, "element has not been read yet!");
		}

		@Override
		public T value() {
			return Objects.requireNonNull(this.value, "element has not been read yet!");
		}
	}
}
