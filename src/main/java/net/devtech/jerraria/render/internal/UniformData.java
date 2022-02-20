package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_DATA_SIZE;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_OFFSET;
import static org.lwjgl.opengl.GL31.glBindBuffer;
import static org.lwjgl.opengl.GL31.glBindBufferBase;
import static org.lwjgl.opengl.GL31.glGenBuffers;
import static org.lwjgl.opengl.GL31.glGetActiveUniformBlockiv;
import static org.lwjgl.opengl.GL31.glGetActiveUniformsiv;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glGetUniformIndices;
import static org.lwjgl.opengl.GL31.glGetUniformLocation;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class UniformData extends GlData {
	final Map<String, Element> elements;
	final List<ElementGroup> groups;
	final List<Uniform> uniforms;

	public UniformData(Map<String, BareShader.Field> fields, int program) {
		Map<String, ElementGroup> groups = new LinkedHashMap<>();
		ElementGroup defaultGroup = new ElementGroup();
		Map<String, Element> elements = new HashMap<>();
		List<Uniform> uniforms = new ArrayList<>();
		for(BareShader.Field value : fields.values()) {
			Element element;
			ElementGroup group;
			int location = glGetUniformLocation(program, value.name());
			if("default".equals(value.groupName(true))) {
				group = defaultGroup;
				element = new StandardUniform(value.name(), value.type(), location, uniforms.size());
				uniforms.add(Uniform.create(value.type(), location));
			} else {
				group = groups.computeIfAbsent(value.groupName(true), s -> new ElementGroup(s, program,
					groups.size()));
				int[] off = {0};
				int uniformIndex = glGetUniformIndices(program, value.name());
				glGetActiveUniformsiv(program, new int[] {uniformIndex}, GL_UNIFORM_OFFSET, off);
				DataType type = value.type();
				element = new VAO.Element(group.groupIndex, value.name(), type, location, off[0]);
			}
			group.elements.add(element);
			elements.put(value.name(), element);
		}

		this.groups = new ArrayList<>(groups.values());
		this.elements = elements;
		this.uniforms = uniforms;
		this.init_();
	}

	public UniformData(UniformData data, boolean preserveUniforms) {
		this.elements = data.elements;
		List<ElementGroup> list = new ArrayList<>();
		for(ElementGroup group : data.groups) {
			ElementGroup elementGroup = new ElementGroup(group, preserveUniforms);
			list.add(elementGroup);
		}
		this.groups = list;
		this.uniforms = data.uniforms.stream().map(u -> preserveUniforms ? Uniform.copy(u) : Uniform.createNew(u)).toList();
	}


	@Override
	public UniformData flush() {
		for(Uniform uniform : this.uniforms) {
			uniform.rebind = true;
			uniform.reset();
		}
		for(VAO.ElementGroup group : this.groups) {
			group.reupload = true;
			group.buffer.vertexCount = 0;
		}
		return this;
	}


	@Override
	public Buf element(Element element) {
		if(element instanceof StandardUniform s) {
			Uniform uniform = this.uniforms.get(s.uniformIndex);
			uniform.rebind = true;
			uniform.reset();
			return uniform;
		} else {
			VAO.Element e = (VAO.Element) element;
			ElementGroup group = this.groups.get(e.groupIndex());
			group.reupload = true;
			return group.buffer;
		}
	}

	@Override
	public Element getElement(String name) {
		return this.elements.get(name);
	}

	public UniformData bind(boolean forceReupload) {
		for(ElementGroup group : this.groups) {
			group.upload();
		}
		for(Uniform uniform : this.uniforms) {
			if(forceReupload) {
				uniform.rebind = true;
			}
			uniform.bind();
		}
		return this;
	}

	static class ElementGroup extends VAO.ElementGroup {
		final int uniformBlockIndex;
		final int[] uniformIndexes;
		final int groupIndex;

		public ElementGroup(String name, int program, int index) {
			super(name);
			this.groupIndex = index;
			this.uniformBlockIndex = glGetUniformBlockIndex(program, name);
			int[] count = {0};
			glGetActiveUniformBlockiv(program, this.uniformBlockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, count);
			this.uniformIndexes = new int[count[0]];
			glGetActiveUniformBlockiv(program,
				this.uniformBlockIndex,
				GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES,
				this.uniformIndexes
			);

			glGetActiveUniformBlockiv(program, this.uniformBlockIndex, GL_UNIFORM_BLOCK_DATA_SIZE, count);
			this.len = count[0];

			glUniformBlockBinding(program, this.uniformBlockIndex, this.groupIndex);
			this.bind();
			glBindBufferBase(GL_UNIFORM_BUFFER, this.groupIndex, this.glId);
			this.buffer = new BufferBuilder(this.len);
			this.buffer.vertexCount = 1;
		}

		public ElementGroup() {
			super("default");
			this.groupIndex = -1;
			this.uniformBlockIndex = -1;
			this.uniformIndexes = null;
		}

		public ElementGroup(ElementGroup group, boolean preserveUniforms) {
			super(group, preserveUniforms);
			this.groupIndex = group.groupIndex;
			this.uniformBlockIndex = group.uniformBlockIndex;
			this.uniformIndexes = group.uniformIndexes;
			this.glId = group.glId;
		}

		@Override
		void upload() {
			if(this.reupload) {
				this.bind();
				this.buffer.uniformCount();
				this.buffer.upload(true);
				this.reupload = false;
			}
		}

		@Override
		void bind() {
			if(this.glId == 0) {
				this.glId = glGenBuffers();
			}
			glBindBuffer(GL_UNIFORM_BUFFER, this.glId);
		}
	}

	public record StandardUniform(String name, DataType type, int location, int uniformIndex) implements Element {}
}
