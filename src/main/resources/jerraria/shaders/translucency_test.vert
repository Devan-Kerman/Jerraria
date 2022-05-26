#version 330 core

in vec3 pos;
in vec4 color;

out vec4 oColor;
out vec4 oPos;

void main() {
	gl_Position = oPos = vec4(pos, 1.0);
	oColor = color;
}
