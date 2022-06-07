package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.buffers.ACBOBuilder;
import net.devtech.jerraria.render.internal.buffers.BufferObjectBuilderAccess;

public class AtomicCounter extends V.UI<End> {
	final boolean canRead;

	protected AtomicCounter(GlData data, GlValue<?> next, String name, boolean read) {
		super(data, next, name);
		this.canRead = read;
	}

	/**
	 * This may be unreliable with multiple elements, as soon as you call this u
	 */
	public long read() {
		if(this.canRead) {
			return ((BufferObjectBuilderAccess) this.data.element(this.element)).uint();
		} else {
			throw new UnsupportedOperationException("Read operator not supported on " + this + "!");
		}
	}

	/**
	 * @param canRead {@link #read}
	 */
	public static Type<AtomicCounter> atomic_ui(String name, String groupName, boolean canRead) {
		return new Simple<>(
			(dat, nex, nam) -> new AtomicCounter(dat, nex, nam, canRead),
			DataType.ATOMIC_UINT,
			name,
			groupName,
			canRead
		);
	}

	public static Type<AtomicCounter> atomic_ui(String name, boolean needsRead) {
		return atomic_ui(name, null, needsRead);
	}
}
