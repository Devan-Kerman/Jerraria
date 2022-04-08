package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;

public abstract class GlValue<N extends GlValue<?>> {
	protected final GlData data;
	final N next;

	protected GlValue(GlData data, GlValue next) {
		this.data = data;
		this.next = (N) next;
	}

	protected N getNext() {
		// todo more validation
		return this.next;
	}

	public enum Loc {
		UNIFORM,
		ATTRIBUTE
	}

	public interface Type<N extends GlValue<?>> {
		N create(GlData data, GlValue<?> next);

		void attach(BareShader.Uncompiled uncompiled, Loc isUniform);
	}

	public interface SimpleType<N extends GlValue<?>> {
		N create(GlData data, GlValue<?> next);
	}

	public interface Attribute {
	}

	protected static <N extends GlValue<?>> Simple<N> simple(SimpleType<N> type, DataType dataType, String name) {
		return new Simple<>(type, dataType, name);
	}

	public record Simple<N extends GlValue<?>>(SimpleType<N> type, DataType dataType, String name) implements Type<N> {
		@Override
		public N create(GlData data, GlValue<?> next) {
			return type.create(data, next);
		}

		@Override
		public void attach(BareShader.Uncompiled uncompiled, Loc isUniform) {
			uncompiled.type(isUniform, this.dataType, this.name);
		}
	}
}
