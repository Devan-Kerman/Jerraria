package net.devtech.jerraria.render.api.base;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL43.*;

import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.render.api.OpenGLSupport;

/**
 * All supported OpenGL types
 */
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
	F64_VEC2(F64, 2, "dvec2", GL_DOUBLE_VEC2),
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
	F64_VEC3(F64, 3, "dvec3", GL_DOUBLE_VEC3),
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
	F64_VEC4(F64, 4, "dvec4", GL_DOUBLE_VEC4),
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

	DMAT2(F64, 4, "dmat2", GL_DOUBLE_MAT2),
	DMAT2x3(F64, 6, "dmat2x3", GL_DOUBLE_MAT2x3),
	DMAT2x4(F64, 8, "dmat2x4", GL_DOUBLE_MAT2x4),
	DMAT3(F64, 9, "dmat3", GL_DOUBLE_MAT3),
	DMAT3x2(F64, 6, "dmat3x2", GL_DOUBLE_MAT3x2),
	DMAT3x4(F64, 12, "dmat3x4", GL_DOUBLE_MAT3x4),
	DMAT4(F64, 16, "dmat4", GL_DOUBLE_MAT4),
	DMAT4x3(F64, 12, "dmat4x3", GL_DOUBLE_MAT4x3),
	DMAT4x2(F64, 8, "dmat4x2", GL_DOUBLE_MAT4x2),

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

	// todo validation for images readonly/writeonly?

	// floats
	TEXTURE_1D(GL_TEXTURE_1D, "sampler1D", GL_SAMPLER_1D),
	TEXTURE_2D(GL_TEXTURE_2D, "sampler2D", GL_SAMPLER_2D),
	TEXTURE_3D(GL_TEXTURE_3D, "sampler3D", GL_SAMPLER_3D),
	TEXTURE_CUBE(GL_TEXTURE_CUBE_MAP, "samplerCube", GL_SAMPLER_CUBE),
	TEXTURE_RECTANGLE(GL_TEXTURE_RECTANGLE, "sampler2dRect", GL_SAMPLER_2D_RECT),
	TEXTURE_1D_ARRAY(GL_TEXTURE_1D_ARRAY, "sampler1DArray", GL_SAMPLER_1D_ARRAY),
	TEXTURE_2D_ARRAY(GL_TEXTURE_2D_ARRAY, "sampler2DArray", GL_SAMPLER_2D_ARRAY),
	TEXTURE_CUBE_MAP_ARRAY(GL_TEXTURE_CUBE_MAP_ARRAY, "samplerCubeArray", GL_SAMPLER_CUBE_MAP_ARRAY),
	TEXTURE_BUFFER(GL_TEXTURE_BUFFER, "samplerBuffer", GL_SAMPLER_BUFFER),
	TEXTURE_2D_MULTISAMPLE(GL_TEXTURE_2D_MULTISAMPLE, "sampler2DMS", GL_SAMPLER_2D_MULTISAMPLE),
	TEXTURE_2D_MULTISAMPLE_ARRAY(GL_TEXTURE_2D_MULTISAMPLE_ARRAY, "sampler2DMSArray", GL_SAMPLER_2D_MULTISAMPLE_ARRAY),

	IMAGE_1D(TEXTURE_1D, "image1D", GL_IMAGE_1D),
	IMAGE_2D(TEXTURE_2D, "image2D", GL_IMAGE_2D),
	IMAGE_3D(TEXTURE_3D, "image3D", GL_IMAGE_3D),
	IMAGE_CUBE(TEXTURE_CUBE, "imageCube", GL_IMAGE_CUBE),
	IMAGE_RECTANGLE(TEXTURE_RECTANGLE, "image2dRect", GL_IMAGE_2D_RECT),
	IMAGE_1D_ARRAY(TEXTURE_1D_ARRAY, "image1DArray", GL_IMAGE_1D_ARRAY),
	IMAGE_2D_ARRAY(TEXTURE_2D_ARRAY, "image2DArray", GL_IMAGE_2D_ARRAY),
	IMAGE_CUBE_MAP_ARRAY(TEXTURE_CUBE_MAP_ARRAY, "imageCubeArray", GL_IMAGE_CUBE_MAP_ARRAY),
	IMAGE_BUFFER(TEXTURE_BUFFER, "imageBuffer", GL_IMAGE_BUFFER),
	IMAGE_2D_MULTISAMPLE(TEXTURE_2D_MULTISAMPLE, "image2DMS", GL_IMAGE_2D_MULTISAMPLE),
	IMAGE_2D_MULTISAMPLE_ARRAY(TEXTURE_2D_MULTISAMPLE_ARRAY, "image2DMSArray", GL_IMAGE_2D_MULTISAMPLE_ARRAY),

	READONLY_IMAGE_1D(IMAGE_1D, "readonlyImage1D"),
	READONLY_IMAGE_2D(IMAGE_2D, "readonlyImage2D"),
	READONLY_IMAGE_3D(IMAGE_3D, "readonlyImage3D"),
	READONLY_IMAGE_CUBE(IMAGE_CUBE, "readonlyImageCube"),
	READONLY_IMAGE_RECTANGLE(IMAGE_RECTANGLE, "readonlyImage2dRect"),
	READONLY_IMAGE_1D_ARRAY(IMAGE_1D_ARRAY, "readonlyImage1DArray"),
	READONLY_IMAGE_2D_ARRAY(IMAGE_2D_ARRAY, "readonlyImage2DArray"),
	READONLY_IMAGE_CUBE_MAP_ARRAY(IMAGE_CUBE_MAP_ARRAY, "readonlyImageCubeArray"),
	READONLY_IMAGE_BUFFER(IMAGE_BUFFER, "readonlyImageBuffer"),
	READONLY_IMAGE_2D_MULTISAMPLE(IMAGE_2D_MULTISAMPLE, "readonlyImage2DMS"),
	READONLY_IMAGE_2D_MULTISAMPLE_ARRAY(IMAGE_2D_MULTISAMPLE_ARRAY, "readonlyImage2DMSArray"),

	WRITEONLY_IMAGE_1D(IMAGE_1D, "writeonlyImage1D"),
	WRITEONLY_IMAGE_2D(IMAGE_2D, "writeonlyImage2D"),
	WRITEONLY_IMAGE_3D(IMAGE_3D, "writeonlyImage3D"),
	WRITEONLY_IMAGE_CUBE(IMAGE_CUBE, "writeonlyImageCube"),
	WRITEONLY_IMAGE_RECTANGLE(IMAGE_RECTANGLE, "writeonlyImage2dRect"),
	WRITEONLY_IMAGE_1D_ARRAY(IMAGE_1D_ARRAY, "writeonlyImage1DArray"),
	WRITEONLY_IMAGE_2D_ARRAY(IMAGE_2D_ARRAY, "writeonlyImage2DArray"),
	WRITEONLY_IMAGE_CUBE_MAP_ARRAY(IMAGE_CUBE_MAP_ARRAY, "writeonlyImageCubeArray"),
	WRITEONLY_IMAGE_BUFFER(IMAGE_BUFFER, "writeonlyImageBuffer"),
	WRITEONLY_IMAGE_2D_MULTISAMPLE(IMAGE_2D_MULTISAMPLE, "writeonlyImage2DMS"),
	WRITEONLY_IMAGE_2D_MULTISAMPLE_ARRAY(IMAGE_2D_MULTISAMPLE_ARRAY, "writeonlyImage2DMSArray"),

	// unsigned ints
	UINT_TEXTURE_1D(GL_TEXTURE_1D, "usampler1D", GL_UNSIGNED_INT_SAMPLER_1D),
	UINT_TEXTURE_2D(GL_TEXTURE_2D, "usampler2D", GL_UNSIGNED_INT_SAMPLER_2D),
	UINT_TEXTURE_3D(GL_TEXTURE_3D, "usampler3D", GL_UNSIGNED_INT_SAMPLER_3D),
	UINT_TEXTURE_CUBE(GL_TEXTURE_CUBE_MAP, "usamplerCube", GL_UNSIGNED_INT_SAMPLER_CUBE),
	UINT_TEXTURE_RECTANGLE(GL_TEXTURE_RECTANGLE, "usampler2dRect", GL_UNSIGNED_INT_SAMPLER_2D_RECT),
	UINT_TEXTURE_1D_ARRAY(GL_TEXTURE_1D_ARRAY, "usampler1DArray", GL_UNSIGNED_INT_SAMPLER_1D_ARRAY),
	UINT_TEXTURE_2D_ARRAY(GL_TEXTURE_2D_ARRAY, "usampler2DArray", GL_UNSIGNED_INT_SAMPLER_2D_ARRAY),
	UINT_TEXTURE_CUBE_MAP_ARRAY(GL_TEXTURE_CUBE_MAP_ARRAY, "usamplerCubeArray",
		GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY),
	UINT_TEXTURE_BUFFER(GL_TEXTURE_BUFFER, "usamplerBuffer", GL_UNSIGNED_INT_SAMPLER_BUFFER),
	UINT_TEXTURE_2D_MULTISAMPLE(GL_TEXTURE_2D_MULTISAMPLE, "usampler2DMS", GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE),
	UINT_TEXTURE_2D_MULTISAMPLE_ARRAY(GL_TEXTURE_2D_MULTISAMPLE_ARRAY,
		"usampler2DMSArray",
		GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY
	),

	UINT_IMAGE_1D(UINT_TEXTURE_1D, "uimage1D", GL_UNSIGNED_INT_IMAGE_1D),
	UINT_IMAGE_2D(UINT_TEXTURE_2D, "uimage2D", GL_UNSIGNED_INT_IMAGE_2D),
	UINT_IMAGE_3D(UINT_TEXTURE_3D, "uimage3D", GL_UNSIGNED_INT_IMAGE_3D),
	UINT_IMAGE_CUBE(UINT_TEXTURE_CUBE, "uimageCube", GL_UNSIGNED_INT_IMAGE_CUBE),
	UINT_IMAGE_RECTANGLE(UINT_TEXTURE_RECTANGLE, "uimage2dRect", GL_UNSIGNED_INT_IMAGE_2D_RECT),
	UINT_IMAGE_1D_ARRAY(UINT_TEXTURE_1D_ARRAY, "uimage1DArray", GL_UNSIGNED_INT_IMAGE_1D_ARRAY),
	UINT_IMAGE_2D_ARRAY(UINT_TEXTURE_2D_ARRAY, "uimage2DArray", GL_UNSIGNED_INT_IMAGE_2D_ARRAY),
	UINT_IMAGE_CUBE_MAP_ARRAY(UINT_TEXTURE_CUBE_MAP_ARRAY, "uimageCubeArray", GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY),
	UINT_IMAGE_BUFFER(UINT_TEXTURE_BUFFER, "uimageBuffer", GL_UNSIGNED_INT_IMAGE_BUFFER),
	UINT_IMAGE_2D_MULTISAMPLE(UINT_TEXTURE_2D_MULTISAMPLE, "uimage2DMS", GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE),
	UINT_IMAGE_2D_MULTISAMPLE_ARRAY(UINT_TEXTURE_2D_MULTISAMPLE_ARRAY,
		"uimage2DMSArray",
		GL_UNSIGNED_INT_IMAGE_2D_MULTISAMPLE_ARRAY
	),

	READONLY_UINT_IMAGE_1D(UINT_IMAGE_1D, "readonlyuImage1D"),
	READONLY_UINT_IMAGE_2D(UINT_IMAGE_2D, "readonlyuImage2D"),
	READONLY_UINT_IMAGE_3D(UINT_IMAGE_3D, "readonlyuImage3D"),
	READONLY_UINT_IMAGE_CUBE(UINT_IMAGE_CUBE, "readonlyuImageCube"),
	READONLY_UINT_IMAGE_RECTANGLE(UINT_IMAGE_RECTANGLE, "readonlyuImage2dRect"),
	READONLY_UINT_IMAGE_1D_ARRAY(UINT_IMAGE_1D_ARRAY, "readonlyuImage1DArray"),
	READONLY_UINT_IMAGE_2D_ARRAY(UINT_IMAGE_2D_ARRAY, "readonlyuImage2DArray"),
	READONLY_UINT_IMAGE_CUBE_MAP_ARRAY(UINT_IMAGE_CUBE_MAP_ARRAY, "readonlyuImageCubeArray"),
	READONLY_UINT_IMAGE_BUFFER(UINT_IMAGE_BUFFER, "readonlyuImageBuffer"),
	READONLY_UINT_IMAGE_2D_MULTISAMPLE(UINT_IMAGE_2D_MULTISAMPLE, "readonlyuImage2DMS"),
	READONLY_UINT_IMAGE_2D_MULTISAMPLE_ARRAY(UINT_IMAGE_2D_MULTISAMPLE_ARRAY, "readonlyuImage2DMSArray"),

	WRITEONLY_UINT_IMAGE_1D(UINT_IMAGE_1D, "writeonlyuImage1D"),
	WRITEONLY_UINT_IMAGE_2D(UINT_IMAGE_2D, "writeonlyuImage2D"),
	WRITEONLY_UINT_IMAGE_3D(UINT_IMAGE_3D, "writeonlyuImage3D"),
	WRITEONLY_UINT_IMAGE_CUBE(UINT_IMAGE_CUBE, "writeonlyuImageCube"),
	WRITEONLY_UINT_IMAGE_RECTANGLE(UINT_IMAGE_RECTANGLE, "writeonlyuImage2dRect"),
	WRITEONLY_UINT_IMAGE_1D_ARRAY(UINT_IMAGE_1D_ARRAY, "writeonlyuImage1DArray"),
	WRITEONLY_UINT_IMAGE_2D_ARRAY(UINT_IMAGE_2D_ARRAY, "writeonlyuImage2DArray"),
	WRITEONLY_UINT_IMAGE_CUBE_MAP_ARRAY(UINT_IMAGE_CUBE_MAP_ARRAY, "writeonlyuImageCubeArray"),
	WRITEONLY_UINT_IMAGE_BUFFER(UINT_IMAGE_BUFFER, "writeonlyuImageBuffer"),
	WRITEONLY_UINT_IMAGE_2D_MULTISAMPLE(UINT_IMAGE_2D_MULTISAMPLE, "writeonlyuImage2DMS"),
	WRITEONLY_UINT_IMAGE_2D_MULTISAMPLE_ARRAY(UINT_IMAGE_2D_MULTISAMPLE_ARRAY, "writeonlyuImage2DMSArray"),

	// signed ints
	INT_TEXTURE_1D(GL_TEXTURE_1D, "isampler1D", GL_INT_SAMPLER_1D),
	INT_TEXTURE_2D(GL_TEXTURE_2D, "isampler2D", GL_INT_SAMPLER_2D),
	INT_TEXTURE_3D(GL_TEXTURE_3D, "isampler3D", GL_INT_SAMPLER_3D),
	INT_TEXTURE_CUBE(GL_TEXTURE_CUBE_MAP, "isamplerCube", GL_INT_SAMPLER_CUBE),
	INT_TEXTURE_RECTANGLE(GL_TEXTURE_RECTANGLE, "isampler2dRect", GL_INT_SAMPLER_2D_RECT),
	INT_TEXTURE_1D_ARRAY(GL_TEXTURE_1D_ARRAY, "isampler1DArray", GL_INT_SAMPLER_1D_ARRAY),
	INT_TEXTURE_2D_ARRAY(GL_TEXTURE_2D_ARRAY, "isampler2DArray", GL_INT_SAMPLER_2D_ARRAY),
	INT_TEXTURE_CUBE_MAP_ARRAY(GL_TEXTURE_CUBE_MAP_ARRAY, "isamplerCubeArray", GL_INT_SAMPLER_CUBE_MAP_ARRAY),
	INT_TEXTURE_BUFFER(GL_TEXTURE_BUFFER, "isamplerBuffer", GL_INT_SAMPLER_BUFFER),
	INT_TEXTURE_2D_MULTISAMPLE(GL_TEXTURE_2D_MULTISAMPLE, "isampler2DMS", GL_INT_SAMPLER_2D_MULTISAMPLE),
	INT_TEXTURE_2D_MULTISAMPLE_ARRAY(GL_TEXTURE_2D_MULTISAMPLE_ARRAY,
		"isampler2DMSArray",
		GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY
	),

	INT_IMAGE_1D(INT_TEXTURE_1D, "iimage1D", GL_INT_IMAGE_1D),
	INT_IMAGE_2D(INT_TEXTURE_2D, "iimage2D", GL_INT_IMAGE_2D),
	INT_IMAGE_3D(INT_TEXTURE_3D, "iimage3D", GL_INT_IMAGE_3D),
	INT_IMAGE_CUBE(INT_TEXTURE_CUBE, "iimageCube", GL_INT_IMAGE_CUBE),
	INT_IMAGE_RECTANGLE(INT_TEXTURE_RECTANGLE, "iimage2dRect", GL_INT_IMAGE_2D_RECT),
	INT_IMAGE_1D_ARRAY(INT_TEXTURE_1D_ARRAY, "iimage1DArray", GL_INT_IMAGE_1D_ARRAY),
	INT_IMAGE_2D_ARRAY(INT_TEXTURE_2D_ARRAY, "iimage2DArray", GL_INT_IMAGE_2D_ARRAY),
	INT_IMAGE_CUBE_MAP_ARRAY(INT_TEXTURE_CUBE_MAP_ARRAY, "iimageCubeArray", GL_INT_IMAGE_CUBE_MAP_ARRAY),
	INT_IMAGE_BUFFER(INT_TEXTURE_BUFFER, "iimageBuffer", GL_INT_IMAGE_BUFFER),
	INT_IMAGE_2D_MULTISAMPLE(INT_TEXTURE_2D_MULTISAMPLE, "iimage2DMS", GL_INT_IMAGE_2D_MULTISAMPLE),
	INT_IMAGE_2D_MULTISAMPLE_ARRAY(INT_TEXTURE_2D_MULTISAMPLE_ARRAY,
		"iimage2DMSArray",
		GL_INT_IMAGE_2D_MULTISAMPLE_ARRAY
	),

	READONLY_INT_IMAGE_1D(INT_IMAGE_1D, "readonlyiImage1D"),
	READONLY_INT_IMAGE_2D(INT_IMAGE_2D, "readonlyiImage2D"),
	READONLY_INT_IMAGE_3D(INT_IMAGE_3D, "readonlyiImage3D"),
	READONLY_INT_IMAGE_CUBE(INT_IMAGE_CUBE, "readonlyiImageCube"),
	READONLY_INT_IMAGE_RECTANGLE(INT_IMAGE_RECTANGLE, "readonlyiImage2dRect"),
	READONLY_INT_IMAGE_1D_ARRAY(INT_IMAGE_1D_ARRAY, "readonlyiImage1DArray"),
	READONLY_INT_IMAGE_2D_ARRAY(INT_IMAGE_2D_ARRAY, "readonlyiImage2DArray"),
	READONLY_INT_IMAGE_CUBE_MAP_ARRAY(INT_IMAGE_CUBE_MAP_ARRAY, "readonlyiImageCubeArray"),
	READONLY_INT_IMAGE_BUFFER(INT_IMAGE_BUFFER, "readonlyiImageBuffer"),
	READONLY_INT_IMAGE_2D_MULTISAMPLE(INT_IMAGE_2D_MULTISAMPLE, "readonlyiImage2DMS"),
	READONLY_INT_IMAGE_2D_MULTISAMPLE_ARRAY(INT_IMAGE_2D_MULTISAMPLE_ARRAY, "readonlyiImage2DMSArray"),

	WRITEONLY_INT_IMAGE_1D(INT_IMAGE_1D, "writeonlyiImage1D"),
	WRITEONLY_INT_IMAGE_2D(INT_IMAGE_2D, "writeonlyiImage2D"),
	WRITEONLY_INT_IMAGE_3D(INT_IMAGE_3D, "writeonlyiImage3D"),
	WRITEONLY_INT_IMAGE_CUBE(INT_IMAGE_CUBE, "writeonlyiImageCube"),
	WRITEONLY_INT_IMAGE_RECTANGLE(INT_IMAGE_RECTANGLE, "writeonlyiImage2dRect"),
	WRITEONLY_INT_IMAGE_1D_ARRAY(INT_IMAGE_1D_ARRAY, "writeonlyiImage1DArray"),
	WRITEONLY_INT_IMAGE_2D_ARRAY(INT_IMAGE_2D_ARRAY, "writeonlyiImage2DArray"),
	WRITEONLY_INT_IMAGE_CUBE_MAP_ARRAY(INT_IMAGE_CUBE_MAP_ARRAY, "writeonlyiImageCubeArray"),
	WRITEONLY_INT_IMAGE_BUFFER(INT_IMAGE_BUFFER, "writeonlyiImageBuffer"),
	WRITEONLY_INT_IMAGE_2D_MULTISAMPLE(INT_IMAGE_2D_MULTISAMPLE, "writeonlyiImage2DMS"),
	WRITEONLY_INT_IMAGE_2D_MULTISAMPLE_ARRAY(INT_IMAGE_2D_MULTISAMPLE_ARRAY, "writeonlyiImage2DMSArray"),

	ATOMIC_UINT(GL_UNSIGNED_INT_ATOMIC_COUNTER, 4, false, "atomic_uint", true, GL_UNSIGNED_INT_ATOMIC_COUNTER);

	public static final Set<DataType> VALID_OUTPUTS = Set.of(TEXTURE_1D,
		TEXTURE_2D,
		TEXTURE_3D,
		TEXTURE_CUBE,
		TEXTURE_RECTANGLE,
		TEXTURE_1D_ARRAY,
		TEXTURE_2D_ARRAY,
		TEXTURE_CUBE_MAP_ARRAY,
		TEXTURE_BUFFER,
		TEXTURE_2D_MULTISAMPLE,
		TEXTURE_2D_MULTISAMPLE_ARRAY
	);

	public static final Set<DataType> UNSUPPORTED_TYPES = new HashSet<>();
	static final Int2ObjectMap<Set<DataType>> COMPATIBLE_DATA_TYPES = new Int2ObjectOpenHashMap<>();

	static {
		for(DataType value : DataType.values()) {
			COMPATIBLE_DATA_TYPES.computeIfAbsent(value.glslType, i -> new HashSet<>()).add(value);
			if(!OpenGLSupport.IMAGE_LOAD_STORE && value.isImage) {
				UNSUPPORTED_TYPES.add(value);
			}
		}
		if(!OpenGLSupport.ATOMIC_COUNTERS) {
			UNSUPPORTED_TYPES.add(ATOMIC_UINT);
		}
	}

	// 39 * 3 * 11
	public final String glslName;
	public final boolean normalized;
	public final int byteCount, elementCount, elementType, glslType;
	public final boolean isMatrix, isSampler, uniformOnly, isImage;
	public final int m, n;

	DataType(int elementType, int byteCount, boolean normalized, String name, boolean uniformOnly, int glslType) {
		this(name, normalized, byteCount, 1, elementType, uniformOnly, glslType);
	}

	DataType(int elementType, int byteCount, boolean normalized, String name, int glslType) {
		this(name, normalized, byteCount, 1, elementType, false, glslType);
	}

	DataType(DataType type, String name, int glslType) {
		this(name, false, 4, 1, type.elementType, true, glslType);
	}

	DataType(int type, String name, int glslType) {
		this(name, false, 4, 1, type, true, glslType);
	}

	DataType(DataType type, String name) {
		this(type.elementType, type.byteCount, type.normalized, name, type.uniformOnly, type.glslType);
	}

	DataType(DataType type, int elementCount, String name, int glslType) {
		this(name, type.normalized, type.byteCount * elementCount, elementCount, type.elementType, false, glslType);
	}

	DataType(int elementType, int byteCount, String name, int glslType) {
		this(name, false, byteCount, 1, elementType, false, glslType);
	}

	DataType(
		String name,
		boolean normalized,
		int byteCount,
		int elementCount,
		int elementType,
		boolean uniformOnly,
		int glslType) {
		this.glslName = name;
		this.normalized = normalized;
		this.byteCount = byteCount;
		this.elementCount = elementCount;
		this.elementType = elementType;
		this.isMatrix = this.name().contains("MAT");
		if(this.isMatrix) {
			int index = this.name().lastIndexOf("MAT")+3;
			String str = this.name().substring(index);
			this.m = Character.digit(str.charAt(0), 10);
			if(str.length() == 1) {
				this.n = this.m;
			} else {
				this.n = Character.digit(str.charAt(2), 10);
			}
		} else {
			this.m = this.n = 1;
		}
		this.isSampler = name.contains("sampler");
		this.isImage = name.contains("image");
		this.uniformOnly = uniformOnly;
		this.glslType = glslType;
	}

	public boolean isOpaque() {
		return this.isImage || this.isSampler;
	}

	public boolean isFloating() {
		return this.elementType == GL_DOUBLE || this.elementType == GL_FLOAT || this.normalized;
	}

	public boolean isCompatible(int glslType) {
		return this.glslType == glslType;
	}

	public static Set<DataType> forGlslType(int type) {
		return COMPATIBLE_DATA_TYPES.getOrDefault(type, Set.of());
	}

	@Override
	public String toString() {
		return this.name() + " (\"" + this.glslName + "\")";
	}
}
