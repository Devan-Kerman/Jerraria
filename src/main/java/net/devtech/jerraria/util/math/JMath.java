package net.devtech.jerraria.util.math;

public class JMath {
	public static final int COS_PRECISION = 2048;
	public static final float[] cosLookup = new float[COS_PRECISION];
	public static final float PI_2 = (float) (Math.PI / 2);
	public static final float PI2 = (float) (Math.PI * 2);
	static final byte[] LogTable256 = new byte[256];

	public static int div(int numerator, int denominator) {
		return (numerator + denominator - 1) / denominator;
	}

	public static int nearestPowerOf2(int in) {
		long v = in & 0xFFFFFFFFL;
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		return (int) v;
	}

	public static int log2(int in) {
		if(in < 0) {
			throw new IllegalArgumentException("in cannot be less than 0!");
		}
		int r;     // r will be lg(v)
		int t, tt; // temporaries

		if((tt = in >> 16) != 0) {
			t = tt >> 8;
			r = (t != 0) ? 24 + LogTable256[t] : 16 + LogTable256[tt];
		} else {
			t = in >> 8;
			r = (t != 0) ? 8 + LogTable256[t] : LogTable256[in];
		}

		r += (1 << r) == in ? 0 : 1;
		return r;
	}

	static {
		LogTable256[0] = LogTable256[1] = 0;
		for(int i = 2; i < 256; i++) {
			LogTable256[i] = (byte) (1 + LogTable256[i / 2]);
		}
		LogTable256[0] = -1; // if you want log(0) to return -1

		for(int i = 0; i < COS_PRECISION; i++) {
			double input = (i * PI2 / COS_PRECISION);
			cosLookup[i] = (float) Math.cos(input);
		}
	}

	public static float cos(float theta) {
		return cosLookup[(int) (COS_PRECISION * (Math.abs(theta) % PI2 / PI2))];
	}

	public static float sin(float theta) {
		return cos(theta - PI_2);
	}
}
