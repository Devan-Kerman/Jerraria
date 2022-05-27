void emit(vec4 color);

#ifdef LINKED_LIST
	#include jerraria:impl/record_ll_trans
#endif

#ifdef SINGLE_PASS
	#include jerraria:impl/record_spwb_trans
#endif

#ifdef DOUBLE_PASS_A
	#include jerraria:impl/record_dpwb_trans_a
#endif

#ifdef DOUBLE_PASS_B
	#include jerraria:impl/record_dpwb_trans_b
#endif
