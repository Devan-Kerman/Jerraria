package net.devtech.jerraria.render.internal;

public abstract class GlData {
	public abstract GlData flush();

	public Buf element(String name) {
		return element(getElement(name));
	}

	public abstract Buf element(Element element);

	public abstract Element getElement(String name);

	public interface Element {}

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
