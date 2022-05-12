package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL31.*;

public enum DataType {
	BOOL(GL_BOOL, 1, "bool"),
	I8(GL_BYTE, 1, "int"),
	U8(GL_UNSIGNED_BYTE, 1, "uint"),
	I16(GL_SHORT, 2, "int"),
	U16(GL_UNSIGNED_SHORT, 2, "uint"),
	I32(GL_INT, 4, "int"),
	U32(GL_UNSIGNED_INT, 4, "int"),

	F32(GL_FLOAT, 4, "float"),
	F64(GL_DOUBLE, 8, "double"),
	NORMALIZED_F8(GL_UNSIGNED_BYTE, 1, true, "float"),
	NORMALIZED_F16(GL_UNSIGNED_SHORT, 2, true, "float"),
	NORMALIZED_F32(GL_UNSIGNED_INT, 4, true, "float"),

	BOOL_VEC2(BOOL, 2, "bvec2"),
	I8_VEC2(I8, 2, "ivec2"),
	U8_VEC2(U8, 2, "uvec2"),
	I16_VEC2(I16, 2, "ivec2"),
	U16_VEC2(U16, 2, "uvec2"),
	I32_VEC2(I32, 2, "ivec2"),
	U32_VEC2(U32, 2, "uvec2"),
	F32_VEC2(F32, 2, "vec2"),
	F64_VEC2(F64, 2, "dvec2"),
	NORMALIZED_F8_VEC2(NORMALIZED_F8, 2, "vec2"),
	NORMALIZED_F16_VEC2(NORMALIZED_F16, 2, "vec2"),
	NORMALIZED_F32_VEC2(NORMALIZED_F32, 2, "vec2"),

	BOOL_VEC3(BOOL, 3, "bvec3"),
	I8_VEC3(I8, 3, "ivec3"),
	U8_VEC3(U8, 3, "uvec3"),
	I16_VEC3(I16, 3, "ivec3"),
	U16_VEC3(U16, 3, "uvec3"),
	I32_VEC3(I32, 3, "ivec3"),
	U32_VEC3(U32, 3, "uvec3"),
	F32_VEC3(F32, 3, "vec3"),
	F64_VEC3(F64, 3, "dvec3"),
	NORMALIZED_F8_VEC3(NORMALIZED_F8, 3, "vec3"),
	NORMALIZED_F16_VEC3(NORMALIZED_F16, 3, "vec3"),
	NORMALIZED_F32_VEC3(NORMALIZED_F32, 3, "vec3"),

	BOOL_VEC4(BOOL, 4, "bvec4"),
	I8_VEC4(I8, 4, "ivec4"),
	U8_VEC4(U8, 4, "uvec4"),
	I16_VEC4(I16, 4, "ivec4"),
	U16_VEC4(U16, 4, "uvec4"),
	I32_VEC4(I32, 4, "ivec4"),
	U32_VEC4(U32, 4, "uvec4"),
	F32_VEC4(F32, 4, "vec4"),
	F64_VEC4(F64, 4, "dvec4"),
	NORMALIZED_F8_VEC4(NORMALIZED_F8, 4, "vec4"),
	NORMALIZED_F16_VEC4(NORMALIZED_F16, 4, "vec4"),
	NORMALIZED_F32_VEC4(NORMALIZED_F32, 4, "vec4"),

	MAT2(F32, 4, "mat2"),
	MAT2x3(F32, 6, "mat2x3"),
	MAT2x4(F32, 8, "mat2x4"),
	MAT3(F32, 9, "mat3"),
	MAT3x2(F32, 6, "mat3x2"),
	MAT3x4(F32, 12, "mat3x4"),
	MAT4(F32, 16, "mat4"),
	MAT4x3(F32, 12, "mat4x3"),
	MAT4x2(F32, 8, "mat4x2"),

	DMAT2(F64, 4, "dmat2"),
	DMAT2x3(F64, 6, "dmat2x3"),
	DMAT2x4(F64, 8, "dmat2x4"),
	DMAT3(F64, 9, "dmat3"),
	DMAT3x2(F64, 6, "dmat3x2"),
	DMAT3x4(F64, 12, "dmat3x4"),
	DMAT4(F64, 16, "dmat4"),
	DMAT4x3(F64, 12, "dmat4x3"),
	DMAT4x2(F64, 8, "dmat4x2"),

