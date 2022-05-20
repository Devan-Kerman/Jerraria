#version 430 core

out vec4 FragColor;

layout(r32ui) uniform coherent uimage2D imgListHead;

void main() {
	ivec2 coord = ivec2(gl_FragCoord.xy*imageSize(imgListHead));
	FragColor = vec4(imageAtomicExchange(imgListHead, ivec2(0, 0), coord.x)/100f, 0, 0, 1);
}
