package net.devtech.jerraria.render.api.basic;

public abstract class GlData {
	public Buf element(String name) {
		return element(getElement(name));
	}

	public abstract Buf element(Element element);

	public abstract Element getElement(String name);

	public interface Element {
		default Element getSelf() {
			return this;
		}
	}

	public interface Buf {
		Buf f(float f);

		Buf i(int i);

		Buf b(byte b);

		Buf bool(boolean b);

		Buf s(short s);

		Buf c(char c);

		Buf d(double d);

		Buf o(Object o);
	}

	public interface BufAdapter extends Buf {
		@Override
		default Buf f(float f) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf i(int i) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf b(byte b) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf bool(boolean b) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf s(short s) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf c(char c) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf d(double d) {
			throw new UnsupportedOperationException();
		}

		@Override
		default Buf o(Object o) {
			throw new UnsupportedOperationException();
		}
	}
}
