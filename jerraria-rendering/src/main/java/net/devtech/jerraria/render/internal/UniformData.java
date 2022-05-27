package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER_BINDING;
import static org.lwjgl.opengl.GL42.GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE;
import static org.lwjgl.opengl.GL42.GL_UNIFORM_ATOMIC_COUNTER_BUFFER_INDEX;
import static org.lwjgl.opengl.GL42.glGetActiveAtomicCounterBufferi;

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
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.basic.ImageFormat;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.internal.state.ProgramDefaultUniformState;
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

		// actual uniforms
		glGetProgramiv(program, GL_ACTIVE_UNIFORMS, aBuf);
		int uniformCount = aBuf.get(0);
		Map<String, ActiveUniform> uniformMap = new LinkedHashMap<>();
		Map<String, UniformBufferBlock> blocksByName = new LinkedHashMap<>();
		Int2ObjectMap<UniformBufferBlock> blocksByIndex = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<UniformBufferBlock> atomicBuffers = new Int2ObjectOpenHashMap<>();
		for(int i = 0; i < uniformCount; i++) {
			String name = glGetActiveUniform(program, i, aBuf, bBuf);
			if(name.startsWith("gl_")) { // ignore builtins
				continue;
			}
			int location = glGetUniformLocation(program, name);
			int offset = glGetActiveUniformsi(program, i, GL_UNIFORM_OFFSET);
			int atomicIndex = glGetActiveUniformsi(program, i, GL_UNIFORM_ATOMIC_COUNTER_BUFFER_INDEX);
			if(atomicIndex != -1) {
				UniformBufferBlock block;
				if(!atomicBuffers.containsKey(atomicIndex)) {
					int binding = glGetActiveAtomicCounterBufferi(program,
						atomicIndex,
						GL_ATOMIC_COUNTER_BUFFER_BINDING
					);
					block = new UniformBufferBlock(name + "Family",
						program,
						binding,
						atomicIndex,
						GL_ATOMIC_COUNTER_BUFFER
					);
					blocksByName.put(block.name, block);
					atomicBuffers.put(atomicIndex, block);
				} else {
					block = atomicBuffers.get(atomicIndex);
				}
				blocksByIndex.put(i, block);
			}

			int size = aBuf.get(0), type = bBuf.get(0);
			if(size > 1) { // add arrays
				String baseName;
				if(name.charAt(name.length() - 1) == ']') {
					baseName = name.substring(
						0,
						Validate.greaterThanEqualTo(name.indexOf('['), 0, "Weird array uniform name: " + name)
					);
				} else {
					baseName = name;
				}
				int stride = glGetActiveUniformsi(program, i, GL_UNIFORM_ARRAY_STRIDE);
				for(int index = 0; index < size; index++) {
					String indexName = String.format("%s[%d]", baseName, index);
					int attribLocation = glGetUniformLocation(program, indexName);
					int byteOffset = stride * index + offset;
					uniformMap.put(indexName, new ActiveUniform(indexName, attribLocation, i, byteOffset, type));
				}
			} else {
				uniformMap.put(name, new ActiveUniform(name, location, i, offset, type));
			}
		}

		List<String> unoptionalNames = fields
			.values()
			.stream()
			.filter(BareShader.Field::isMandatory)
			.map(BareShader.Field::name)
			.toList();
		Set<String> unreferencedUniforms = new LinkedHashSet<>(uniformMap.keySet());
		unoptionalNames.forEach(unreferencedUniforms::remove);
		Set<String> unresolvedUniforms = new LinkedHashSet<>(unoptionalNames);
		unresolvedUniforms.removeAll(uniformMap.keySet());
		if(!unresolvedUniforms.isEmpty() || !unreferencedUniforms.isEmpty()) {
			throw new IllegalStateException("Uniform(s) with name(s) " + unreferencedUniforms + " were not referenced!" +
			                                " Uniform(s) with name(s) " + unresolvedUniforms + " were not found!");
		}

		// uniform blocks
		glGetProgramiv(program, GL_ACTIVE_UNIFORM_BLOCKS, aBuf);
		int uniformBlockCount = aBuf.get(0);
		int bindingPointCounter = 0;
		for(int i = 0; i < uniformBlockCount; i++) {
			String name = glGetActiveUniformBlockName(program, i);
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_DATA_SIZE, aBuf);
			int uboSize = aBuf.get(0);
			if(uboSize > 16000) {
				System.err.println("[Warning] byte size of uniform block " + name + " in shader " + id + " exceeds " + "minimum guaranteed UBO size!");
			}
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, aBuf);
			int activeUniforms = aBuf.get(0);
			IntBuffer uniformIndexes = ByteBuffer
				.allocateDirect(activeUniforms * 4)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer();
			glGetActiveUniformBlockiv(program, i, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, uniformIndexes);

			int bindingPoint;
			while(atomicBuffers.containsKey(bindingPoint = bindingPointCounter++)) {
			}

			UniformBufferBlock block = new UniformBufferBlock(name, program, bindingPoint, i, GL_UNIFORM_BUFFER);
			for(int off = 0; off < activeUniforms; off++) {
				int uniformIndex = uniformIndexes.get(off);
				block.uniformIndices.add(uniformIndex);
				blocksByIndex.put(uniformIndex, block);
			}
			blocksByName.put(name, block);
		}


		AtomicInteger imageUnitCounter = new AtomicInteger();
		AtomicInteger textureUnitCounter = new AtomicInteger();
		Map<String, Element> elements = new HashMap<>();
		List<Uniform> uniforms = new ArrayList<>();
		for(ActiveUniform uniform : uniformMap.values()) {
			String name = uniform.name;
			Element element;
			BareShader.Field field = fields.get(name);
			DataType type = field.type();
			if(!type.isCompatible(uniform.glslType)) {
				Set<DataType> types = DataType.forGlslType(uniform.glslType);
				List<String> glslNames = types.stream().map(DataType::toString).toList();
				throw new UnsupportedOperationException(type + " is not valid for type for \"" + glslNames + " " + name + "\" " + "suggested types: " + types);
			}

			if(uniform.location != -1) {
				if(field.groupName(true) != null) {
					throw new IllegalArgumentException("Uniform with name " + name + " is not in an interface block!");
				}
				element = new StandardUniform(name, type, uniform.location, uniforms.size());
				if(type.isImage) {
					uniforms.add(Uniform.createImage(type,
						uniform.location,
						imageUnitCounter.getAndIncrement(),
						(ImageFormat) field.extra()
					));
				} else if(type.isSampler) {
					uniforms.add(Uniform.createSampler(type, uniform.location, textureUnitCounter.getAndIncrement()));
				} else {
					uniforms.add(Uniform.create(type, uniform.location));
				}
			} else {
				if(type.isOpaque()) {
					throw new UnsupportedOperationException("Opaque types like " + type + " cannot exist in uniform " +
					                                        "buffer blocks!");
				}

				// for nested arrays or arrays of a vanilla type, we must start at the [0] index and calculate forward!
				UniformBufferBlock group = blocksByIndex.get(uniform.index);
				if(group == null) {
					throw new UnsupportedOperationException("Uniform with name " + name + " is not in an " +
					                                        "interface block!");
				}

				String groupName = field.groupName(true);
				if(groupName != null && !groupName.equals(group.name)) {
					throw new IllegalArgumentException("Specified group name " + groupName + " does not match real " + "group name " + group.name + ", omit the group name to " + "autodetect!");
				}

				element = new ElementImpl(group.groupIndex, name, type, uniform.index, uniform.byteOffset);
			}

			elements.put(uniform.name, element);
		}

		this.groups = new ArrayList<>(blocksByName.values());
		this.elements = elements;
		uniforms.forEach(u -> u.state = new ProgramDefaultUniformState());
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

	@Override
	public Buf element(Element element) {
		if(element instanceof StandardUniform s) {
			Uniform uniform = this.uniforms.get(s.uniformIndex);
			uniform.reupload = true;
			uniform.reset();
			return uniform;
		} else {
			ElementImpl e = (ElementImpl) element;
			UniformBufferBlock group = this.groups.get(e.groupIndex());
			BufferObjectBuilder buffer = group.buffer();
			buffer.offset(e.byteOffset());
			return buffer;
		}
	}


	// todo when we add SSBOs, we need a deleteAll or something since the range in which it binds is variable

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

		// todo better copying
		if(from instanceof StandardUniform standard) {
			Uniform fromUniform = this.uniforms.get(standard.uniformIndex);
			Uniform toUniform = this.uniforms.get(((StandardUniform) to).uniformIndex);
			fromUniform.copyTo(toUniform);
			toUniform.reupload = true;
		} else {
			ElementImpl fromE = (ElementImpl) from;
			ElementImpl toE = (ElementImpl) to;
			if(fromE.type() != toE.type()) {
				throw new IllegalArgumentException("Cannot copy " + fromE.type() + " to " + toE.type() + "!");
			}
			UniformBufferBlock fromGroup = this.groups.get(fromE.groupIndex());
			UniformBufferBlock toGroup = toData.groups.get(toE.groupIndex());
			BufferObjectBuilder fromBuffer = fromGroup.buffer();
			BufferObjectBuilder toBuffer = toGroup.buffer();
			toBuffer.copyAttribute(
				toGroup.alloc,
				fromE.byteOffset(),
				fromE.type().byteCount,
				fromBuffer,
				fromGroup.alloc
			);
		}
	}

	public UniformData upload() {
		for(UniformBufferBlock group : this.groups) {
			group.upload();
		}
		for(Uniform uniform : this.uniforms) {
			if(uniform.state.updateUniform(uniform, uniform.reupload)) {
				uniform.upload();
				uniform.reupload = false;
			}
			uniform.alwaysUpload();
		}
		return this;
	}

	public void markForRebind() {
		for(Uniform uniform : this.uniforms) {
			uniform.reupload = true;
		}
	}

	record ActiveUniform(String name, int location, // normal uniforms
	                     int index, int byteOffset, // UBO uniforms
	                     int glslType) {}

	static class UniformBufferBlockManager {
		private static final int INITIAL_BUFFER_LEN = 4;
		final int bufferType;
		final int bindingIndex;
		final int binding;
		final int byteLength;
		final int paddedByteLength;
		final IntArrayList available = new IntArrayList(INITIAL_BUFFER_LEN);
		final String name;
		final GLContextState.IndexedBufferTargetState bind;
		final BufferObjectBuilder buffer;
		final Int2ObjectMap<IntList> deferredCopies = new Int2ObjectOpenHashMap<>();
		int nextNewId;

		UniformBufferBlockManager(String group, int program, int binding, int index, int type) {
			this.name = group;
			this.bufferType = type;
			this.binding = binding;
			this.bindingIndex = index;
			if(type == GL_UNIFORM_BUFFER) {
				glUniformBlockBinding(program, index, binding);
				this.byteLength = glGetActiveUniformBlocki(program, index, GL_UNIFORM_BLOCK_DATA_SIZE);
				this.bind = GLContextState.UNIFORM_BUFFER;
			} else {
				this.byteLength = glGetActiveAtomicCounterBufferi(program, index, GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE);
				this.bind = GLContextState.ATOMIC_COUNTERS;
			}

			this.paddedByteLength = JMath.ceil(this.byteLength, UBO_PADDING);
			this.buffer = BufferObjectBuilder.uniform(this.paddedByteLength);
			this.postInit();
		}

		public BufferObjectBuilder forIndex(int alloc) { // todo deferred copy
			//IntList remove = this.deferredCopies.remove(alloc);
			//if(remove != null) {
			//	for(int i = 0; i < remove.size(); i++) {
			//		this.buffer.copyFrom(remove.getInt(i), this.buffer, alloc, 1);
			//	}
			//}
			this.buffer.index(alloc);
			return this.buffer;
		}

		public void bindRange(int alloc) {
			this.buffer.upload(false); // flush contents
			this.bind.bindBufferRange(
				this.bindingIndex,
				this.buffer.getOrGenId(),
				alloc * this.paddedByteLength,
				this.byteLength
			);
		}

		public int allocate(UniformBufferBlock block, boolean permanent) {
			int allocated;
			IntArrayList available = this.available;
			synchronized(available) {
				if(available.isEmpty()) {
					allocated = this.nextNewId++;
				} else {
					allocated = available.popInt();
				}
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

		private void postInit() {
			for(int i = INITIAL_BUFFER_LEN - 1; i >= 0; i--) {
				this.available.add(i);
			}
			this.nextNewId = INITIAL_BUFFER_LEN;
			this.bindRange(0);
		}
	}

	static class UniformBufferBlock {
		final UniformBufferBlockManager manager;
		final int alloc;
		final int groupIndex;
		final IntSet uniformIndices;
		final int bufferType;
		final String name;

		// todo deferred copy

		public UniformBufferBlock(UniformBufferBlockManager manager, int bufferType) {
			this.manager = manager;
			this.name = manager.name;
			this.alloc = this.manager.allocate(this, true);
			this.groupIndex = this.manager.binding;
			this.uniformIndices = new IntOpenHashSet();
			this.bufferType = bufferType;
		}

		public UniformBufferBlock(String name, int program, int binding, int index, int type) {
			this(new UniformBufferBlockManager(name, program, binding, index, type), type);
		}

		public UniformBufferBlock(UniformBufferBlock group, boolean preserveUniforms) {
			this.name = group.name;
			this.manager = group.manager;
			this.alloc = this.manager.allocate(this, false);
			this.groupIndex = group.groupIndex;
			this.uniformIndices = group.uniformIndices;
			this.bufferType = group.bufferType;

			if(preserveUniforms) {
				BufferObjectBuilder builder = this.manager.forIndex(this.alloc);
				builder.appendFrom(builder, 0, 1);
			}
		}

		public BufferObjectBuilder buffer() {
			BufferObjectBuilder builder = this.manager.forIndex(this.alloc);
			builder.next();
			return builder;
		}

		void upload() {
			this.manager.bindRange(this.alloc);
		}
	}

	public record StandardUniform(String name, DataType type, int location, int uniformIndex) implements Element {}
}
