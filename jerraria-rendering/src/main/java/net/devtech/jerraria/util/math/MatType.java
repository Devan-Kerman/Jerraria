package net.devtech.jerraria.util.math;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class MatType {
	static final AtomicInteger id = new AtomicInteger();
	public static final MatType MAT3 = new Mat2x3f.Type();
	public static final MatType MAT4 = new Mat4f.Type();
	public final int typeId = id.getAndIncrement();

	MatType() {}

	public abstract Mat createIdentity();

	public static int ids() {
		return id.get();
	}
}
