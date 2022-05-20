package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL20;

public class VAO extends GlData {
	static final IntArrayList RECLAIM_VAO_IDS = new IntArrayList(), RECLAIM_VBO_IDS = new IntArrayList();

	static class VAOReference {int vaoGlId;}

	final VAOReference reference;
	final Map<String, Element> elements;
	final List<ElementGroup> groups;
	final ElementGroup last;

	public VAO(Map<String, BareShader.Field> fields, int program, Id id) {
		IntBuffer aBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		IntBuffer bBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		record ActiveField(String name, int location, int type) {}
		Map<String, ActiveField> fieldsByName = new LinkedHashMap<>();
		glGetProgramiv(program, GL_ACTIVE_ATTRIBUTES, aBuf);
		int activeAttributes = aBuf.get(0);
		for(int i = 0; i < activeAttributes; i++) {
			String name = glGetActiveAttrib(program, i, aBuf, bBuf);
			if(name.startsWith("gl_")) { // ignore builtins
				continue;
			}
			int location = glGetAttribLocation(program, name);
			if(location == -1) {
				throw new IllegalStateException("Unable to find location of " + name + " in shader!");
			}
			int size = aBuf.get(0), type = bBuf.get(0);
			if(size > 1) { // add arrays
				String baseName;
				if(name.charAt(name.length()-1) == ']') {
					baseName = name.substring(0, Validate.greaterThanEqualTo(name.indexOf('['), 0, "Weird array uniform name: " + name));
				} else {
					baseName = name;
				}
				for(int index = 0; index < size; index++) {
					int attribLocation = glGetAttribLocation(program, name);
					String indexName = String.format("%s[%d]", baseName, index);
					fieldsByName.put(indexName, new ActiveField(indexName, attribLocation, type));
				}
			} else {
				fieldsByName.put(name, new ActiveField(name, location, type));
			}
		}

		Set<String> unrefAttrib = new LinkedHashSet<>(fieldsByName.keySet());
		unrefAttrib.removeAll(fields.keySet());
		Set<String> unresAttrib = new LinkedHashSet<>(fields.keySet());
		unresAttrib.removeAll(fieldsByName.keySet());
		if(!unresAttrib.isEmpty() || !unrefAttrib.isEmpty()) {
			throw new IllegalStateException("Vertex Attribute(s) with name(s) " + unrefAttrib + " were not referenced! Vertex Attribute(s) with name(s) " + unresAttrib + " were not found! (may have been optimized out)");
		}

		Map<String, Element> elements = new HashMap<>();
		Map<String, ElementGroup> groups = new LinkedHashMap<>();
		ElementGroup last = null;
		for(ActiveField value : fieldsByName.values()) {
			String name = value.name;
			BareShader.Field field = fields.get(name);
			if(!field.type().isCompatible(value.type)) {
				Set<DataType> types = DataType.forGlslType(value.type);
				List<String> glslNames = types.stream().map(DataType::toString).toList();
				throw new UnsupportedOperationException(field.type() + " is not valid for type for \"" + glslNames + " " + name + "\" " +
				                                        "suggested types: " + types);
			}
			last = groups.computeIfAbsent(field.groupName(false), ElementGroup::new);
			int groupIndex = new ArrayList<>(groups.values()).indexOf(last);
			var element = new Element(groupIndex, name, field.type(), value.location, last.byteLength);
			elements.put(name, element);
			last.elements.add(element);
			last.byteLength += element.type.byteCount;
		}

		this.groups = List.copyOf(groups.values());
		this.last = last;
		this.elements = elements;
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
				if(reference.vaoGlId != 0) {
					RECLAIM_VAO_IDS.add(reference.vaoGlId);
				}
				for(ElementGroup group : groups) {
					if(group.glId != 0) {
						RECLAIM_VBO_IDS.add(group.glId);
					}
				}
			}
		});
	}

	public void copy(int id) {
		for(ElementGroup group : this.groups) {
			group.buffer.copyVertex(group.buffer, id);
		}
	}

	public void copy(VAO vao, int id) {
		if(vao.groups.size() != this.groups.size()) {
			throw new UnsupportedOperationException("Vertex Copying Not Supported Between These 2 VAOs!");
		}
		for(int i = 0; i < this.groups.size(); i++) {
			ElementGroup from = this.groups.get(i), to = vao.groups.get(i);
			if(from.byteLength != to.byteLength) {
				throw new UnsupportedOperationException("Vertex Copying Not Supported Between These 2 VAOs!");
			}
			from.buffer.copyVertex(to.buffer, id);
		}
	}

	public VAO flush() {
		for(ElementGroup group : this.groups) {
			group.buffer.vertexCount = 0;
			group.buffer.buffer.position(0);
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

	public VAO markForReupload() {
		for(ElementGroup group : this.groups) {
			group.reupload = true;
		}
		return this;
	}

	public VAO next() {
		for(ElementGroup group : this.groups) {
			group.buffer.next();
			group.reupload = true;
		}
		return this;
	}

	private VAO bind0() {
		int id = this.reference.vaoGlId;
		if(this.reference.vaoGlId == 0) {
			id = this.reference.vaoGlId = genVAO(this.groups, false);
		}
		glBindVertexArray(id);
		return this;
	}

	public VAO bind() {
		this.bind0();
		this.updateGroups();
		return this;
	}

	public void drawArrays(int mode) {
		glDrawArrays(mode, 0, this.last.buffer.vertexCount);
	}

	public void drawArraysInstanced(int mode, int count) {
		glDrawArraysInstanced(mode, 0, this.last.buffer.vertexCount, count);
	}

	public void drawElements(int mode, int elements, int type) {
		glDrawElements(mode, elements, type,0L);
	}

	public void drawElementsInstanced(int mode, int elements, int type, int count) {
		glDrawElementsInstanced(mode, elements, type, 0L, count);
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
					group.byteLength,
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
		int byteLength;

		public ElementGroup(String name) {
			this.name = name;
			this.elements = new ArrayList<>();
			this.glId = this.initGlId();
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
				this.buffer.upload(GL_ARRAY_BUFFER);
				this.reupload = false;
			}
		}

		void bind() {
			if(this.glId == 0) {
				this.glId = this.initGlId();
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
