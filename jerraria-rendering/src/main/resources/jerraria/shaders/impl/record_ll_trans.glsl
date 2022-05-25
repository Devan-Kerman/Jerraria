// record stage of transparent rendering outlined in
// https://on-demand.gputechconf.com/gtc/2014/presentations/S4385-order-independent-transparency-opengl.pdf
layout (early_fragment_tests) in;

layout(rgba32ui) uniform coherent uimageBuffer translucencyBuffer;
layout(r32ui) uniform coherent uimage2D imgListHead;
layout(binding=0) uniform atomic_uint counter;

vec4 position();

vec4 color();

void main() { // todo write solid pixels immediately?
	uint idx = atomicCounterIncrement(counter) + 1u;// position where data is stored
	if (idx < imageSize(translucencyBuffer)) {
		ivec2 coord = ivec2(position().xy*imageSize(imgListHead));
		uint prev = imageAtomicExchange(imgListHead, coord, idx); // next in list
		imageStore(translucencyBuffer, int(idx), uvec4(packUnorm4x8(color()), floatBitsToUint(oPos.z), 0, prev));
	}
}
