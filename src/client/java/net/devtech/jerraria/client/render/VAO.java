package net.devtech.jerraria.client.render;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL40.glGetAttribLocation;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.devtech.jerraria.client.render.err.IncompatibleVertexFormatException;
import net.devtech.jerraria.client.render.glsl.ast.declaration.ExternalFieldDeclarationAstNode;
import net.devtech.jerraria.client.render.types.DataType;
import org.lwjgl.opengl.GL30;

public record VAO(QuickAccess quick, Map<String, Element> elements, List<ElementGroup> groups, Element first, int glId) {
	public static final Multimap<DataType, String> GL_NAMES = HashMultimap.create();
	private static final Map<VAO, VAO> CACHE = new HashMap<>();

	static {
		for(DataType value : DataType.values()) {
			GL_NAMES.put(value, value.glslName);
		}
		GL_NAMES.put(DataType.MAT2, "mat2x2");
		GL_NAMES.put(DataType.MAT3, "mat3x3");
		GL_NAMES.put(DataType.MAT4, "mat4x4");

		GL_NAMES.put(DataType.NORMALIZED_F8_MAT2, "mat2x2");
		GL_NAMES.put(DataType.NORMALIZED_F8_MAT3, "mat3x3");
		GL_NAMES.put(DataType.NORMALIZED_F8_MAT4, "mat4x4");

		GL_NAMES.put(DataType.NORMALIZED_F16_MAT2, "mat2x2");
		GL_NAMES.put(DataType.NORMALIZED_F16_MAT3, "mat3x3");
		GL_NAMES.put(DataType.NORMALIZED_F16_MAT4, "mat4x4");

		GL_NAMES.put(DataType.NORMALIZED_F32_MAT2, "mat2x2");
		GL_NAMES.put(DataType.NORMALIZED_F32_MAT3, "mat3x3");
		GL_NAMES.put(DataType.NORMALIZED_F32_MAT4, "mat4x4");
	}

	VAO(Map<String, Field> fields, ShaderParser code, int program) {
		this(null, initializeLocations(fields, code, program), null, null,-1);
	}

	VAO(VAO elements) {
		this(new QuickAccess(elements.elements),
			elements.elements,
			elements.elements.values().stream().map(Element::group).distinct().toList(),
			elements.elements.values().iterator().next(),
			bindVAO());

		for(Element value : this.elements.values()) {
			value.group.bind();
			DataType type = value.type;
			GL30.glVertexAttribPointer(value.location,
				type.elementCount,
				type.elementType,
				type.normalized,
				type.byteCount,
				value.byteOffset);
			GL30.glEnableVertexAttribArray(value.location);
		}
	}

	public static VAO getOrCreate(Map<String, Field> fields, ShaderParser source, int program) {
		VAO vao = new VAO(fields, source, program);
		return CACHE.computeIfAbsent(vao, VAO::new);
	}

	public static VAO getOrCreateDefaultGroup(Map<String, DataType> types, ShaderParser source, int program) {
		Map<String, Field> fields = new HashMap<>();
		types.forEach((name, type) -> fields.put(name, new Field(type, name, false)));
		VAO vao = new VAO(fields, source, program);
		return CACHE.computeIfAbsent(vao, VAO::new);
	}

	public VAO createNew() {
		return new VAO(this);
	}

	public VAO start() {
		for(ElementGroup group : this.groups) {
			group.buffer.vertexCount = 0;
		}
		return this;
	}

	public ByteBuffer element(Element element) {
		BufferBuilder buffer = element.group.buffer;
		ByteBuffer byteBuf = buffer.buffer;
		byteBuf.position(buffer.vertexOffset() + element.byteOffset);
		return byteBuf;
	}

	public VAO next() {
		for(ElementGroup group : this.groups) {
			group.buffer.next();
			group.reupload = true;
		}
		return this;
	}

	public void bind() {
		GL30.glBindVertexArray(this.glId);
	}

	public void bindAndDraw(int mode) {
		this.bind();
		this.drawArray(mode);
	}

	public void drawArray(int mode) {
		for(ElementGroup group : this.groups) {
			group.upload();
		}
		glDrawArrays(mode, 0, this.first.group.buffer.vertexCount);
	}

	static int bindVAO() {
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		return vao;
	}

	static Map<String, Element> initializeLocations(Map<String, Field> fields, ShaderParser src, int program) {
		List<ShaderParser.Field> inputs = src.getFields(ExternalFieldDeclarationAstNode.ExternalFieldType.ATTRIBUTE);
		ImmutableMap.Builder<String, Element> elements = ImmutableMap.builder();
		Map<String, ElementGroup> groups = new HashMap<>();
		for(ShaderParser.Field field : inputs) {
			find(groups, fields, elements, program, field.name(), field.glslType(), -1);
		}
		ImmutableMap<String, Element> built = elements.build();
		for(Field value : fields.values()) {
			if(!built.containsKey(value.name)) {
				String debug = inputs.stream().map(ShaderParser.Field::name).collect(Collectors.joining(", "));
				throw new IncompatibleVertexFormatException("No field found for " + value.name + " in shader! Field " + "names: " + debug);
			}
		}

		for(ElementGroup value : groups.values()) {
			value.buffer = new BufferBuilder(value.len);
		}
		return built;
	}

	static void find(
		Map<String, ElementGroup> groups,
		Map<String, Field> fields,
		ImmutableMap.Builder<String, Element> elements,
		int shader,
		String fieldName,
		String elementType,
		int preComputed) {

		Field field = fields.get(fieldName);
		if(field == null) {
			throw new IncompatibleVertexFormatException("No element for name " + fieldName);
		}

		int location = preComputed == -1 ? glGetAttribLocation(shader, fieldName) : preComputed;

		int counter = 0;
		int loc;
		String name;
		while((loc = glGetAttribLocation(shader, (name = fieldName + '[' + counter + ']'))) != -1) {
			find(groups, fields, elements, shader, name, elementType, loc);
			counter++;
		}

		if(counter == 0) { // not array
			if(!GL_NAMES.containsEntry(field.type, elementType)) {
				throw new IncompatibleVertexFormatException("Incompatible Vertex Attribute Type: requested " + field.type.glslName + " found " + elementType);
			}
			ElementGroup group = groups.computeIfAbsent(field.groupName, ElementGroup::new);
			var element = new Element(group, field.name, field.type, location, group.len);
			elements.put(field.name, element);
			group.len += element.type.byteCount;
		}
	}

	public record QuickAccess(Element color, Element pos) {
		public QuickAccess(Map<String, Element> element) {
			this(element.get("color"), element.get("pos"));
		}
	}

	final static class ElementGroup {
		BufferBuilder buffer;
		boolean reupload;
		int glId;
		int len;

		public ElementGroup(String name) {
		}

		void upload() {
			if(this.reupload) {
				this.bind();
				this.buffer.upload();
				this.reupload = false;
			}
		}

		void bind() {
			if(glId == 0) {
				glId = GL30.glGenBuffers();
			}
			GL30.glBindBuffer(GL_ARRAY_BUFFER, glId);
		}
	}

	public record Element(ElementGroup group, String name, DataType type, int location, int byteOffset) {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Element e && e.type == type && e.location == location && name.equals(e.name);
		}

		@Override
		public int hashCode() {
			int result = this.name.hashCode();
			result = 31 * result + this.type.hashCode();
			result = 31 * result + this.location;
			return result;
		}
	}

	public record Field(DataType type, String name, String groupName) {
		public Field(DataType type, String name, boolean isUniform) {
			this(type, name, isUniform ? name + "_" : "default");
		}
	}
}
