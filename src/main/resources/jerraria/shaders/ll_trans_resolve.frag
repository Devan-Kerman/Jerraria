#version 430 core

// maximum number of fragments to sort in one pass
// the rest are just mixed together willy-nilly
#ifndef MAX_SORT
	#define MAX_SORT 8
#endif

layout (early_fragment_tests) in;

layout(rgba32ui) uniform readonly coherent uimageBuffer translucencyBuffer;
layout(r32ui) uniform readonly coherent uimage2D imgListHead;
uniform vec2 screenSpace;

in vec3 oPos;

// sort impl is broken
void sort(inout uvec2 fragments[MAX_SORT], int n) {
	for(int i = 1; i < n; i++) {
		uvec2 current = fragments[i];
		float depthA = uintBitsToFloat(current).y;
		int r = i-1;
		for(; r >= 0; r--) {
			uvec2 comp = fragments[r];
			float depthB = uintBitsToFloat(comp).y;
			if(depthA < depthB) {
				fragments[r+1] = comp;
			} else {
				break;
			}
		}
		fragments[r+1] = current;
	}
}

void main() {
	ivec2 coord = ivec2(oPos.xy*imageSize(imgListHead));
	uvec2 fragments[MAX_SORT]; // test init

	// head pointer
	uint idx = imageLoad(imgListHead, coord).r;

	// load first K colors, maybe we should only load the first K high alpha numbers?
	int sorted = 0;
	while((idx != 0) && sorted < MAX_SORT) {
		uvec4 translucency = imageLoad(translucencyBuffer, int(idx));
		fragments[sorted++] = translucency.rg;
		idx = translucency.a;
	}

	// the paper says to do a preliminary sort, dunno why
	sort(fragments, sorted);

	vec4 color = vec4(0);
	for (int i = 0; i < sorted; i++) {
		vec4 blend = unpackUnorm4x8(fragments[i].x);
		color = blend * blend.a + color * (1-color.a);
	}

	while(idx != 0) {
		uvec4 translucency = imageLoad(translucencyBuffer, int(idx));
		vec4 blend = unpackUnorm4x8(translucency.r);
		color = blend * blend.a + color * (1-color.a);
		idx = translucency.a;
	}

	gl_FragColor = color;
	//gl_FragColor = vec4(sorted, sorted-1, sorted-2, 1);
}
