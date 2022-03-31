#version 330 core
out vec4 FragColor;

in vec3 oColor;
in vec2 oUV;

uniform sampler2D texture_;

void main() {
	FragColor = texture(texture_, oUV) * vec4(oColor, 1.0);
}
