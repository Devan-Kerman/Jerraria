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
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.internal.buffers.VBOBuilder;
import net.devtech.jerraria.util.Validate;

public class VertexData extends GlData {
	final LazyVAOReference reference;
	public final Map<String, ElementImpl> elements;
	final List<VertexBufferObject> groups;
	final VertexBufferObject last;

	public VertexData(Map<String, BareShader.Field> fields, int program) {
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
		record ProtoVertexBufferObject(String name, IntList offsets, int[] len, List<Element> elements) {
			public ProtoVertexBufferObject(String name) {
				this(name, new IntArrayList(), new int[] {0}, new ArrayList<>());
			}
		}

		Map<String, ProtoVertexBufferObject> vbos = new LinkedHashMap<>();
		for(ActiveField value : fieldsByName.values()) {
			String name = value.name;
			BareShader.Field field = fields.get(name);
			if(!field.type().isCompatible(value.type)) {
				Set<DataType> types = DataType.forGlslType(value.type);
				List<String> glslNames = types.stream().map(DataType::toString).toList();
				throw new UnsupportedOperationException(field.type() + " is not valid for type for \"" + glslNames + " " + name + "\" " +
				                                        "suggested types: " + types);
			}

			ProtoVertexBufferObject last = vbos.computeIfAbsent(field.groupName(false), ProtoVertexBufferObject::new);
			int groupIndex = new ArrayList<>(vbos.values()).indexOf(last);
			var element = new ElementImpl(groupIndex, name, field.type(), value.location, last.offsets.size(), -1, false);
			last.offsets.add(last.len[0]);
			last.len[0] += element.type().byteCount;
			elements.put(name, element);
			last.elements.add(element);
		}

		this.groups = vbos.values().stream().map(p -> new VertexBufferObject(p.name, p.offsets, p.len[0], p.elements)).toList();
		this.last = this.groups.get(0);
		this.elements = elements;
		this.reference = new LazyVAOReference();
	}

	public VertexData(VertexData vao, boolean copyContents) {
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

	public void flush() {
		this.validate();
		for(VertexBufferObject group : this.groups) {
			group.getBuilder().reset();
		}
	}

	@Override
	public Buf element(GlData.Element elem) {
		this.validate();
		ElementImpl element = (ElementImpl) elem;
		VBOBuilder buffer = this.groups.get(element.groupIndex()).getBuilder();
		buffer.offset(element.offsetIndex());
		return buffer;
	}

	@Override
	public GlData.Element getElement(String name) {
		this.validate();
		return this.elements.get(name);
	}

	public VertexData vert() {
		this.validate();
		for(VertexBufferObject group : this.groups) {
			group.getBuilder().vert();
		}
		return this;
	}

	public interface PositionDataModifier {
		void modifier(ByteBuffer input, Buf buf);
	}

	public int modifyPosData(int fromVertex, int toVertex, GlData.Element elem, PositionDataModifier fixer) {
		ElementImpl element = (ElementImpl) elem;
		VBOBuilder buffer = this.groups.get(element.groupIndex()).getBuilder();
		if(toVertex == -65) {
			toVertex = buffer.getVertexCount();
		}
		buffer.assertElementRange(fromVertex, toVertex);
		int start = buffer.getVertexCount();
		try {
			for(int vertex = fromVertex; vertex < toVertex; vertex++) {
				buffer.struct(vertex);
				ByteBuffer offset = buffer.offset(element.offsetIndex());
				ByteBuffer slice = offset.slice(offset.position(), element.type().byteCount);
				fixer.modifier(slice, buffer);
			}
		} finally {
			buffer.struct(start);
		}
		return toVertex;
	}

	public VertexData bind() {
		this.validate();
		this.reference.bind(this.groups);
		return this;
	}

	public void drawArrays(int mode) {
		this.validate();
		glDrawArrays(mode, 0, this.last.getBuilder().getVertexCount());
	}

	public void drawArraysInstanced(int mode, int count) {
		this.validate();
		glDrawArraysInstanced(mode, 0, this.last.getBuilder().getVertexCount(), count);
	}

	public void drawElements(int mode, int elements, int type) {
		this.validate();
		glDrawElements(mode, elements, type,0L);
	}

	public void drawElementsInstanced(int mode, int elements, int type, int count) {
		this.validate();
		glDrawElementsInstanced(mode, elements, type, 0L, count);
	}

	@Override
	public void invalidate() {
		this.reference.close();
		for(VertexBufferObject group : this.groups) {
			group.close();
		}
	}

	public void bake() {
		this.validate();
		for(VertexBufferObject group : this.groups) {
			group.getBuilder().bake();
		}
	}
}
