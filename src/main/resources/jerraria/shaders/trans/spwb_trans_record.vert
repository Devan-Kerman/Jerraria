#version 330 core

in vec4 color;
in vec3 pos;

out vec4 oColor;
out vec3 oPos;

void main() {
	gl_Position = vec4(pos, 1.0);
	oColor = color;
}
