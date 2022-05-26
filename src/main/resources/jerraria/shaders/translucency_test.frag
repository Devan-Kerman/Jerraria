#include jerraria:api/translucency

in vec4 oPos;
in vec4 oColor;

vec4 position() {
	return oPos;
}

vec4 color() {
	return oColor;
}
