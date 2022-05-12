package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.devtech.jerraria.util.Id;
import org.lwjgl.opengl.GL20;

public class VAO extends GlData {
	static final IntArrayList RECLAIM_VAO_IDS = new IntArrayList(), RECLAIM_VBO_IDS = new IntArrayList();
	static class ShaderVAOState {
		/**
		 * The id of the VAO currently bound to the shader
		 */
		int currentlyBoundId;

		public void bind(int id, boolean force) {
			int current = this.currentlyBoundId;
			if(current != id || force) {
				glBindVertexArray(id);
				this.currentlyBoundId = id;
			}
		}
	}

	static class VAOReference {int vaoGlId; boolean initialized;}

	final ShaderVAOState manager;
	final VAOReference reference;
	final Map<String, Element> elements;
	final List<ElementGroup> groups;
	final ElementGroup last;

	public VAO(Map<String, BareShader.Field> fields, int program, Id id) {
		Map<String, Element> elements = new HashMap<>();
		Map<String, ElementGroup> groups = new LinkedHashMap<>();
		ElementGroup last = null;
		for(BareShader.Field field : fields.values()) {
			int location = glGetAttribLocation(program, field.name());
			if(location == -1) {
				throw new IllegalArgumentException("Could not find field by name " + field.name() + " in " + id);
			}

			last = groups.computeIfAbsent(field.groupName(false), ElementGroup::new);
			int groupIndex = new ArrayList<>(groups.values()).indexOf(last);
			var element = new Element(groupIndex, field.name(), field.type(), location, last.byteLength);
			elements.put(field.name(), element);
			last.elements.add(element);
			last.byteLength += element.type.byteCount;
		}

		this.last = last;
		this.elements = elements;
		this.groups = List.copyOf(groups.values());
		this.manager = new ShaderVAOState();
		this.reference = new VAOReference();
		this.reference.vaoGlId = genVAO(this.groups, true);
	}

	public VAO(VAO vao, boolean copyContents) {
		List<ElementGroup> groups = new ArrayList<>();
		ElementGroup last = null;
		for(ElementGroup group : vao.groups) {
			groups.add(last = new ElementGroup(group, copyContents));
		}
		this.groups = groups;
		this.elements = vao.elements;
		this.last = last;
		this.manager = vao.manager;

		synchronized(RECLAIM_VAO_IDS) {
			if(!RECLAIM_VAO_IDS.isEmpty()) {
				glDeleteVertexArrays(RECLAIM_VAO_IDS.toIntArray());
				RECLAIM_VAO_IDS.clear();
			}
			if(!RECLAIM_VBO_IDS.isEmpty()) {
				glDeleteBuffers(RECLAIM_VBO_IDS.toIntArray());
				RECLAIM_VBO_IDS.clear();
			}
		}

		VAOReference reference = this.reference = new VAOReference();
		BareShader.GL_CLEANUP.register(this, () -> {
			synchronized(RECLAIM_VAO_IDS) {
				if(reference.initialized) {
					RECLAIM_VAO_IDS.add(reference.vaoGlId);
				}
				for(ElementGroup group : groups) {
					if(group.validGlId) {
						RECLAIM_VBO_IDS.add(group.glId);
					}
				}
			}
		});
	}

	@Override
	public VAO flush() {
		for(ElementGroup group : this.groups) {
			group.buffer.vertexCount = 0;
		}
		return this;
	}

	@Override
	public Buf element(GlData.Element elem) {
		Element element = (Element) elem;
		BufferBuilder buffer = this.groups.get(element.groupIndex).buffer;
		ByteBuffer byteBuf = buffer.buffer;
		byteBuf.position(buffer.vertexOffset() + element.byteOffset);
		return buffer;
	}

	@Override
	public GlData.Element getElement(String name) {
		return this.elements.get(name);
	}

	public VAO next() {
		for(ElementGroup group : this.groups) {
			group.buffer.next();
			group.reupload = true;
		}
		return this;
	}

	public VAO bind() {
		int id = this.reference.vaoGlId;
		if(!this.reference.initialized) {
			id = this.reference.vaoGlId = genVAO(this.groups, false);
		}
		this.manager.bind(id, false);
		return this;
	}

	public void bindAndDraw(int mode) {
		this.bind();
		this.updateGroups();
		glDrawArrays(mode, 0, this.last.buffer.vertexCount);
	}

	public void bindAndDrawInstanced(int mode, int count) {
		this.bind();
		this.updateGroups();
		glDrawArraysInstanced(mode, 0, this.last.buffer.vertexCount, count);
	}

	private static int genVAO(Collection<ElementGroup> groups, boolean initialize) {
		final int glId = bindVAO();
		for(ElementGroup group : groups) {
			if(initialize) {
				group.buffer = new BufferBuilder(group.byteLength);
			}
			group.bind();
			for(GlData.Element v : group.elements) {
				Element value = (Element) v;
				DataType type = value.type;
				GL20.glVertexAttribPointer(
					value.location,
					type.elementCount,
					type.elementType,
					type.normalized,
					type.byteCount,
					value.byteOffset
				);
				glEnableVertexAttribArray(value.location);
			}
		}
		return glId;
	}

	static int bindVAO() {
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		return vao;
	}

	private void updateGroups() {
		for(ElementGroup group : this.groups) {
			group.upload();
		}
	}

	static class ElementGroup {
		final List<GlData.Element> elements;
		final String name;
		BufferBuilder buffer;
		boolean reupload;
		int glId;
		boolean validGlId;
		int byteLength;

		public ElementGroup(String name) {
			this.name = name;
			this.elements = new ArrayList<>();
			this.glId = this.initGlId();
			this.validGlId = true;
		}

		public ElementGroup(ElementGroup group, boolean copyContents) {
			this.name = group.name;
			this.elements = group.elements;
			this.byteLength = group.byteLength;
			if(copyContents) {
				this.buffer = new BufferBuilder(group.buffer);
				this.reupload = true;
			} else {
				this.buffer = new BufferBuilder(group.byteLength);
			}
		}

		void upload() {
			if(this.reupload) {
				this.bind();
				this.buffer.upload(false);
				this.reupload = false;
			}
		}

		void bind() {
			if(!this.validGlId) {
				this.glId = this.initGlId();
				this.validGlId = true;
			}
			glBindBuffer(GL_ARRAY_BUFFER, this.glId);
		}

		protected int initGlId() {
			return glGenBuffers();
		}
	}

	public record Element(int groupIndex, String name, DataType type, int location, int byteOffset)
		implements GlData.Element {}
}
