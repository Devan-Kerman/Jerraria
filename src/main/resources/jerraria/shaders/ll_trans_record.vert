#version 330 core

in vec3 pos;
in vec4 color;

out vec4 oColor;
out vec3 oPos;

void main() {
	gl_Position = vec4(pos, 1.0);
	oColor = color;
	oPos = pos;
}
