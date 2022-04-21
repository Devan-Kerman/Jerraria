package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL31.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL31.glDrawArrays;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL30;

public class VAO extends GlData {
	final int glId;
	final Map<String, Element> elements;
	final List<ElementGroup> groups;
	final ElementGroup last;

	public VAO(Map<String, BareShader.Field> fields, int program) {
		Map<String, Element> elements = new HashMap<>();
		Map<String, ElementGroup> groups = new LinkedHashMap<>();
		ElementGroup last = null;
		for(BareShader.Field field : fields.values()) {
			int location = glGetAttribLocation(program, field.name());
			if(location == -1) {
				throw new IllegalArgumentException("Could not find field by name " + field.name());
			}

			last = groups.computeIfAbsent(field.groupName(false), ElementGroup::new);
			int groupIndex = new ArrayList<>(groups.values()).indexOf(last);
			var element = new Element(groupIndex, field.name(), field.type(), location, last.len);
			elements.put(field.name(), element);
			last.elements.add(element);
			last.len += element.type.byteCount;
		}

		this.last = last;
		this.elements = elements;
		this.groups = new ArrayList<>(groups.values());
		this.glId = bindVAO();

		for(ElementGroup group : groups.values()) {
			group.buffer = new BufferBuilder(group.len);
			group.bind();
			for(GlData.Element v : group.elements) {
				Element value = (Element) v;
				DataType type = value.type;
				GL30.glVertexAttribPointer(
					value.location,
					type.elementCount,
					type.elementType,
					type.normalized,
					type.byteCount,
					value.byteOffset
				);
				GL30.glEnableVertexAttribArray(value.location);
			}
		}
		this.init_();
	}

	public VAO(VAO vao, boolean copyContents) {
		this.glId = vao.glId;
		List<ElementGroup> groups = new ArrayList<>();
		ElementGroup last = null;
		for(ElementGroup group : vao.groups) {
			groups.add(last = new ElementGroup(group, copyContents));
		}
		this.groups = groups;
		this.elements = vao.elements;
		this.last = last;
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
		GL30.glBindVertexArray(this.glId);
		return this;
	}

	public void bindAndDraw(int mode, boolean forceReupload) {
		this.bind();
		this.drawArray(mode, forceReupload);
	}

	public void drawArray(int mode, boolean forceReupload) {
		for(ElementGroup group : this.groups) {
			if(forceReupload) {
				group.reupload = true;
			}
			group.upload();
		}
		glDrawArrays(mode, 0, this.last.buffer.vertexCount);
	}

	static int bindVAO() {
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		return vao;
	}

	static class ElementGroup {
		final List<GlData.Element> elements;
		final String name;
		BufferBuilder buffer;
		boolean reupload;
		int glId;
		int len;

		public ElementGroup(String name) {
			this.name = name;
			this.elements = new ArrayList<>();
		}

		public ElementGroup(ElementGroup group, boolean copyContents) {
			this.name = group.name;
			this.elements = group.elements;
			this.len = group.len;
			if(copyContents) {
				this.buffer = new BufferBuilder(group.buffer);
			} else {
				this.buffer = new BufferBuilder(group.len);
			}
			this.glId = group.glId;
		}

		void upload() {
			if(this.reupload) {
				this.bind();
				this.buffer.upload(false);
				this.reupload = false;
			}
		}

		void bind() {
			if(this.glId == 0) {
				this.glId = GL30.glGenBuffers();
			}
			GL30.glBindBuffer(GL_ARRAY_BUFFER, this.glId);
		}
	}

	public record Element(int groupIndex, String name, DataType type, int location, int byteOffset)
		implements GlData.Element {}
}
