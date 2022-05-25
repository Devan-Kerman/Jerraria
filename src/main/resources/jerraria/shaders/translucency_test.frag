#include jerraria:api/translucency

in vec4 oColor;

vec4 position() {
	return gl_FragCoord;
}

vec4 color() {
	return oColor;
}
