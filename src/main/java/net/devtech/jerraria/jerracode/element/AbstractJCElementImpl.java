package net.devtech.jerraria.jerracode.element;

public abstract class AbstractJCElementImpl<T> implements JCElement<T> {
	@Override
	public boolean equals(Object o) {
		return equals0(o);
	}

	@Override
	public int hashCode() {
		return hashCode0();
	}

	@Override
	public String toString() {
		return toString0();
	}
}
