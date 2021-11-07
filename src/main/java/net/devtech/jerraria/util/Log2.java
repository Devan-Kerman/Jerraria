package net.devtech.jerraria.util;

public final class Log2 {
	private static final byte[] LogTable256 = new byte[256];

	static {
		LogTable256[0] = LogTable256[1] = 0;
		for(int i = 2; i < 256; i++) {
			LogTable256[i] = (byte) (1 + LogTable256[i / 2]);
		}
		LogTable256[0] = -1; // if you want log(0) to return -1
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
}
