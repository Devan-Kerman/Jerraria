package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.state.GLContextState;
import org.lwjgl.opengl.GL20;

/**
 * Since it cannot be guaranteed that a VAO is created on render thread, we need to lazily initialize it. And we use an
 * object for garbage collection reasons.
 */
class LazyVAOReference {
	static final IntArrayList RECLAIM_VAO_IDS = new IntArrayList();
	int vaoGlId;

	public LazyVAOReference() {
	}

	public void bind(List<VertexBufferObject> groups) {
		int id = this.vaoGlId;
		boolean gen = this.vaoGlId == 0;
		if(gen) {
			reclaimVAO();
			id = this.vaoGlId = glGenVertexArrays();
			int finalId = id;
			BareShader.GL_CLEANUP.register(this, () -> {
				synchronized(RECLAIM_VAO_IDS) {
					RECLAIM_VAO_IDS.add(finalId);
				}
			});
		}

		GLContextState.bindVAO(id);
		for(VertexBufferObject group : groups) {
			if(group.bindAndUpload()) {
				for(GlData.Element v : group.elements) {
					ElementImpl value = (ElementImpl) v;
					DataType type = value.type();
					GL20.glVertexAttribPointer(
						value.location(),
						type.elementCount,
						type.elementType,
						type.normalized,
						group.byteLength,
						value.byteOffset()
					);
					//glEnableVertexAttribArray(value.location());
				}
			}
		}

		if(gen) {
			for(VertexBufferObject group : groups) {
				for(GlData.Element element : group.elements) {
					glEnableVertexAttribArray(((ElementImpl)element).location());
				}
			}
		}
	}

	public static void reclaimVAO() {
		synchronized(RECLAIM_VAO_IDS) {
			if(!RECLAIM_VAO_IDS.isEmpty()) {
				int[] arrays = RECLAIM_VAO_IDS.toIntArray();
				GLContextState.untrackVAOIfBound(arrays);
				glDeleteVertexArrays(arrays);
				RECLAIM_VAO_IDS.clear();
			}
		}
	}
}
