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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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
	final UniformBufferBlock[] groups;
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
		Int2ObjectMap<UniformBufferBlock> atomics = new Int2ObjectOpenHashMap<>();
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
				int binding = glGetActiveAtomicCounterBufferi(program,
					atomicIndex,
					GL_ATOMIC_COUNTER_BUFFER_BINDING
				);
				if(!atomics.containsKey(binding)) {
					block = new UniformBufferBlock(name + "Family",
						program,
						binding,
						atomicIndex,
						GL_ATOMIC_COUNTER_BUFFER
					);
					blocksByName.put(block.name, block);
					atomics.put(binding, block);
				} else {
					block = atomics.get(binding);
				}
				blocksByIndex.put(i, block);
			}

			int size = aBuf.get(0), type = bBuf.get(0);
			if(size > 1) { // add arrays
				String baseName;
				if(name.charAt(name.length() - 1) == ']') {
					baseName = name.substring(0,
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
			throw new IllegalStateException("Uniform(s) with name(s) " + unreferencedUniforms + " were not " +
			                                "referenced!" + " Uniform(s) with name(s) " + unresolvedUniforms + " were "
			                                + "not found!");
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
			while(atomics.containsKey(bindingPoint = bindingPointCounter++)) {
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
					throw new UnsupportedOperationException("Opaque types like " + type + " cannot exist in uniform " + "buffer blocks!");
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

		int max = blocksByName.values().stream().mapToInt(i -> i.groupIndex).max().orElse(0)+1;
		UniformBufferBlock[] groups = new UniformBufferBlock[max];
		for(UniformBufferBlock value : blocksByName.values()) {
			groups[value.groupIndex] = value;
		}
		this.groups = groups;
		this.elements = elements;
		uniforms.forEach(u -> u.state = new ProgramDefaultUniformState());
		this.uniforms = uniforms;
	}

	public UniformData(UniformData data, boolean preserveUniforms) {
		this.elements = data.elements;
		UniformBufferBlock[] groups = new UniformBufferBlock[data.groups.length];
		for(UniformBufferBlock group : data.groups) {
			UniformBufferBlock uniformBufferBlock = new UniformBufferBlock(group, preserveUniforms);
			groups[group.groupIndex] = uniformBufferBlock;
		}
		this.groups = groups;
		this.uniforms = data.uniforms
			.stream()
			.map(u -> preserveUniforms ? Uniform.copy(u) : Uniform.createNew(u))
			.toList();
	}

	@Override
	public Buf element(Element element) {
		this.validate();
		if(element instanceof StandardUniform s) {
			Uniform uniform = this.uniforms.get(s.uniformIndex);
			uniform.reupload = true;
			uniform.reset();
			return uniform;
		} else {
			ElementImpl e = (ElementImpl) element;
			UniformBufferBlock group = this.groups[e.groupIndex()];
			BufferObjectBuilder buffer = group.buffer();
			buffer.offset(e.byteOffset());
			buffer.next();
			return buffer;
		}
	}


	// todo when we add SSBOs, we need a deleteAll or something since the range in which it binds is variable

	@Override
	public Element getElement(String name) {
		this.validate();
		return this.elements.get(name);
	}

	@Override
	public void invalidate() {
		for(UniformBufferBlock group : this.groups) {
			if(group != null) {
				group.close();
			}
		}
	}

	public void copyTo(Element from, UniformData toData, Element to) {
		this.validate();
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
			UniformBufferBlock fromGroup = this.groups[fromE.groupIndex()];
			UniformBufferBlock toGroup = toData.groups[toE.groupIndex()];
			BufferObjectBuilder fromBuffer = fromGroup.buffer();
			BufferObjectBuilder toBuffer = toGroup.buffer();
			toBuffer.copyAttribute(toGroup.alloc,
				fromE.byteOffset(),
				fromE.type().byteCount,
				fromBuffer,
				fromGroup.alloc
			);
		}
	}

	public UniformData upload() {
		this.validate();
		for(UniformBufferBlock group : this.groups) {
			if(group != null) {
				group.upload();
			}
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
		this.validate();
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
		/**
		 * to -> from
		 */
		final Int2IntMap deferredCopies = new Int2IntOpenHashMap();
		int nextNewId;

		UniformBufferBlockManager(String group, int program, int binding, int index, int type) {
			this.name = group;
			this.bufferType = type;
			this.binding = binding;
			this.bindingIndex = index;
			if(type == GL_UNIFORM_BUFFER) {
				glUniformBlockBinding(program, index, binding);
				this.byteLength = glGetActiveUniformBlocki(program, index, GL_UNIFORM_BLOCK_DATA_SIZE);
				this.paddedByteLength = JMath.ceil(this.byteLength, UBO_PADDING);
				this.bind = GLContextState.UNIFORM_BUFFER;
				this.buffer = BufferObjectBuilder.uniform(this.paddedByteLength);
			} else {
				this.byteLength = glGetActiveAtomicCounterBufferi(program, index, GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE);
				this.paddedByteLength = JMath.ceil(this.byteLength, UBO_PADDING);
				this.bind = GLContextState.ATOMIC_COUNTERS;
				this.buffer = BufferObjectBuilder.atomic_counter(this.paddedByteLength);
			}

			this.postInit();
			this.deferredCopies.defaultReturnValue(-1);
		}

		public BufferObjectBuilder forIndex(int alloc) {
			synchronized(this.deferredCopies) {
				int from = this.deferredCopies.remove(alloc);
				if(from != -1) {
					this.buffer.copyFrom(alloc, this.buffer, from, 1, true);
				}
				var iterator = this.deferredCopies.int2IntEntrySet().iterator();
				while(iterator.hasNext()) {
					Int2IntMap.Entry entry = iterator.next();
					if(entry.getIntValue() == alloc) { // from == this
						int key = entry.getIntKey();
						this.buffer.copyFrom(key, this.buffer, alloc, 1, true);
						iterator.remove();
					}
				}
			}

			this.buffer.index(alloc);
			return this.buffer;
		}

		public void addDeferredCopy(int from, int to) {
			synchronized(this.deferredCopies) {
				//this.buffer.copyFrom(to, this.buffer, from, 1, true);
				this.deferredCopies.put(to, from);
			}
		}

		public void bindRange(int alloc) {
			int real;
			synchronized(this.deferredCopies) {
				real = this.deferredCopies.getOrDefault(alloc, alloc);
			}
			this.buffer.upload(false); // flush contents
			this.bind.bindBufferRange(this.binding,
				this.buffer.getId(),
				real * this.paddedByteLength,
				this.byteLength
			);
		}

		public int allocate() {
			int allocated;
			synchronized(this.available) {
				IntArrayList available = this.available;
				if(available.isEmpty()) {
					allocated = this.nextNewId++;
				} else {
					allocated = available.popInt();
					synchronized(this.deferredCopies) {
						this.deferredCopies.remove(allocated);
						for(Int2IntMap.Entry entry : this.deferredCopies.int2IntEntrySet()) {
							if(entry.getIntValue() == allocated) {
								this.buffer.copyFrom(entry.getIntKey(), this.buffer, allocated, 1, true);
							}
						}
					}
				}
			}

			return allocated;
		}

		public void reclaim(int alloc) {
			synchronized(this.available) {
				this.available.add(alloc);
			}
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
			this.alloc = this.manager.allocate();
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
			this.alloc = this.manager.allocate();
			this.groupIndex = group.groupIndex;
			this.uniformIndices = group.uniformIndices;
			this.bufferType = group.bufferType;

			if(preserveUniforms) {
				this.manager.addDeferredCopy(group.alloc, this.alloc);
			}
		}

		public BufferObjectBuilder buffer() {
			return this.manager.forIndex(this.alloc);
		}

		public void close() {
			this.manager.reclaim(this.alloc);
		}

		void upload() {
			this.manager.bindRange(this.alloc);
		}
	}

	public record StandardUniform(String name, DataType type, int location, int uniformIndex) implements Element {}
}
