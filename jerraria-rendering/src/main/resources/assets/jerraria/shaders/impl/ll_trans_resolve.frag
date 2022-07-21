#version 430 core

// maximum number of fragments to sort in one pass
// the rest are just mixed together willy-nilly
#ifndef MAX_SORT
#define MAX_SORT 10
#endif

layout(rgba32ui) uniform readonly coherent uimageBuffer translucencyBuffer;
layout(r32ui) uniform readonly coherent uimage2D imgListHead;
uniform vec2 screenSpace;

// sort impl is broken
void sort(inout uvec2 fragments[MAX_SORT], int n) {
	for (int i = 1; i < n; i++) {
		uvec2 current = fragments[i];
		float depthA = uintBitsToFloat(current.y);
		int r = i-1;
		for (; r >= 0; r--) {
			uvec2 comp = fragments[r];
			float depthB = uintBitsToFloat(comp.y);

			#ifndef Z_FIGHTING_FIX
				if (depthA > depthB) {
					fragments[r+1] = comp;
				} else {
					break;
				}
			#else
				if (depthA > depthB || (depthA == depthB && comp.x > current.x)) {
					fragments[r+1] = comp;
				} else {
					break;
				}
			#endif
		}
		fragments[r+1] = current;
	}
}

/**
 * @returns the Kth furthest fragment, either this value or some value in the array
*/
vec4 insert(inout uvec2 fragments[MAX_SORT], uvec2 vec) {
	// to check if pop, just check the last element in offsets
	uvec2 last = fragments[MAX_SORT-1];
	float depthLast = uintBitsToFloat(last.y);
	float depthB = uintBitsToFloat(vec.y);
	if (depthLast < depthB) {
		return unpackUnorm4x8(vec.x);// if vector is not within Kth closest, tail sort it
	} else {
		// sorting heuristic: we assume that render order is *roughly* in the correct order because we can render
		// 	whole chunks, BERs and Entities in order fairly trivially
		int i = MAX_SORT-2;
		for (; i >= 0; i--) {
			uvec2 current = fragments[i];// current Kth furthest fragment
			float depthA = uintBitsToFloat(current.y);
			if (depthB > depthA) {
				fragments[i+1] = fragments[i];
			} else {
				break;
			}
		}
		fragments[i+1] = vec;
		return unpackUnorm4x8(last.x);
	}
}

void main() {
	ivec2 coord = ivec2(gl_FragCoord.xy);
	uvec2 fragments[MAX_SORT];// test init

	// head pointer
	uint idx = imageLoad(imgListHead, coord).r;

	// load first K colors, maybe we should only load the first K high alpha numbers?
	int sorted = 0;
	while ((idx != 0) && sorted < MAX_SORT) {
		uvec4 translucency = imageLoad(translucencyBuffer, int(idx));
		fragments[sorted++] = translucency.rg;
		idx = translucency.b;
	}

	// the paper says to do a preliminary sort, dunno why
	sort(fragments, sorted);

	vec4 color = vec4(0);

	// tail blending
	while (idx != 0) {
		uvec4 translucency = imageLoad(translucencyBuffer, int(idx));
		vec4 blend = insert(fragments, translucency.rg);
		color = blend * blend.a + color * (1-color.a);
		idx = translucency.b;
	}

	// sorted blending
	for (int i = 0; i < sorted; i++) {
		vec4 blend = unpackUnorm4x8(fragments[i].x);
		color = blend * blend.a + color * (1-color.a);
	}

	gl_FragColor = color;
}
