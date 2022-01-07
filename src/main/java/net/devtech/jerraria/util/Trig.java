package net.devtech.jerraria.util;

public class Trig {
	public static final int COS_PRECISION = 2048;
	public static final float[] cosLookup = new float[COS_PRECISION];
	public static final float PI_2 = (float) (Math.PI / 2);
	public static final float PI2 = (float) (Math.PI * 2);
	static {
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
