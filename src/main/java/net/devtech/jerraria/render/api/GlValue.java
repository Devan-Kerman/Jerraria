package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;

/**
 * A GlValue provides a java-friendly interface to a vertex attribute or uniform. <br>
 * Oftentimes GlValues correspond 1:1 with a vertex attribute or uniform as in the case of {@link AbstractGlValue}. <br>
 * However, the api is intentionally left open-ended to allow for more advanced GlValues.
 * For example if you have a struct in your shader, you can make an object in java that corresponds to that struct, and make
 *  a GlValue that takes in that object and uses the element api to "serialize" it in a form the api can understand.<br>
 * <br>
 * <b>If a {@link GlValue} can be used as a uniform it must implement {@link GlValue.Uniform}. <br>
 * If a {@link GlValue} can be used as a vertex attribute it must implement {@link GlValue.Attribute}. <br></b>
 * @implNote To implement the api, check out the subclasses for examples.
 * @see AbstractGlValue
 * @see Vec3.F
 */
public abstract class GlValue<N extends GlValue<?>> { // todo .fillDefaults()
	protected final GlData data;
	final N next;

	/**
	 * @param data A universal interface for uploading vertex attributes and uniforms
	 * @param next {@link #getNext()}
	 */
	protected GlValue(GlData data, GlValue next) {
		this.data = data;
		this.next = (N) next;
	}

	/**
	 * @return the next GlValue in the chain or {@link End} if this is a uniform or the last vertex attribute.
	 */
	protected N getNext() {
		return this.next;
	}

	/**
	 * @see #simple(SimpleType, DataType, String, String)
	 */
	protected static <N extends AbstractGlValue<?>> Simple<N> simple(SimpleType<N> type, DataType dataType, String name) {
		return new Simple<>(type, dataType, name, null);
	}

	/**
	 * @param type A GlValueFactory, this should call your constructor
	 * @param dataType the type of this AbstractGlValue, this is the glsl type + normalized variants and the like.
	 * @param name the full path of the uniform or vertex attribute in the shader
	 * @param groupName
	 *  <p>&#09;If this is used as a vertex attribute, the group name simply identifies the buffer the data of this attribute is stored in.
	 *  in {@link #simple(SimpleType, DataType, String)} the groupName is null, which states all vertex attributes will share the same
	 *  buffer.</p> <br>
	 *  <p>&#09;If this is used as a uniform, the group name identifies the
	 *      <a href=https://www.khronos.org/opengl/wiki/Uniform_Buffer_Object>Uniform Block</a> that the uniform is in.
	 *      Like vertex attributes, each groupName has its own buffer. </p>
	 */
	protected static <N extends AbstractGlValue<?>> Simple<N> simple(SimpleType<N> type, DataType dataType, String name, String groupName) {
		return new Simple<>(type, dataType, name, groupName);
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
		N create(GlData data, GlValue<?> next, String name);
	}

	public interface Attribute {
	}

	public interface Uniform {
	}

	public record Simple<N extends GlValue<?>>(SimpleType<N> type, DataType dataType, String name, String groupName) implements Type<N> {
		@Override
		public N create(GlData data, GlValue<?> next) {
			return type.create(data, next, this.name);
		}

		@Override
		public void attach(BareShader.Uncompiled uncompiled, Loc isUniform) {
			String groupName = this.groupName;
			if(this.groupName == null && isUniform == Loc.UNIFORM) {
				groupName = "default";
			}
			uncompiled.type(isUniform, this.dataType, this.name, groupName);
		}
	}
}
