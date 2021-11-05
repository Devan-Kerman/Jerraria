package net.devtech.jerraria.client.render.shaders;

import static org.lwjgl.opengl.GL40.glGetAttribLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.devtech.jerraria.client.render.shaders.err.IncompatibleVertexFormatException;
import net.devtech.jerraria.client.render.shaders.glsl.ast.declaration.ExternalFieldDeclarationAstNode;
import net.devtech.jerraria.client.render.shaders.types.DataType;

public record VertexFormat(List<Element> elements) {
	public static final Multimap<DataType, String> GL_NAMES = HashMultimap.create();

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

	static void compute(ShaderParser src, int shader, Map<String, Element> elements) {
		List<ShaderParser.Field> fields = src.getFields(ExternalFieldDeclarationAstNode.ExternalFieldType.ATTRIBUTE);
		for(ShaderParser.Field field : fields) {
			find(elements, shader, field.name(), field.glslType(), -1);
		}
		for(Element value : elements.values()) {
			if(value.location == -1) {
				String debug = fields.stream().map(ShaderParser.Field::name).collect(Collectors.joining(", "));
				throw new IncompatibleVertexFormatException("No field found for " + value.name + " in shader! Field names: " + debug);
			}
		}
	}

	private static void find(Map<String, Element> elements, int shader, String fieldName, String elementType, int preComputed) {
		Element element = elements.get(fieldName);
		if(element == null) {
			throw new IncompatibleVertexFormatException("No element for name " + fieldName);
		}

		int location = preComputed == -1 ? glGetAttribLocation(shader, fieldName) : preComputed;

		int counter = 0;
		int loc;
		String name;
		while((loc = glGetAttribLocation(shader, (name = fieldName + '[' + counter + ']'))) != -1) {
			find(elements, shader, name, elementType, loc);
			counter++;
		}

		if(counter == 0) { // not array
			//if(GL_NAMES.)
			// todo check element type compatibility
			element.location = location;
		}
	}

	public static final class Element {
		public final String name;
		public final DataType type;
		int location = -1;

		public Element(String name, DataType type) {
			this.name = name;
			this.type = type;
		}
	}
}
