#version 330 core

in vec3 pos;
in vec4 color;
in vec2 uv;

out vec4 oColor;
out vec2 oUV;

uniform mat3 mat_;

void main() {
	gl_Position = vec4(mat_*pos, 1.0);
	oColor = color;
	oUV = uv;
}
