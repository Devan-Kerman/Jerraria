#version 430 core

in vec3 pos;
out vec3 oPos;

void main() {
	gl_Position = vec4(pos, 1.0);
	oPos = pos;
}
