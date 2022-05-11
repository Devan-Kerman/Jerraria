#version 430 core

uniform data {
	vec3 offsets[32];
};

in vec3 pos;
in vec3 color;

out vec3 vertexColor;

void main() {
	gl_Position = vec4(pos + offsets[gl_InstanceID], 1.0);
	vertexColor = color;
}
