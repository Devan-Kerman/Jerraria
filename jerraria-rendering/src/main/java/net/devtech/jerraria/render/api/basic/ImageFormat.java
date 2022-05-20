package net.devtech.jerraria.render.api.basic;

import static org.lwjgl.opengl.GL40.GL_R11F_G11F_B10F;
import static org.lwjgl.opengl.GL40.GL_R16;
import static org.lwjgl.opengl.GL40.GL_R16F;
import static org.lwjgl.opengl.GL40.GL_R16I;
import static org.lwjgl.opengl.GL40.GL_R16UI;
import static org.lwjgl.opengl.GL40.GL_R16_SNORM;
import static org.lwjgl.opengl.GL40.GL_R32F;
import static org.lwjgl.opengl.GL40.GL_R32I;
import static org.lwjgl.opengl.GL40.GL_R32UI;
import static org.lwjgl.opengl.GL40.GL_R8;
import static org.lwjgl.opengl.GL40.GL_R8I;
import static org.lwjgl.opengl.GL40.GL_R8UI;
import static org.lwjgl.opengl.GL40.GL_RG16;
import static org.lwjgl.opengl.GL40.GL_RG16F;
import static org.lwjgl.opengl.GL40.GL_RG16I;
import static org.lwjgl.opengl.GL40.GL_RG16UI;
import static org.lwjgl.opengl.GL40.GL_RG16_SNORM;
import static org.lwjgl.opengl.GL40.GL_RG32F;
import static org.lwjgl.opengl.GL40.GL_RG32I;
import static org.lwjgl.opengl.GL40.GL_RG32UI;
import static org.lwjgl.opengl.GL40.GL_RG8;
import static org.lwjgl.opengl.GL40.GL_RG8I;
import static org.lwjgl.opengl.GL40.GL_RG8UI;
import static org.lwjgl.opengl.GL40.GL_RG8_SNORM;
import static org.lwjgl.opengl.GL40.GL_RGB10_A2;
import static org.lwjgl.opengl.GL40.GL_RGB10_A2UI;
import static org.lwjgl.opengl.GL40.GL_RGBA16;
import static org.lwjgl.opengl.GL40.GL_RGBA16F;
import static org.lwjgl.opengl.GL40.GL_RGBA16I;
import static org.lwjgl.opengl.GL40.GL_RGBA16UI;
import static org.lwjgl.opengl.GL40.GL_RGBA16_SNORM;
import static org.lwjgl.opengl.GL40.GL_RGBA32F;
import static org.lwjgl.opengl.GL40.GL_RGBA32I;
import static org.lwjgl.opengl.GL40.GL_RGBA32UI;
import static org.lwjgl.opengl.GL40.GL_RGBA8;
import static org.lwjgl.opengl.GL40.GL_RGBA8I;
import static org.lwjgl.opengl.GL40.GL_RGBA8UI;
import static org.lwjgl.opengl.GL40.GL_RGBA8_SNORM;

/**
 * https://www.khronos.org/opengl/wiki/Image_Load_Store
 */
public enum ImageFormat {
	RGBA32F(GL_RGBA32F, "gl_rgba32f"),
	RGBA32UI(GL_RGBA32UI, "gl_rgba32ui"),
	RGBA16F(GL_RGBA16F, "gl_rgba16f"),
	RGBA16UI(GL_RGBA16UI, "gl_rgba16ui"),
	RG32F(GL_RG32F, "gl_rg32f"),
	RGB10_A2UI(GL_RGB10_A2UI, "gl_rgb10_a2ui"),
	RG16F(GL_RG16F, "gl_rg16f"),
	RGBA8UI(GL_RGBA8UI, "gl_rgba8ui"),
	R11F_G11F_B10F(GL_R11F_G11F_B10F, "gl_r11f_g11f_b10f"),
	RG32UI(GL_RG32UI, "gl_rg32ui"),
	R32F(GL_R32F, "gl_r32f"),
	RG16UI(GL_RG16UI, "gl_rg16ui"),
	R16F(GL_R16F, "gl_r16f"),
	RG8UI(GL_RG8UI, "gl_rg8ui"),
	RGBA16(GL_RGBA16, "gl_rgba16"),
	R32UI(GL_R32UI, "gl_r32ui"),
	RGB10_A2(GL_RGB10_A2, "gl_rgb10_a2"),
	R16UI(GL_R16UI, "gl_r16ui"),
	RGBA8(GL_RGBA8, "gl_rgba8"),
	R8UI(GL_R8UI, "gl_r8ui"),
	RG16(GL_RG16, "gl_rg16"),
	RGBA32I(GL_RGBA32I, "gl_rgba32i"),
	RG8(GL_RG8, "gl_rg8"),
	RGBA16I(GL_RGBA16I, "gl_rgba16i"),
	R16(GL_R16, "gl_r16"),
	RGBA8I(GL_RGBA8I, "gl_rgba8i"),
	R8(GL_R8, "gl_r8"),
	RG32I(GL_RG32I, "gl_rg32i"),
	RGBA16_SNORM(GL_RGBA16_SNORM, "gl_rgba16_snorm"),
	RG16I(GL_RG16I, "gl_rg16i"),
	RGBA8_SNORM(GL_RGBA8_SNORM, "gl_rgba8_snorm"),
	RG8I(GL_RG8I, "gl_rg8i"),
	RG16_SNORM(GL_RG16_SNORM, "gl_rg16_snorm"),
	R32I(GL_R32I, "gl_r32i"),
	RG8_SNORM(GL_RG8_SNORM, "gl_rg8_snorm"),
	R16I(GL_R16I, "gl_r16i"),
	R16_SNORM(GL_R16_SNORM, "gl_r16_snorm"),
	R8I(GL_R8I, "gl_r8i");

	// maybe add compatible types?

	public final int glId;
	public final String qualifier;

	ImageFormat(int id, String qualifier) {
		this.glId = id;
		this.qualifier = qualifier;
	}
}
