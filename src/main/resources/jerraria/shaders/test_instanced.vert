#version 330 core

uniform Offsets {
	vec3 offsets[32];
};

uniform Colors {
	vec3 colors[32];
};

in vec3 pos;

out vec3 oColor;

void main() {
	gl_Position = vec4(pos + offsets[gl_InstanceID], 1.0);
	oColor = colors[gl_InstanceID];
}
