// record stage of transparent rendering outlined in
// https://on-demand.gputechconf.com/gtc/2014/presentations/S4385-order-independent-transparency-opengl.pdf
#version 430 core

layout (early_fragment_tests) in;

layout(rgba32ui) uniform coherent uimageBuffer translucencyBuffer;
layout(r32ui) uniform coherent uimage2D imgListHead;
layout(binding=0) uniform atomic_uint counter;

in vec3 oPos;
in vec4 oColor;

void main() { // todo write solid pixels immediately?
	uint idx = atomicCounterIncrement(counter) + 1u;// position where data is stored
	if (idx < imageSize(translucencyBuffer)) {
		ivec2 coord = ivec2(oPos.xy*imageSize(imgListHead));
		uint prev = imageAtomicExchange(imgListHead, coord, idx); // next in list
		imageStore(translucencyBuffer, int(idx), uvec4(packUnorm4x8(oColor), floatBitsToUint(oPos.z), 0, prev));
	}
}
