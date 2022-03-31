#version 330 core
out vec4 FragColor;

in vec3 oColor;
in vec2 oUV;

uniform sampler2D texture_;

void main() {
	vec4 pixel = texture(texture_, oUV);
	if(pixel.a < .1) {
		discard;
	}
	FragColor = pixel * vec4(oColor, 1.0);
}
