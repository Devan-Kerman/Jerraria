#include jerraria:api/translucency

uniform Test {
	vec4 color;
};

void main() {
	emit(color);
}
