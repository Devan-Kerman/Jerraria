#version 430 core

in vec3 pos;
in vec3 color;

out vec3 vertexColor;

void main() {
	gl_Position = vec4(pos, 1.0);
	vertexColor = color;
}
