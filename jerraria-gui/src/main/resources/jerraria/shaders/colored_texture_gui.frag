#version 330 core

in vec4 oColor;
in vec2 oUV;

out vec4 FragColor;
out float reveal;

uniform sampler2D texture_;

void main() {
	vec4 pixel = texture(texture_, oUV);
	if(pixel.a < .1) {
		discard;
	}
	FragColor = pixel * oColor;
}
