package net.devtech.jerraria.jerracode.internal;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.devtech.jerraria.jerracode.JCType;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.JCElement;

public class PairJCType<A, B, AN, BN> implements JCType<Pair<A, B>, Pair<JCElement, JCElement>> {
	public final JCType<A, AN> a;
	public final JCType<B, BN> b;

	public PairJCType(JCType<A, AN> a, JCType<B, BN> b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public NativeJCType<Pair<JCElement, JCElement>> nativeType() {
		return NativeJCType.PAIR;
	}

	@Override
	public Pair<JCElement, JCElement> convertToNative(Pair<A, B> value) {
		JCElement a = JCElement.create(this.a, value.first()), b = JCElement.create(this.b, value.second());
		return new ObjectObjectImmutablePair<>(a, b);
	}

	@Override
	public Pair<A, B> convertFromNative(Pair<JCElement, JCElement> value) {
		return null;
	}
}
