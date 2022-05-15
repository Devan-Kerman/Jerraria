package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL31.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_ARRAY_STRIDE;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BLOCK_DATA_SIZE;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_OFFSET;
import static org.lwjgl.opengl.GL31.glBindBufferBase;
import static org.lwjgl.opengl.GL31.glBindBufferRange;
import static org.lwjgl.opengl.GL31.glBufferData;
import static org.lwjgl.opengl.GL31.glCopyBufferSubData;
import static org.lwjgl.opengl.GL31.glDeleteBuffers;
import static org.lwjgl.opengl.GL31.glGenBuffers;
import static org.lwjgl.opengl.GL31.glGetActiveUniformBlockiv;
import static org.lwjgl.opengl.GL31.glGetActiveUniformsi;
import static org.lwjgl.opengl.GL31.glGetInteger;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glGetUniformIndices;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.math.JMath;


public class UniformData extends GlData {
	public static final int UBO_PADDING = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	final Map<String, Element> elements;
	final List<UniformBufferBlock> groups;
	final List<Uniform> uniforms;

	public UniformData(Map<String, BareShader.Field> fields, int program, Id id) {
		Map<String, UniformBufferBlock> groups = new LinkedHashMap<>();
		UniformBufferBlock defaultGroup = new UniformBufferBlock();
		Map<String, Element> elements = new HashMap<>();
		List<Uniform> uniforms = new ArrayList<>();
		int textureUnitCounter = 0;
		for(BareShader.Field value : fields.values()) {
			Element element;
			UniformBufferBlock group;
			if(value.type().isSampler) {
				int location = glGetUniformLocation(program, value.name());
				if(location == -1) {
					throw new IllegalStateException("Unable to find uniform " + value.name() + " in " + id);
				}
				group = defaultGroup;
				element = new StandardUniform(value.name(), value.type(), location, uniforms.size());
				uniforms.add(Uniform.createSampler(value.type(), location, textureUnitCounter++));
			} else if("default".equals(value.groupName(true))) {
				int location = glGetUniformLocation(program, value.name());
				if(location == -1) {
					throw new IllegalStateException("Unable to find uniform " + value.name() + " in " + id);
				}
				group = defaultGroup;
				element = new StandardUniform(value.name(), value.type(), location, uniforms.size());
				uniforms.add(Uniform.create(value.type(), location));
			} else {
				group = groups.computeIfAbsent(
					value.groupName(true),
					s -> new UniformBufferBlock(s, program, groups.size())
				);

				// for nested arrays or arrays of a vanilla type, we must start at the [0] index and calculate forward!
				int location, byteOffset;
				String name = value.name();
				int endIndex = name.length() - 1;
				if(name.charAt(endIndex) == ']') { // end-level array
					int open = name.lastIndexOf('[');
					String zero = name.substring(0, open) + "[0]";
					location = glGetUniformIndices(program, zero);
					int stride = glGetActiveUniformsi(program, location, GL_UNIFORM_ARRAY_STRIDE);
					int index = Integer.parseInt(name.substring(open + 1, endIndex));
					int offset = glGetActiveUniformsi(program, location, GL_UNIFORM_OFFSET);
					byteOffset = stride * index + offset;
				} else {
					location = glGetUniformIndices(program, name);
					byteOffset = glGetActiveUniformsi(program, location, GL_UNIFORM_OFFSET);
				}

				if(location == -1) {
					throw new IllegalStateException("Unable to find " + name + " in " + id);
				}

				DataType type = value.type();
				element = new VAO.Element(group.groupIndex, value.name(), type, location, byteOffset);
			}
			group.elements.add(element);
			elements.put(value.name(), element);
		}

		this.groups = new ArrayList<>(groups.values());
		this.elements = elements;
		this.uniforms = uniforms;
	}

	public UniformData(UniformData data, boolean preserveUniforms) {
		this.elements = data.elements;
		List<UniformBufferBlock> list = new ArrayList<>();
		for(UniformBufferBlock group : data.groups) {
			UniformBufferBlock uniformBufferBlock = new UniformBufferBlock(group, preserveUniforms);
			list.add(uniformBufferBlock);
		}
		this.groups = list;
		this.uniforms = data.uniforms
			.stream()
			.map(u -> preserveUniforms ? Uniform.copy(u) : Uniform.createNew(u))
			.toList();
	}


	public UniformData flush() {
		for(Uniform uniform : this.uniforms) {
			uniform.rebind = true;
			uniform.reset();
		}
		for(VAO.ElementGroup group : this.groups) {
			group.reupload = true;
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
			UniformBufferBlock group = this.groups.get(e.groupIndex());
			group.reupload = true;
			BufferBuilder buffer = group.buffer;
			ByteBuffer byteBuf = buffer.buffer;
			byteBuf.position(e.byteOffset());
			return buffer;
		}
	}

	public void copyTo(Element from, UniformData toData, Element to) {
		if(from instanceof LazyElement l) {
			from = l.getSelf();
		}
		if(to instanceof LazyElement l) {
			to = l.getSelf();
		}

		if(to.getClass() != from.getClass()) {
			throw new IllegalArgumentException("Either both or neither uniforms must be in an interface block");
		}
		if(from instanceof StandardUniform standard) {
			Uniform fromUniform = this.uniforms.get(standard.uniformIndex);
			Uniform toUniform = this.uniforms.get(((StandardUniform) to).uniformIndex);
			fromUniform.copyTo(toUniform);
			toUniform.rebind = true;
		} else {
			VAO.Element fromE = (VAO.Element) from;
			VAO.Element toE = (VAO.Element) to;
			if(fromE.type() != toE.type()) {
				throw new IllegalArgumentException("Cannot copy " + fromE.type() + " to " + toE.type() + "!");
			}
			UniformBufferBlock fromGroup = this.groups.get(fromE.groupIndex());
			UniformBufferBlock toGroup = toData.groups.get(toE.groupIndex());
			ByteBuffer fromBuffer = fromGroup.buffer.buffer;
			ByteBuffer toBuffer = toGroup.buffer.buffer;
			toBuffer.put(toE.byteOffset(), fromBuffer, fromE.byteOffset(), fromE.type().byteCount);
			toGroup.reupload = true;
		}
	}

