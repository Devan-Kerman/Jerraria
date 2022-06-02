#version 430 core

struct Instance {
	vec4 color;
	float scale;
};

buffer MyBuffer {
	vec4 fade;
	Instance colors[];
};

in vec3 pos;

out vec4 vColor;

void main() {
	gl_Position = vec4(pos, 1.0);
	Instance instance = colors[gl_InstanceID];
	vColor = instance.color + instance.scale * fade;
}
