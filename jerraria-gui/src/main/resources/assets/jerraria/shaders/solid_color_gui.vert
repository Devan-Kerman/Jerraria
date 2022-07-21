#version 330

in vec3 pos;
in vec4 color;

out vec4 vColor;

void main() {
	gl_Position = vec4(pos, 1);
	vColor = color;
}
