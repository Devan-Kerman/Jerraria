package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
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

public class VAO extends GlData {
	static final IntArrayList RECLAIM_VBO_IDS = new IntArrayList();

	final LazyVAOReference reference;
	final Map<String, ElementImpl> elements;
	final List<VertexBufferObject> groups;
	final VertexBufferObject last;

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

		Map<String, ElementImpl> elements = new HashMap<>();
		Map<String, VertexBufferObject> vbos = new LinkedHashMap<>();
		VertexBufferObject last = null;
		for(ActiveField value : fieldsByName.values()) {
			String name = value.name;
			BareShader.Field field = fields.get(name);
			if(!field.type().isCompatible(value.type)) {
				Set<DataType> types = DataType.forGlslType(value.type);
				List<String> glslNames = types.stream().map(DataType::toString).toList();
				throw new UnsupportedOperationException(field.type() + " is not valid for type for \"" + glslNames + " " + name + "\" " +
				                                        "suggested types: " + types);
			}
			last = vbos.computeIfAbsent(field.groupName(false), VertexBufferObject::new);
			int groupIndex = new ArrayList<>(vbos.values()).indexOf(last);
			var element = new ElementImpl(groupIndex, name, field.type(), value.location, last.byteLength);
			elements.put(name, element);
			last.elements.add(element);
			last.byteLength += element.type().byteCount;
		}

		this.groups = List.copyOf(vbos.values());
		this.last = last;
		this.elements = elements;
		this.reference = new LazyVAOReference();
		this.reference.bind(this.groups);
	}

	public VAO(VAO vao, boolean copyContents) {
		List<VertexBufferObject> groups = new ArrayList<>();
		VertexBufferObject last = null;
		for(VertexBufferObject group : vao.groups) {
			groups.add(last = new VertexBufferObject(group, copyContents));
		}
		this.groups = groups;
		this.elements = vao.elements;
		this.last = last;

		this.reference = new LazyVAOReference();
	}

	public VAO flush() {
		for(VertexBufferObject group : this.groups) {
			group.getBuilder().reset();
		}
		return this;
	}

	@Override
	public Buf element(GlData.Element elem) {
		ElementImpl element = (ElementImpl) elem;
		BufferObjectBuilder buffer = this.groups.get(element.groupIndex()).getBuilder();
		buffer.offset(element.byteOffset());
		return buffer;
	}

	@Override
	public GlData.Element getElement(String name) {
		return this.elements.get(name);
	}

	public VAO next() {
		for(VertexBufferObject group : this.groups) {
			group.getBuilder().next();
		}
		return this;
	}

	public VAO bind() {
		this.reference.bind(this.groups);
		return this;
	}

	public void drawArrays(int mode) {
		glDrawArrays(mode, 0, this.last.getBuilder().totalCount());
	}

	public void drawArraysInstanced(int mode, int count) {
		glDrawArraysInstanced(mode, 0, this.last.getBuilder().totalCount(), count);
	}

	public void drawElements(int mode, int elements, int type) {
		glDrawElements(mode, elements, type,0L);
	}

	public void drawElementsInstanced(int mode, int elements, int type, int count) {
		glDrawElementsInstanced(mode, elements, type, 0L, count);
	}
}
