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
	}
}
