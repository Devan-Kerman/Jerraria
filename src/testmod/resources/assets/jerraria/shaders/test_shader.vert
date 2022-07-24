#version 430

in vec3 Position;

uniform mat4 ProjMat;
buffer Mats {
	mat4 blockEntityMat[];
} instanceData;

void main() {
	gl_Position = ProjMat * instanceData.blockEntityMat[gl_InstanceID] * vec4(Position, 1.0);
}