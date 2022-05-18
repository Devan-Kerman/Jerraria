package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.GL_ACTIVE_UNIFORMS;
import static org.lwjgl.opengl.GL20.glGetActiveUniform;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL31.GL_ACTIVE_UNIFORM_BLOCKS;
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
import static org.lwjgl.opengl.GL31.glGetActiveUniformBlockName;
import static org.lwjgl.opengl.GL31.glGetActiveUniformBlockiv;
import static org.lwjgl.opengl.GL31.glGetActiveUniformsi;
import static org.lwjgl.opengl.GL31.glGetInteger;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glGetUniformIndices;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

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
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.JMath;


public class UniformData extends GlData {
	public static final int UBO_PADDING = glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
	final Map<String, Element> elements;
	final List<UniformBufferBlock> groups;
	final List<Uniform> uniforms;

	public UniformData(Map<String, BareShader.Field> fields, int program, Id id) {
		IntBuffer aBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		IntBuffer bBuf = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

		// uniform blocks
		Map<String, UniformBufferBlock> blocksByName = new LinkedHashMap<>();
		Int2ObjectMap<UniformBufferBlock> blocksByIndex = new Int2ObjectOpenHashMap<>();
		glGetProgramiv(program, GL_ACTIVE_UNIFORM_BLOCKS, aBuf);
		int uniformBlockCount = aBuf.get(0);
		for(int i = 0; i < uniformBlockCount; i++) {
			String name = glGetActiveUniformBlockName(program, i);
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_DATA_SIZE, aBuf);
			int uboSize = aBuf.get(0);
			if(uboSize > 16000) {
				System.err.println("[Warning] byte size of uniform block " + name + " in shader " + id + " exceeds " +
				                   "minimum guaranteed UBO size!");
			}
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, aBuf);
			int activeUniforms = aBuf.get(0);
			IntBuffer uniformIndexes = ByteBuffer
				.allocateDirect(activeUniforms * 4)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer();
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndexes);
			UniformBufferBlock block = new UniformBufferBlock(name, program, i);
			for(int off = 0; off < activeUniforms; off++) {
				int uniformIndex = uniformIndexes.get(off);
				block.uniformIndices.add(uniformIndex);
				blocksByIndex.put(uniformIndex, block);
			}
			blocksByName.put(name, block);
		}

		// actual uniforms
		glGetProgramiv(program, GL_ACTIVE_UNIFORMS, aBuf);
		int uniformCount = aBuf.get(0);
		record ActiveUniform(String name,
		                     int location, // normal uniforms
		                     int index, int byteOffset, // UBO uniforms
		                     int glslType
		) {}
		Map<String, ActiveUniform> uniformMap = new LinkedHashMap<>();
		for(int i = 0; i < uniformCount; i++) {
			String name = glGetActiveUniform(program, i, aBuf, bBuf);
			if(name.startsWith("gl_")) { // ignore builtins
				continue;
			}
			int location = glGetUniformLocation(program, name);
			int size = aBuf.get(0), type = bBuf.get(0);
			if(size > 1) { // add arrays
				String baseName;
				if(name.charAt(name.length()-1) == ']') {
					baseName = name.substring(0, Validate.greaterThanEqualTo(name.indexOf('['), 0, "Weird array uniform name: " + name));
				} else {
					baseName = name;
				}
				int stride = glGetActiveUniformsi(program, i, GL_UNIFORM_ARRAY_STRIDE);
				int offset = glGetActiveUniformsi(program, i, GL_UNIFORM_OFFSET);
				for(int index = 0; index < size; index++) {
					String indexName = String.format("%s[%d]", baseName, index);
					int attribLocation = glGetUniformLocation(program, indexName);
					int byteOffset = stride * index + offset;
					uniformMap.put(indexName, new ActiveUniform(indexName, attribLocation, i, byteOffset, type));
				}
			} else {
				uniformMap.put(name, new ActiveUniform(name, location, i, -1, type));
			}
		}
		Set<String> unreferencedUniforms = new LinkedHashSet<>(uniformMap.keySet());
		unreferencedUniforms.removeAll(fields.keySet());
		Set<String> unresolvedUniforms = new LinkedHashSet<>(fields.keySet());
		unresolvedUniforms.removeAll(uniformMap.keySet());
		if(!unresolvedUniforms.isEmpty() || !unreferencedUniforms.isEmpty()) {
			throw new IllegalStateException("Uniform(s) with name(s) " + unreferencedUniforms + " were not referenced! Uniform(s) with name(s) " + unresolvedUniforms + " were not found!");
		}

		AtomicInteger textureUnitCounter = new AtomicInteger();
		Map<String, Element> elements = new HashMap<>();
		List<Uniform> uniforms = new ArrayList<>();
		for(ActiveUniform uniform : uniformMap.values()) {
			String name = uniform.name;
			Element element;
			BareShader.Field field = fields.get(name);
			DataType type = field.type();
			if(!type.isCompatible(uniform.glslType)) {
				throw new UnsupportedOperationException(type + " is not valid for type for \"" + name + "\" " +
				                                        "suggested types: " + DataType.forGlslType(
					uniform.glslType));
			}

			if(uniform.location != -1) {
				if(field.groupName(true) != null) {
					throw new IllegalArgumentException("Uniform with name " + name + " is not in an interface " +
					                                   "block!");
				}
				element = new StandardUniform(name, type, uniform.location, uniform.index);
				if(type.isSampler) {
					uniforms.add(Uniform.createSampler(type, uniform.location, textureUnitCounter.getAndIncrement()));
				} else {
					uniforms.add(Uniform.create(type, uniform.location));
				}
			} else {
				// for nested arrays or arrays of a vanilla type, we must start at the [0] index and calculate forward!
				UniformBufferBlock group = blocksByIndex.get(uniform.index);
				if(group == null) {
					throw new UnsupportedOperationException("Uniform with name " + name + " is not in an " +
					                                        "interface block!");
				}

				String groupName = field.groupName(true);
				if(groupName != null && !groupName.equals(group.name)) {
					throw new IllegalArgumentException("Specified group name " + groupName + " does not match real " +
					                                   "group name " + group.name + ", omit the group name to " +
					                                   "autodetect!");
				}

				element = new VAO.Element(group.groupIndex, name, type, uniform.index, uniform.byteOffset);
				group.elements.add(element);
			}

			elements.put(uniform.name, element);
		}

		this.groups = new ArrayList<>(blocksByName.values());
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

	@Override
	public Element getElement(String name) {
		return this.elements.get(name);
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
			this.bind(0);
		}

		public void bind(int index) {
			glBindBufferRange(GL_UNIFORM_BUFFER,
				this.groupBinding,
				this.uboGlId,
				(long) index * this.paddedByteLength,
				this.byteLength
			);
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
		final IntSet uniformIndices;

		public UniformBufferBlock(String name, int program, int index) {
			super(name);
			this.manager = new UniformBufferBlockManager(name, program, index, 4);
			this.byteLength = this.manager.paddedByteLength;
			this.glId = this.manager.uboGlId;

			this.buffer = new BufferBuilder(this.byteLength);
			this.buffer.vertexCount = 1;

			this.index = this.manager.allocate(this, true);
			this.groupIndex = this.manager.groupBinding;
			this.uniformIndices = new IntOpenHashSet();
		}

		public UniformBufferBlock() {
			super("default");
			this.groupIndex = -1;
			this.manager = null;
			this.index = -1;
			this.uniformIndices = null;
		}

		public UniformBufferBlock(UniformBufferBlock group, boolean preserveUniforms) {
			super(group, preserveUniforms);
			this.manager = group.manager;
			this.index = this.manager.allocate(this, false);
			this.groupIndex = group.groupIndex;
			this.glId = group.glId;
			this.reupload = preserveUniforms;
			this.uniformIndices = group.uniformIndices;
		}

		@Override
		void upload() {
			this.manager.bind(this.index);
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
			this.manager.bind(this.index);
		}

		@Override
		protected int initGlId() {
			return this.glId;
		}
	}

	public record StandardUniform(String name, DataType type, int location, int uniformIndex) implements Element {}
}
