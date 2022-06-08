package net.devtech.jerraria.render.api;

import java.util.Objects;

import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.internal.ElementImpl;
import net.devtech.jerraria.render.internal.UniformData;

/**
 * A render api attribute who acts as a direct analog to a gl attribute. For example in the following shader we have a
 * vertex attribute named "pos".
 * <pre>{@code
 *  in vec3 pos;
 *  in vec3 color;
 *
 *  out vec3 vertexColor;
 *  void main() {
 *      gl_Position = vec4(pos, 1.0);
 *      vertexColor = color;
 *  }
 *  }</pre>
 * In the rendering api we may have an {@link AbstractGlValue} with name "pos" that corresponds to that vertex
 * attribute.
 *
 * @see Vec3.F
 */
public abstract class AbstractGlValue<N extends GlValue<?>> extends GlValue<N> implements GlValue.Copiable, GlValue.Indexable {
	protected final String name;
	protected final GlData.Element element;

	protected AbstractGlValue(GlData data, GlValue next, String name) {
		super(data, next);
		this.name = name;
		this.element = Objects.requireNonNull(data.getElement(name), name + " not found!");
	}

	@Override
	public void copyTo(GlValue value) {
		AbstractGlValue dst = (AbstractGlValue) value;
		UniformData from = (UniformData) this.data.getSelf(), to = (UniformData) value.data.getSelf();
		from.copyTo(this.element, to, dst.element);
	}

	@Override
	public int getIndex() {
		if(this.element.getSelf() instanceof ElementImpl i) {
			return i.arrayIndex();
		}
		return -2;
	}
}
