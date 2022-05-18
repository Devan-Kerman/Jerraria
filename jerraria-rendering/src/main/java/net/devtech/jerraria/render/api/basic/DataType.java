package net.devtech.jerraria.render.api.basic;

import static org.lwjgl.opengl.GL30.*;

import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.lwjgl.opengl.GL40;

public enum DataType {
	BOOL(GL_BOOL, 1, "bool", GL_BOOL),
	I8(GL_BYTE, 1, "int", GL_INT),
	U8(GL_UNSIGNED_BYTE, 1, "uint", GL_UNSIGNED_INT),
	I16(GL_SHORT, 2, "int", GL_INT),
	U16(GL_UNSIGNED_SHORT, 2, "uint", GL_UNSIGNED_INT),
	I32(GL_INT, 4, "int", GL_INT),
	U32(GL_UNSIGNED_INT, 4, "int", GL_UNSIGNED_INT),

	F32(GL_FLOAT, 4, "float", GL_FLOAT),
	F64(GL_DOUBLE, 8, "double", GL_DOUBLE),
	NORMALIZED_F8(GL_UNSIGNED_BYTE, 1, true, "float", GL_FLOAT),
	NORMALIZED_F16(GL_UNSIGNED_SHORT, 2, true, "float", GL_FLOAT),
	NORMALIZED_F32(GL_UNSIGNED_INT, 4, true, "float", GL_FLOAT),

	BOOL_VEC2(BOOL, 2, "bvec2", GL_BOOL_VEC2),
	I8_VEC2(I8, 2, "ivec2", GL_INT_VEC2),
	U8_VEC2(U8, 2, "uvec2", GL_UNSIGNED_INT_VEC2),
	I16_VEC2(I16, 2, "ivec2", GL_INT_VEC2),
	U16_VEC2(U16, 2, "uvec2", GL_UNSIGNED_INT_VEC2),
	I32_VEC2(I32, 2, "ivec2", GL_INT_VEC2),
	U32_VEC2(U32, 2, "uvec2", GL_UNSIGNED_INT_VEC2),
	F32_VEC2(F32, 2, "vec2", GL_FLOAT_VEC2),
	F64_VEC2(F64, 2, "dvec2", GL40.GL_DOUBLE_VEC2),
	NORMALIZED_F8_VEC2(NORMALIZED_F8, 2, "vec2", GL_FLOAT_VEC2),
	NORMALIZED_F16_VEC2(NORMALIZED_F16, 2, "vec2", GL_FLOAT_VEC2),
	NORMALIZED_F32_VEC2(NORMALIZED_F32, 2, "vec2", GL_FLOAT_VEC2),

	BOOL_VEC3(BOOL, 3, "bvec3", GL_BOOL_VEC3),
	I8_VEC3(I8, 3, "ivec3", GL_INT_VEC3),
	U8_VEC3(U8, 3, "uvec3", GL_UNSIGNED_INT_VEC3),
	I16_VEC3(I16, 3, "ivec3", GL_INT_VEC3),
	U16_VEC3(U16, 3, "uvec3", GL_UNSIGNED_INT_VEC3),
	I32_VEC3(I32, 3, "ivec3", GL_INT_VEC3),
	U32_VEC3(U32, 3, "uvec3", GL_UNSIGNED_INT_VEC3),
	F32_VEC3(F32, 3, "vec3", GL_FLOAT_VEC3),
	F64_VEC3(F64, 3, "dvec3", GL40.GL_DOUBLE_VEC3),
	NORMALIZED_F8_VEC3(NORMALIZED_F8, 3, "vec3", GL_FLOAT_VEC3),
	NORMALIZED_F16_VEC3(NORMALIZED_F16, 3, "vec3", GL_FLOAT_VEC3),
	NORMALIZED_F32_VEC3(NORMALIZED_F32, 3, "vec3", GL_FLOAT_VEC3),

