package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.List;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.state.GLContextState;
import org.lwjgl.opengl.GL20;

/**
 * Since it cannot be guaranteed that a VAO is created on render thread, we need to lazily initialize it. And we use an
 * object for garbage collection reasons.
 */
class LazyVAOReference {
	int vaoGlId;

	public LazyVAOReference() {
	}

	public void bind(List<VertexBufferObject> groups) {
		int id = this.vaoGlId;
		boolean gen = this.vaoGlId == 0;
		if(gen) {
			id = this.vaoGlId = glGenVertexArrays();
		}

		GLContextState.bindVAO(id);
		for(VertexBufferObject group : groups) {
			boolean needsRebind = group.bind();
			if(needsRebind || gen) {
				for(GlData.Element v : group.elements) {
					ElementImpl value = (ElementImpl) v;
					DataType type = value.type();
					GL20.glVertexAttribPointer(
						value.location(),
						type.elementCount,
						type.elementType,
						type.normalized,
						group.byteLength,
						group.getOffset(value.offsetIndex())
					);
				}
			}
		}

		if(gen) {
			for(VertexBufferObject group : groups) {
				for(GlData.Element element : group.elements) {
					glEnableVertexAttribArray(((ElementImpl) element).location());
				}
			}
		}
	}

	public void close() {
		if(this.vaoGlId != 0) {
			glDeleteVertexArrays(this.vaoGlId);
			GLContextState.untrackVAOIfBound(this.vaoGlId);
		}
	}
}
