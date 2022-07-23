#version 150
in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;

void main() {
	vec3 pos = Position + ChunkOffset;
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