	BOOL_VEC4(BOOL, 4, "bvec4", GL_BOOL_VEC4),
	I8_VEC4(I8, 4, "ivec4", GL_INT_VEC4),
	U8_VEC4(U8, 4, "uvec4", GL_UNSIGNED_INT_VEC4),
	I16_VEC4(I16, 4, "ivec4", GL_INT_VEC4),
	U16_VEC4(U16, 4, "uvec4", GL_UNSIGNED_INT_VEC4),
	I32_VEC4(I32, 4, "ivec4", GL_INT_VEC4),
	U32_VEC4(U32, 4, "uvec4", GL_UNSIGNED_INT_VEC4),
	F32_VEC4(F32, 4, "vec4", GL_FLOAT_VEC4),
	F64_VEC4(F64, 4, "dvec4", GL40.GL_DOUBLE_VEC4),
	NORMALIZED_F8_VEC4(NORMALIZED_F8, 4, "vec4", GL_FLOAT_VEC4),
	NORMALIZED_F16_VEC4(NORMALIZED_F16, 4, "vec4", GL_FLOAT_VEC4),
	NORMALIZED_F32_VEC4(NORMALIZED_F32, 4, "vec4", GL_FLOAT_VEC4),

	MAT2(F32, 4, "mat2", GL_FLOAT_MAT2),
	MAT2x3(F32, 6, "mat2x3", GL_FLOAT_MAT2x3),
	MAT2x4(F32, 8, "mat2x4", GL_FLOAT_MAT2x4),
	MAT3(F32, 9, "mat3", GL_FLOAT_MAT3),
	MAT3x2(F32, 6, "mat3x2", GL_FLOAT_MAT3x2),
	MAT3x4(F32, 12, "mat3x4", GL_FLOAT_MAT3x4),
	MAT4(F32, 16, "mat4", GL_FLOAT_MAT4),
	MAT4x3(F32, 12, "mat4x3", GL_FLOAT_MAT4x3),
	MAT4x2(F32, 8, "mat4x2", GL_FLOAT_MAT4x2),

	DMAT2(F64, 4, "dmat2", GL40.GL_DOUBLE_MAT2),
	DMAT2x3(F64, 6, "dmat2x3", GL40.GL_DOUBLE_MAT2x3),
	DMAT2x4(F64, 8, "dmat2x4", GL40.GL_DOUBLE_MAT2x4),
	DMAT3(F64, 9, "dmat3", GL40.GL_DOUBLE_MAT3),
	DMAT3x2(F64, 6, "dmat3x2", GL40.GL_DOUBLE_MAT3x2),
	DMAT3x4(F64, 12, "dmat3x4", GL40.GL_DOUBLE_MAT3x4),
	DMAT4(F64, 16, "dmat4", GL40.GL_DOUBLE_MAT4),
	DMAT4x3(F64, 12, "dmat4x3", GL40.GL_DOUBLE_MAT4x3),
	DMAT4x2(F64, 8, "dmat4x2", GL40.GL_DOUBLE_MAT4x2),

	NORMALIZED_F8_MAT2(NORMALIZED_F8, 4, "mat2", GL_FLOAT_MAT2),
	NORMALIZED_F8_MAT2x3(NORMALIZED_F8, 6, "mat2x3", GL_FLOAT_MAT2x3),
	NORMALIZED_F8_MAT2x4(NORMALIZED_F8, 8, "mat2x4", GL_FLOAT_MAT2x4),
	NORMALIZED_F8_MAT3(NORMALIZED_F8, 9, "mat3", GL_FLOAT_MAT3),
	NORMALIZED_F8_MAT3x2(NORMALIZED_F8, 6, "mat3x2", GL_FLOAT_MAT3x2),
	NORMALIZED_F8_MAT3x4(NORMALIZED_F8, 12, "mat3x4", GL_FLOAT_MAT3x4),
	NORMALIZED_F8_MAT4(NORMALIZED_F8, 16, "mat4", GL_FLOAT_MAT4),
	NORMALIZED_F8_MAT4x3(NORMALIZED_F8, 12, "mat4x3", GL_FLOAT_MAT4x3),
	NORMALIZED_F8_MAT4x2(NORMALIZED_F8, 8, "mat4x2", GL_FLOAT_MAT4x2),

	NORMALIZED_F16_MAT2(NORMALIZED_F16, 4, "mat2", GL_FLOAT_MAT2),
	NORMALIZED_F16_MAT2x3(NORMALIZED_F16, 6, "mat2x3", GL_FLOAT_MAT2x3),
	NORMALIZED_F16_MAT2x4(NORMALIZED_F16, 8, "mat2x4", GL_FLOAT_MAT2x4),
	NORMALIZED_F16_MAT3(NORMALIZED_F16, 9, "mat3", GL_FLOAT_MAT3),
	NORMALIZED_F16_MAT3x2(NORMALIZED_F16, 6, "mat3x2", GL_FLOAT_MAT3x2),
	NORMALIZED_F16_MAT3x4(NORMALIZED_F16, 12, "mat3x4", GL_FLOAT_MAT3x4),
	NORMALIZED_F16_MAT4(NORMALIZED_F16, 16, "mat4", GL_FLOAT_MAT4),
	NORMALIZED_F16_MAT4x3(NORMALIZED_F16, 12, "mat4x3", GL_FLOAT_MAT4x3),
	NORMALIZED_F16_MAT4x2(NORMALIZED_F16, 8, "mat4x2", GL_FLOAT_MAT4x2),

