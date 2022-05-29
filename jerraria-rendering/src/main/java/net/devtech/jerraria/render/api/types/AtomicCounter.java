package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.buffers.UniformBufferBuilder;
import net.devtech.jerraria.render.internal.buffers.VertexBufferObjectBuilder;

public class AtomicCounter extends V.UI<End> { // todo get atomic int value
	protected AtomicCounter(GlData data, GlValue<?> next, String name) {
		super(data, next, name);
	}

	/**
	 * This may be unreliable with multiple elements
	 */
	public long read() {
		return ((UniformBufferBuilder)this.data.element(this.element)).readAtomicCounter();
	}

	public static Type<AtomicCounter> atomic_ui(String name, String groupName) {
		return simple(AtomicCounter::new, DataType.ATOMIC_UINT, name, groupName);
	}

	public static Type<AtomicCounter> atomic_ui(String name) {
		return atomic_ui(name, null);
	}
}