	NORMALIZED_F8_MAT2(NORMALIZED_F8, 4, "mat2"),
	NORMALIZED_F8_MAT2x3(NORMALIZED_F8, 6, "mat2x3"),
	NORMALIZED_F8_MAT2x4(NORMALIZED_F8, 8, "mat2x4"),
	NORMALIZED_F8_MAT3(NORMALIZED_F8, 9, "mat3"),
	NORMALIZED_F8_MAT3x2(NORMALIZED_F8, 6, "mat3x2"),
	NORMALIZED_F8_MAT3x4(NORMALIZED_F8, 12, "mat3x4"),
	NORMALIZED_F8_MAT4(NORMALIZED_F8, 16, "mat4"),
	NORMALIZED_F8_MAT4x3(NORMALIZED_F8, 12, "mat4x3"),
	NORMALIZED_F8_MAT4x2(NORMALIZED_F8, 8, "mat4x2"),

	NORMALIZED_F16_MAT2(NORMALIZED_F16, 4, "mat2"),
	NORMALIZED_F16_MAT2x3(NORMALIZED_F16, 6, "mat2x3"),
	NORMALIZED_F16_MAT2x4(NORMALIZED_F16, 8, "mat2x4"),
	NORMALIZED_F16_MAT3(NORMALIZED_F16, 9, "mat3"),
	NORMALIZED_F16_MAT3x2(NORMALIZED_F16, 6, "mat3x2"),
	NORMALIZED_F16_MAT3x4(NORMALIZED_F16, 12, "mat3x4"),
	NORMALIZED_F16_MAT4(NORMALIZED_F16, 16, "mat4"),
	NORMALIZED_F16_MAT4x3(NORMALIZED_F16, 12, "mat4x3"),
	NORMALIZED_F16_MAT4x2(NORMALIZED_F16, 8, "mat4x2"),

	NORMALIZED_F32_MAT2(NORMALIZED_F32, 4, "mat2"),
	NORMALIZED_F32_MAT2x3(NORMALIZED_F32, 6, "mat2x3"),
	NORMALIZED_F32_MAT2x4(NORMALIZED_F32, 8, "mat2x4"),
	NORMALIZED_F32_MAT3(NORMALIZED_F32, 9, "mat3"),
	NORMALIZED_F32_MAT3x2(NORMALIZED_F32, 6, "mat3x2"),
	NORMALIZED_F32_MAT3x4(NORMALIZED_F32, 12, "mat3x4"),
	NORMALIZED_F32_MAT4(NORMALIZED_F32, 16, "mat4"),
	NORMALIZED_F32_MAT4x3(NORMALIZED_F32, 12, "mat4x3"),
	NORMALIZED_F32_MAT4x2(NORMALIZED_F32, 8, "mat4x2"),

	TEXTURE_1D(GL_TEXTURE_1D, 4, false, "sampler1D"),
	TEXTURE_2D(GL_TEXTURE_2D, 4, false, "sampler2D"),
	TEXTURE_3D(GL_TEXTURE_3D, 4, false, "sampler3D");

	public final String glslName;
	public final boolean normalized;
	public final int byteCount, elementCount, elementType;
	public final boolean isMatrix, isSampler;


	DataType(int glslType, int byteCount, boolean normalized, String name) {
		this(name, normalized, byteCount, 1, glslType);
	}

	DataType(DataType type, int elementCount, String name) {
		this(name, type.normalized, type.byteCount * elementCount, elementCount, type.elementType);
	}

	DataType(int glslType, int byteCount, String name) {
		this(name, false, byteCount, 1, glslType);
	}

	DataType(String name, boolean normalized, int byteCount, int elementCount, int elementType) {
		this.glslName = name;
		this.normalized = normalized;
		this.byteCount = byteCount;
		this.elementCount = elementCount;
		this.elementType = elementType;
		this.isMatrix = this.name().contains("MAT");
		this.isSampler = name.contains("sampler");
	}

	public boolean isFloating() {
		return this.elementType == GL_DOUBLE || this.elementType == GL_FLOAT || this.normalized;
	}

}