	@Override
	public Element getElement(String name) {
		return this.elements.get(name);
	}

	public UniformData markForReupload() {
		for(UniformBufferBlock group : this.groups) {
			group.reupload = true;
		}
		for(Uniform uniform : this.uniforms) {
			uniform.rebind = true;
		}
		return this;
	}

	public UniformData upload() {
		for(UniformBufferBlock group : this.groups) {
			group.upload();
		}
		for(Uniform uniform : this.uniforms) {
			if(uniform.rebind) {
				uniform.upload();
				uniform.rebind = false;
			}
			uniform.alwaysUpload();
		}
		return this;
	}

	static class UniformBufferBlockManager {
		final int uniformBlockIndex;
		final int[] uniformIndexes;
		final int groupBinding;
		final int byteLength;
		final int paddedByteLength;
		final IntArrayList available;
		int currentlyBoundIndex;
		int uboGlId;
		int bufferLength;

		UniformBufferBlockManager(String name, int program, int binding, int length) {
			this.groupBinding = binding;
			this.uniformBlockIndex = glGetUniformBlockIndex(program, name);
			glUniformBlockBinding(program, this.uniformBlockIndex, binding);

			int[] buf = {0};
			glGetActiveUniformBlockiv(program, this.groupBinding, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, buf);
			this.uniformIndexes = new int[buf[0]];
			glGetActiveUniformBlockiv(program,
				this.groupBinding,
				GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES,
				this.uniformIndexes
			);

			glGetActiveUniformBlockiv(program, this.groupBinding, GL_UNIFORM_BLOCK_DATA_SIZE, buf);
			this.byteLength = buf[0];
			this.paddedByteLength = JMath.ceil(this.byteLength, UBO_PADDING);

			this.available = new IntArrayList(length);
			for(int i = length - 1; i >= 0; i--) {
				this.available.add(i);
			}

			this.bufferLength = length;
			this.allocateBuffer();
			this.bind(0, true);
		}

		public void bind(int index, boolean force) {
			int current = this.currentlyBoundIndex;
			if(current != index || force) {
				glBindBufferRange(
					GL_UNIFORM_BUFFER,
					this.groupBinding,
					this.uboGlId,
					(long) index * this.paddedByteLength,
					this.byteLength
				);
				this.currentlyBoundIndex = index;
			}
		}

		public int allocate(UniformBufferBlock block, boolean permanent) {
			int allocated;
			IntArrayList available = this.available;
			synchronized(available) {
				if(available.isEmpty()) {
					int oldBuffer = this.uboGlId;
					int oldLength = this.bufferLength;
					int newLength = oldLength * 2;
					this.bufferLength = newLength;
					int newBuffer = this.allocateBuffer();
					glCopyBufferSubData(oldBuffer, newBuffer, 0, 0, oldLength);
					glDeleteBuffers(oldBuffer);
					for(int i = newLength - 1; i >= oldLength; i--) {
						available.add(i);
					}
				}
				allocated = available.popInt();
			}

			if(!permanent) {
				BareShader.GL_CLEANUP.register(block, () -> {
					synchronized(available) {
						available.add(allocated);
					}
				});
			}

			return allocated;
		}

		protected int allocateBuffer() {
			int id = this.uboGlId = glGenBuffers();
			glBindBufferBase(GL_UNIFORM_BUFFER, this.groupBinding, this.uboGlId);
			int bufferLength = this.paddedByteLength * this.bufferLength;
			glBufferData(GL_UNIFORM_BUFFER, bufferLength, GL_STATIC_DRAW);
			return id;
		}
	}

	static class UniformBufferBlock extends VAO.ElementGroup {
		final UniformBufferBlockManager manager;
		final int index;
		final int groupIndex;

		public UniformBufferBlock(String name, int program, int index) {
			super(name);
			this.manager = new UniformBufferBlockManager(name, program, index, 4);
			this.byteLength = this.manager.paddedByteLength;
			this.glId = this.manager.uboGlId;

			this.buffer = new BufferBuilder(this.byteLength);
			this.buffer.vertexCount = 1;

			this.index = this.manager.allocate(this, true);
			this.groupIndex = this.manager.groupBinding;
		}

		public UniformBufferBlock() {
			super("default");
			this.groupIndex = -1;
			this.manager = null;
			this.index = -1;
		}

		public UniformBufferBlock(UniformBufferBlock group, boolean preserveUniforms) {
			super(group, preserveUniforms);
			this.manager = group.manager;
			this.index = this.manager.allocate(this, false);
			this.groupIndex = group.groupIndex;
			this.glId = group.glId;
			this.reupload = preserveUniforms;
		}

		@Override
		void upload() {
			this.manager.bind(this.index, false);
			if(this.reupload) {
				this.buffer.uniformCount();
				this.buffer.subUpload(true, (long) this.index * this.byteLength, 1);
				this.reupload = false;
			}
		}

		@Override
		void bind() {
			if(this.glId == 0) {
				this.glId = this.initGlId();
			}
			this.manager.bind(this.index, false);
		}

		@Override
		protected int initGlId() {
			return this.glId;
		}
	}

	public record StandardUniform(String name, DataType type, int location, int uniformIndex) implements Element {}
}