	NORMALIZED_F32_MAT2(NORMALIZED_F32, 4, "mat2", GL_FLOAT_MAT2),
	NORMALIZED_F32_MAT2x3(NORMALIZED_F32, 6, "mat2x3", GL_FLOAT_MAT2x3),
	NORMALIZED_F32_MAT2x4(NORMALIZED_F32, 8, "mat2x4", GL_FLOAT_MAT2x4),
	NORMALIZED_F32_MAT3(NORMALIZED_F32, 9, "mat3", GL_FLOAT_MAT3),
	NORMALIZED_F32_MAT3x2(NORMALIZED_F32, 6, "mat3x2", GL_FLOAT_MAT3x2),
	NORMALIZED_F32_MAT3x4(NORMALIZED_F32, 12, "mat3x4", GL_FLOAT_MAT3x4),
	NORMALIZED_F32_MAT4(NORMALIZED_F32, 16, "mat4", GL_FLOAT_MAT4),
	NORMALIZED_F32_MAT4x3(NORMALIZED_F32, 12, "mat4x3", GL_FLOAT_MAT4x3),
	NORMALIZED_F32_MAT4x2(NORMALIZED_F32, 8, "mat4x2", GL_FLOAT_MAT4x2),

	TEXTURE_1D(GL_TEXTURE_1D, 4, false, "sampler1D", true, GL_SAMPLER_1D),
	TEXTURE_2D(GL_TEXTURE_2D, 4, false, "sampler2D", true, GL_SAMPLER_2D),
	TEXTURE_3D(GL_TEXTURE_3D, 4, false, "sampler3D", true, GL_SAMPLER_3D);

	// todo the rest of https://www.khronos.org/registry/OpenGL-Refpages/gl4/html/glGetActiveUniform.xhtml
	static final Int2ObjectMap<Set<DataType>> COMPATIBLE_DATA_TYPES = new Int2ObjectOpenHashMap<>();

	public final String glslName;
	public final boolean normalized;
	public final int byteCount, elementCount, elementType, glslType;
	public final boolean isMatrix, isSampler, uniformOnly;

	DataType(int elementType, int byteCount, boolean normalized, String name, boolean uniformOnly, int glslType) {
		this(name, normalized, byteCount, 1, elementType, uniformOnly, glslType);
	}

	DataType(int elementType, int byteCount, boolean normalized, String name, int glslType) {
		this(name, normalized, byteCount, 1, elementType, false, glslType);
	}

	DataType(DataType type, int elementCount, String name, int glslType) {
		this(name, type.normalized, type.byteCount * elementCount, elementCount, type.elementType, false, glslType);
	}

	DataType(int elementType, int byteCount, String name, int glslType) {
		this(name, false, byteCount, 1, elementType, false, glslType);
	}

	DataType(String name, boolean normalized, int byteCount, int elementCount, int elementType, boolean uniformOnly, int glslType) {
		this.glslName = name;
		this.normalized = normalized;
		this.byteCount = byteCount;
		this.elementCount = elementCount;
		this.elementType = elementType;
		this.isMatrix = this.name().contains("MAT");
		this.isSampler = name.contains("sampler");
		this.uniformOnly = uniformOnly;
		this.glslType = glslType;

	}

	static {
		for(DataType value : DataType.values()) {
			COMPATIBLE_DATA_TYPES.computeIfAbsent(value.glslType, i -> new HashSet<>()).add(value);
		}
	}

	public boolean isFloating() {
		return this.elementType == GL_DOUBLE || this.elementType == GL_FLOAT || this.normalized;
	}

	public boolean isCompatible(int glslType) {
		return this.glslType == glslType;
	}

	public static Set<DataType> forGlslType(int type) {
		return COMPATIBLE_DATA_TYPES.get(type);
	}
}
