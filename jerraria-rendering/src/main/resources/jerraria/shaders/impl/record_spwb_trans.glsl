out vec4 accum;
out float reveal;

vec4 position();

vec4 color();

void main() {
	vec4 color = color();
	float weight = clamp(pow(min(1.0, color.a * 10.0) + 0.01, 3.0) * 1e8 * pow(1.0 - position().z * 0.9, 3.0), 1e-2, 3e3);
	accum = vec4(color.rgb * color.a, color.a) * weight;
	reveal = color.a;
}
