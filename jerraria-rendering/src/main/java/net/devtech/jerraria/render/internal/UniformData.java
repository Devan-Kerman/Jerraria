package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL46.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.devtech.jerraria.render.api.OpenGLSupport;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.base.ImageFormat;
import net.devtech.jerraria.render.internal.buffers.ACBOBuilder;
import net.devtech.jerraria.render.internal.buffers.BufferObjectBuilderAccess;
import net.devtech.jerraria.render.internal.buffers.SSBOBuilder;
import net.devtech.jerraria.render.internal.buffers.SharedUBOBuilder;
import net.devtech.jerraria.render.internal.buffers.UBOBuilder;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.internal.state.ProgramDefaultUniformState;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL46;

public class UniformData extends GlData {
	public final Map<String, Element> elements;
	final UniformBufferBlock[] groups;
	final List<ShaderBufferBlock> ssbos;
	final List<Uniform> uniforms;

	public UniformData(Map<String, BareShader.Field> fields, int program, Id id) {
		IntBuffer aBuf = buffer(1);
		IntBuffer bBuf = buffer(1);
		// actual uniforms
		glGetProgramiv(program, GL_ACTIVE_UNIFORMS, aBuf);
		int uniformCount = aBuf.get(0);
		Map<String, ActiveUniform> uniformMap = new LinkedHashMap<>();
		Map<String, UniformBufferBlock> blocksByName = new LinkedHashMap<>();
		Int2ObjectMap<UniformBufferBlock> blocksByIndex = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<UniformBufferBlock> atomics = new Int2ObjectOpenHashMap<>();
		for(int uni = 0; uni < uniformCount; uni++) {
			String name = glGetActiveUniform(program, uni, aBuf, bBuf);
			if(name.startsWith("gl_")) { // ignore builtins
				continue;
			}
			int location = glGetUniformLocation(program, name);
			int offset = glGetActiveUniformsi(program, uni, GL_UNIFORM_OFFSET);
			int atomicIndex = glGetActiveUniformsi(program, uni, GL_UNIFORM_ATOMIC_COUNTER_BUFFER_INDEX);
			if(atomicIndex != -1) {
				UniformBufferBlock block;
				int binding = glGetActiveAtomicCounterBufferi(program, atomicIndex, GL_ATOMIC_COUNTER_BUFFER_BINDING);
				if(!atomics.containsKey(binding)) {
					block = new UniformBufferBlock(name + "Family",
						new int[] {0},
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
				blocksByIndex.put(uni, block);
			}

			int size = aBuf.get(0), type = bBuf.get(0);
			if(size > 1) { // add arrays
				String baseName = arrayIndexTemplate(name);
				int stride = glGetActiveUniformsi(program, uni, GL_UNIFORM_ARRAY_STRIDE);
				for(int index = 0; index < size; index++) {
					String indexName = String.format("%s[%d]", baseName, index);
					int attribLocation = glGetUniformLocation(program, indexName);
					int byteOffset = stride * index + offset;
					uniformMap.put(indexName, new ActiveUniform(indexName, attribLocation, uni, byteOffset, type, -1));
				}
			} else {
				uniformMap.put(name, new ActiveUniform(name, location, uni, offset, type, -1));
			}
		}

		List<ShaderBufferBlock> ssbos;
		if(OpenGLSupport.SSBO) {
			ssbos = new ArrayList<>();
			int activeSSBOs = glGetProgramInterfacei(program, GL_SHADER_STORAGE_BLOCK, GL_ACTIVE_RESOURCES);
			int maxSSBOLen = glGetProgramInterfacei(program, GL_SHADER_STORAGE_BLOCK, GL_MAX_NAME_LENGTH);
			int maxVarLen = glGetProgramInterfacei(program, GL_BUFFER_VARIABLE, GL_MAX_NAME_LENGTH);
			int maxActiveVars = glGetProgramInterfacei(program, GL_SHADER_STORAGE_BLOCK, GL_MAX_NUM_ACTIVE_VARIABLES);
			IntBuffer buffer = buffer(maxActiveVars), query = buffer(6), answer = buffer(6);
			for(int ssbo = 0; ssbo < activeSSBOs; ssbo++) {
				String name = glGetProgramResourceName(program, GL_SHADER_STORAGE_BLOCK, ssbo, maxSSBOLen);
				int resource = glGetProgramResourceIndex(program, GL_SHADER_STORAGE_BLOCK, name);
				aBuf.put(0, GL_BUFFER_BINDING);
				glGetProgramResourceiv(program, GL_SHADER_STORAGE_BLOCK, resource, aBuf, null, bBuf);
				int binding = bBuf.get(0);

				aBuf.put(0, GL_NUM_ACTIVE_VARIABLES);
				glGetProgramResourceiv(program, GL_SHADER_STORAGE_BLOCK, resource, aBuf, null, bBuf);
				int activeVars = bBuf.get(0);

				buffer.limit(activeVars);
				aBuf.put(0, GL_ACTIVE_VARIABLES);
				glGetProgramResourceiv(program, GL_SHADER_STORAGE_BLOCK, resource, aBuf, null, buffer);

				record StructArrayField(String name, int rootOffset, int type) {}
				IntList fixed = new IntArrayList();
				List<StructArrayField> structArrayFields = new ArrayList<>();

				int stride = -1, off = Integer.MAX_VALUE;
				for(int i = 0; i < activeVars; i++) {
					int varResource = buffer.get(i);
					query
						.put(0, GL_OFFSET)
						.put(1, GL_ARRAY_STRIDE)
						.put(2, GL_ARRAY_SIZE)
						.put(3, GL_TYPE)
						.put(4, GL_TOP_LEVEL_ARRAY_STRIDE)
						.put(5, GL_TOP_LEVEL_ARRAY_SIZE);
					glGetProgramResourceiv(program, GL_BUFFER_VARIABLE, varResource, query, null, answer);
					int offset = answer.get(0), arrayStride = answer.get(1), arraySize = answer.get(2);
					int type = answer.get(3), arrayStride2 = answer.get(4), arraySize2 = answer.get(5);
					String varName = glGetProgramResourceName(program, GL_BUFFER_VARIABLE, varResource, maxVarLen);
					if(arraySize2 != 0 && arraySize != 0) {
						if(arraySize == 1) {
							uniformMap.put(varName, new ActiveUniform(varName, -1, ssbo, offset, type, -2));
							fixed.add(offset);
						} else {
							String baseName = arrayIndexTemplate(varName);
							for(int index = 0; index < arraySize; index++) {
								String indexName = String.format("%s[%d]", baseName, index);
								int byteOffset = arrayStride * index + offset;
								uniformMap.put(indexName, new ActiveUniform(indexName, -1, ssbo, byteOffset, type,
									-2));
								fixed.add(byteOffset);
							}
						}
					} else {
						if(stride != -1 && stride != arrayStride2) {
							throw new UnsupportedOperationException("Impossible/Unsupported SSBO layout");
						}
						stride = arrayStride2;
						off = Math.min(offset, off);
						structArrayFields.add(new StructArrayField(arrayIndexTemplate(varName), offset, type));
					}
				}

				structArrayFields.sort(Comparator.comparingInt(s -> s.rootOffset));
				int[] structOffsets = new int[structArrayFields.size()];
				for(int i = 0; i < structArrayFields.size(); i++) {
					StructArrayField field = structArrayFields.get(i);
					structOffsets[i] = field.rootOffset - off;
					uniformMap.put(
						field.name,
						new ActiveUniform(field.name, -1, ssbo, structOffsets[i], field.type, 1)
					);
				}
				fixed.sort(IntComparators.NATURAL_COMPARATOR);
				int[] fixedOffsets = fixed.toIntArray();
				SSBOBuilder builder = new SSBOBuilder(off, fixedOffsets, stride, structOffsets, off);
				ShaderBufferBlock block = new ShaderBufferBlock(builder, binding);
				ssbos.add(block);
			}
		} else {
			ssbos = List.of();
		}
		this.ssbos = ssbos;

		List<String> mandatoryNames = fields
			.values()
			.stream()
			.filter(BareShader.Field::isMandatory)
			.map(BareShader.Field::name)
			.toList();
		Set<String> unreferencedUniforms = new LinkedHashSet<>(uniformMap.keySet());
		fields.keySet().forEach(unreferencedUniforms::remove);
		Set<String> unresolvedUniforms = new LinkedHashSet<>(mandatoryNames);
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

			int[] offsets = new int[activeUniforms];
			ActiveUniform[] uniforms = uniformMap.values().toArray(ActiveUniform[]::new);
			int cur = 0;
			for(int uid = 0; uid < activeUniforms; uid++) {
				int uniformIndex = uniformIndexes.get(uid);
				for(; cur < uniforms.length; cur++) {
					ActiveUniform uniform = uniforms[cur];
					if(uniform.index == uniformIndex) {
						offsets[uid] = uniform.byteOffset;
						break;
					}
				}
			}
			Arrays.sort(offsets);

			UniformBufferBlock block = new UniformBufferBlock(name,
				offsets,
				program,
				bindingPoint,
				i,
				GL_UNIFORM_BUFFER
			);
			for(int off = 0; off < activeUniforms; off++) {
				int uniformIndex = uniformIndexes.get(off);
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
			if(!type.isCompatible(uniform.glslType) || type.normalized) {
				Set<DataType> types = DataType.forGlslType(uniform.glslType);
				List<String> glslNames = types.stream().map(DataType::toString).toList();
				Set<String> suggested = types
					.stream()
					.filter(d -> !d.normalized)
					.map(DataType::toString)
					.collect(Collectors.toSet());
				throw new UnsupportedOperationException(type + " is not valid for type for \"" + glslNames + " " + name + "\" " + "suggested types: " + suggested);
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
			} else if(uniform.ssboType == -1) {
				if(type.isOpaque()) {
					throw new UnsupportedOperationException("Opaque types like " + type + " cannot exist in uniform " + "buffer blocks!");
				}

				// for nested arrays or arrays of a vanilla type, we must start at the [0] index and calculate forward!
				UniformBufferBlock group = blocksByIndex.get(uniform.index);
				if(group == null) {
					throw new UnsupportedOperationException("Uniform with name " + name + " is not in an " + "uniform "
					                                        + "block!");
				}

				String groupName = field.groupName(true);
				if(groupName != null && !groupName.equals(group.name)) {
					throw new IllegalArgumentException("Specified group name " + groupName + " does not match real " + "group name " + group.name + ", omit the group name to " + "autodetect!");
				}

				int offsetIndex = Arrays.binarySearch(group.manager.buffer.structIntervals, uniform.byteOffset);
				if(offsetIndex == -1) {
					throw new IllegalStateException();
				}
				// todo check for atomic
				boolean feedback = false;
				if(type == DataType.ATOMIC_UINT && field.extra() instanceof Boolean b) {
					feedback = b;
				}
				element = new ElementImpl(group.groupIndex, name, type, offsetIndex, uniform.byteOffset, -1, feedback);
				group.manager.needsFeedback = true;
			} else {
				ShaderBufferBlock block = ssbos.get(uniform.index);
				if(uniform.ssboType == -2) {
					int offsetIndex = Arrays.binarySearch(block.builder.fixedIntervals, uniform.byteOffset);
					if(offsetIndex == -1) {
						throw new IllegalStateException();
					}
					element = new ElementImpl(uniform.index, name, type, offsetIndex, uniform.byteOffset, -2, false);
				} else {
					int offsetIndex = Arrays.binarySearch(block.builder.structIntervals, uniform.byteOffset);
					if(offsetIndex == -1) {
						throw new IllegalStateException();
					}
					element = new ElementImpl(uniform.index, name, type, offsetIndex, uniform.byteOffset, 0, false);
				}
			}

			elements.put(uniform.name, element);
		}

		int max = blocksByName.values().stream().mapToInt(i -> i.groupIndex).max().orElse(0) + 1;
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
			if(group == null) {
				continue;
			}
			UniformBufferBlock uniformBufferBlock = new UniformBufferBlock(group, preserveUniforms);
			groups[group.groupIndex] = uniformBufferBlock;
		}
		this.groups = groups;
		this.uniforms = data.uniforms
			.stream()
			.map(u -> preserveUniforms ? Uniform.copy(u) : Uniform.createNew(u))
			.toList();
		this.ssbos = data.ssbos.stream().map(ShaderBufferBlock::new).toList();
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
			int index = e.arrayIndex();
			if(index == -1) {
				UniformBufferBlock group = this.groups[e.groupIndex()];
				BufferObjectBuilderAccess buffer = group.buffer();
				buffer.variable(e.location());
				return buffer;
			} else {
				ShaderBufferBlock block = this.ssbos.get(e.groupIndex());
				block.builder.structVariable(e.arrayIndex(), e.location());
				return block.builder;
			}
		}
	}

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
		for(ShaderBufferBlock ssbo : this.ssbos) {
			ssbo.builder.close();
		}
	}

	public Element getElement(Element name, int index) {
		if(name instanceof ElementImpl e && e.arrayIndex() >= 0) {
			return new ElementImpl((ElementImpl) name, index);
		} else if(name instanceof ElementImpl e) {
			return this.getElement(arrayIndexTemplate(e.name()) + "[" + index + "]");
		} else if(name instanceof StandardUniform u) {
			return this.getElement(arrayIndexTemplate(u.name()) + "[" + index + "]");
		} else {
			throw new UnsupportedOperationException();
		}
	}
	// todo when we add SSBOs, we need a deleteAll or something since the range in which it binds is variable

	public void copyTo(
		UniformData toData,
		int fromGroupIndex,
		int fromByteOffset,
		int fromArrayIndex,
		int toGroupIndex,
		int toByteOffset,
		int toArrayIndex,
		int len) {
		if(fromArrayIndex == -1 && toArrayIndex == -1) {
			UniformBufferBlock fromGroup = this.groups[fromGroupIndex];
			UniformBufferBlock toGroup = toData.groups[toGroupIndex];
			BufferObjectBuilderAccess fromBuffer = fromGroup.buffer();
			BufferObjectBuilderAccess toBuffer = toGroup.buffer();
			toBuffer.copyFrom(fromBuffer, fromGroup.alloc, toGroup.alloc, fromByteOffset, toByteOffset, len);
		} else if(fromArrayIndex != -1 && toArrayIndex != -1) {
			ShaderBufferBlock fromBlock = this.ssbos.get(fromGroupIndex);
			ShaderBufferBlock toBlock = toData.ssbos.get(toGroupIndex);
			toBlock.builder.copyFrom(fromBlock.builder,
				fromArrayIndex,
				toArrayIndex,
				fromByteOffset,
				toByteOffset,
				len
			);
		} else {
			throw new UnsupportedOperationException(
				"Cannot current between Uniforms in different locations (eg. SSBO vs UBO vs Standard Uniform)!");
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
			if(!(to instanceof StandardUniform to_)) {
				throw new UnsupportedOperationException(
					"Cannot current between Uniforms in different locations (eg. SSBO vs UBO vs Standard Uniform)!");
			}
			Uniform fromUniform = this.uniforms.get(standard.uniformIndex);
			Uniform toUniform = this.uniforms.get(to_.uniformIndex);
			fromUniform.copyTo(toUniform);
			toUniform.reupload = true;
		} else {
			ElementImpl fromE = (ElementImpl) from;
			ElementImpl toE = (ElementImpl) to;
			if(fromE.type() != toE.type()) {
				throw new UnsupportedOperationException("Cannot current " + fromE.type() + " to " + toE.type() + "!");
			}
			this.copyTo(toData,
				fromE.groupIndex(),
				fromE.offsetIndex(),
				fromE.arrayIndex(),
				toE.groupIndex(),
				toE.offsetIndex(),
				toE.arrayIndex(),
				toE.type().byteCount
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
		for(ShaderBufferBlock ssbo : this.ssbos) {
			ssbo.bind();
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

	public void feedback() {
		boolean membar = true;
		for(UniformBufferBlock group : this.groups) {
			if(group != null && group.manager.needsFeedback) {
				if(membar) {
					glMemoryBarrier(GL46.GL_ATOMIC_COUNTER_BARRIER_BIT);
					membar = false;
				}
				group.manager.buffer.loadFeedback();
			}
		}
	}

	private static String arrayIndexTemplate(String name) {
		String baseName;
		if(name.charAt(name.length() - 1) == ']') {
			baseName = name.substring(0,
				Validate.greaterThanEqualTo(name.indexOf('['), 0, "Weird array uniform name: " + name)
			);
		} else {
			baseName = name;
		}
		return baseName;
	}

	static IntBuffer buffer(int ints) {
		return ByteBuffer.allocateDirect(4 * ints).order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	record ActiveUniform(String name, int location, // normal uniforms
	                     int index, int byteOffset, // UBO uniforms
	                     int glslType, int ssboType) {}

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
		final SharedUBOBuilder buffer;
		/**
		 * to -> from
		 */
		final Int2IntMap deferredCopies = new Int2IntOpenHashMap();
		boolean needsFeedback;
		int nextNewId;

		UniformBufferBlockManager(String group, int[] uniformOffsets, int program, int binding, int index, int type) {
			this.name = group;
			this.bufferType = type;
			this.binding = binding;
			this.bindingIndex = index;
			if(type == GL_UNIFORM_BUFFER) {
				glUniformBlockBinding(program, index, binding);
				this.byteLength = glGetActiveUniformBlocki(program, index, GL_UNIFORM_BLOCK_DATA_SIZE);
				this.paddedByteLength = JMath.ceil(this.byteLength, UBOBuilder.UBO_PADDING);
				this.bind = GLContextState.UNIFORM_BUFFER;
				this.buffer = new UBOBuilder(this.byteLength, this.paddedByteLength, uniformOffsets, 0);
			} else {
				this.byteLength = glGetActiveAtomicCounterBufferi(program, index, GL_ATOMIC_COUNTER_BUFFER_DATA_SIZE);
				this.paddedByteLength = JMath.ceil(this.byteLength, ACBOBuilder.ACBO_PADDING);
				this.bind = GLContextState.ATOMIC_COUNTERS;
				this.buffer = new ACBOBuilder(this.byteLength, this.paddedByteLength, uniformOffsets, 0);
			}

			this.postInit();
			this.deferredCopies.defaultReturnValue(-1);
		}

		public BufferObjectBuilderAccess forIndex(int alloc) {
			synchronized(this.deferredCopies) {
				int from = this.deferredCopies.remove(alloc);
				if(from != -1) {
					this.buffer.copyFrom(this.buffer, from, alloc);
				}
				var iterator = this.deferredCopies.int2IntEntrySet().iterator();
				while(iterator.hasNext()) {
					Int2IntMap.Entry entry = iterator.next();
					if(entry.getIntValue() == alloc) { // from == this
						int key = entry.getIntKey();
						this.buffer.copyFrom(this.buffer, alloc, key);
						iterator.remove();
					}
				}
			}
			return this.buffer.struct(alloc);
		}

		public void addDeferredCopy(int from, int to) {
			synchronized(this.deferredCopies) {
				//this.buffer.copyFrom(to, this.buffer, from, 1, true);
				this.deferredCopies.put(to, from);
			}
		}

		public void bindRange(int alloc) {
			synchronized(this.deferredCopies) {
				int real = this.deferredCopies.getOrDefault(alloc, alloc);
				this.buffer.bind(this.binding, real);
			}
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
								this.buffer.copyFrom(this.buffer, allocated, entry.getIntKey());
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

	static class ShaderBufferBlock {
		final SSBOBuilder builder;
		final int binding;

		ShaderBufferBlock(SSBOBuilder builder, int binding) {
			this.builder = builder;
			this.binding = binding;
		}

		ShaderBufferBlock(ShaderBufferBlock builder) {
			this(new SSBOBuilder(builder.builder), builder.binding);
		}

		public void bind() {
			this.builder.bind(this.binding);
		}
	}

	static class UniformBufferBlock {
		final UniformBufferBlockManager manager;
		final int alloc;
		final int groupIndex;
		final int bufferType;
		final String name;

		public UniformBufferBlock(UniformBufferBlockManager manager, int bufferType) {
			this.manager = manager;
			this.name = manager.name;
			this.alloc = this.manager.allocate();
			this.groupIndex = this.manager.binding;
			this.bufferType = bufferType;
		}

		public UniformBufferBlock(String name, int[] uniformOffsets, int program, int binding, int index, int type) {
			this(new UniformBufferBlockManager(name, uniformOffsets, program, binding, index, type), type);
		}

		public UniformBufferBlock(UniformBufferBlock group, boolean preserveUniforms) {
			this.name = group.name;
			this.manager = group.manager;
			this.alloc = this.manager.allocate();
			this.groupIndex = group.groupIndex;
			this.bufferType = group.bufferType;

			if(preserveUniforms) {
				this.manager.addDeferredCopy(group.alloc, this.alloc);
			}
		}

		public BufferObjectBuilderAccess buffer() { // todo upload partial or something
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
