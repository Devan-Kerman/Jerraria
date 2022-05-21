#version 330 core

in vec3 oPos;
in vec4 oColor;

out vec4 accum;
out float reveal;

void main() {
	vec4 color = oColor;
	float weight = max(min(1.0, max(max(color.r, color.g), color.b) * color.a), color.a) * clamp(0.03 / (1e-5 + pow(oPos.z / 200, 4.0)), 1e-2, 3e3);
	accum = vec4(color.rgb * color.a, color.a) * weight;
	reveal = color.a;
}
