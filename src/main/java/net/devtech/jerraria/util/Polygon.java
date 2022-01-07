package net.devtech.jerraria.util;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

public class Polygon {
	public static final Polygon UNIT_SQUARE = square(1).build();
	public static final Polygon UNIT_CIRCLE = regularPolygon(10, .5f).offset(.5f, .5f).build();

	final int len;
	final float[] points;
	final float minX, minY, maxX, maxY;
	byte isConcave;

	private Polygon(int len, float[] points) {
		this.len = len;
		this.points = points;
		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
		for(int i = 0; i < len; i+=2) {
			float x = points[i], y = points[i+1];
			if(x < minX) minX = x;
			if(y < minY) minY = y;
			if(x > maxX) maxX = x;
			if(y > maxY) maxY = y;
		}
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public static Polygon.Builder rect(float width, float height) {
		return new Builder(4).cart(0, 0).cart(width, 0).cart(width, height).cart(0, height);
	}

	public static Polygon.Builder square(float size) {
		return rect(size, size);
	}

	public static Polygon.Builder builder() {
		return new Builder();
	}

	public static Polygon.Builder builder(int expected) {
		return new Builder(expected);
	}

	public static Polygon.Builder regularPolygon(int resolution, float radius) {
		Builder builder = new Builder(resolution);
		for(int i = 0; i < resolution; i++) {
			builder.polarOrigin(radius, (Trig.PI2 / resolution) * i);
		}
		return builder;
	}

	public static void main(String[] args) {
		Polygon.Builder builder = builder(10);
		for(int i = 0; i < 10; i++) {
			builder.polar(100, (float)(Math.random() * Math.PI * 2), 0, 0);
		}
		System.out.println(builder.build());
	}

	public int vertices() {
		return this.len / 2;
	}

	public float getX(int vertex) {
		if(vertex < 0 || vertex*2 >= this.len) {
			throw new IndexOutOfBoundsException(vertex);
		}
		return this.points[vertex*2];
	}

	public float getY(int vertex) {
		if(vertex < 0 || vertex*2 >= this.len) {
			throw new IndexOutOfBoundsException(vertex);
		}
		return this.points[vertex*2+1];
	}

	public boolean intersects(Polygon polygon) {
		// todo proper polygon collision
		return !(this.minX > polygon.maxX || this.minY > polygon.maxY || this.maxX < polygon.minX || this.maxY < polygon.minY);
	}

	public boolean isConvex() {
		return !this.isConcave();
	}

	public boolean isConcave() {
		byte is = this.isConcave;
		return switch(is) {
			case 0 -> {
				float[] points = this.points;
				float px1 = points[0], py1 = points[1], px2 = points[2], py2 = points[3], px3 = points[4], py3 = points[5];
				float orig = getZCross(px1, py1, px2, py2, px3, py3);
				for(int i = 6; i < this.len + 4; i += 2) {
					px1 = px2;
					py1 = py2;
					px2 = px3;
					py2 = py3;
					int index = i % this.len;
					px3 = points[index];
					py3 = points[index + 1];
					float zcross = getZCross(px1, py1, px2, py2, px3, py3);
					if(Math.signum(orig) != Math.signum(zcross)) {
						this.isConcave = 1;
						yield true;
					}
				}
				this.isConcave = 2;
				yield false;
			}
			case 1 -> true;
			case 2 -> false;
			default -> throw new IllegalStateException("Unexpected value: " + is);
		};
	}

	public Builder toBuilder() {
		return new Builder(this);
	}

	private static float getZCross(float px1, float py1, float px2, float py2, float px3, float py3) {
		float dx1 = px3 - px2, dy1 = py3 - py2;
		float dx2 = px1 - px2, dy2 = py1 - py2;
		return dx1 * dy2 - dy1 * dx2;
	}

	public static final class Builder {
		FloatArrayList list;
		boolean copy;

		public Builder(int expected) {
			this.list = new FloatArrayList(expected * 2);
		}

		public Builder() {
			this(4);
		}

		public Builder(Builder builder) {
			this.list = new FloatArrayList(builder.list);
		}

		public Builder(Polygon polygon) {
			this.list = FloatArrayList.wrap(polygon.points, polygon.len);
			this.copy = true;
		}

		public Builder cart(float x, float y) {
			doCopy();
			list.add(x);
			list.add(y);
			return this;
		}

		public Builder polar(float r, float theta, float offX, float offY) {
			doCopy();
			list.add(Trig.cos(theta) * r + offX);
			list.add(Trig.sin(theta) * r + offY);
			return this;
		}

		public Builder polarUnit(float r, float theta) {
			return this.polar(r, theta, .5f, .5f);
		}

		public Builder polarOrigin(float r, float theta) {
			return this.polar(r, theta, 0, 0);
		}

		public Builder offset(float x, float y) {
			this.doCopy();
			for(int i = 0; i < this.list.size(); i += 2) {
				this.list.set(i, this.list.getFloat(i) + x);
				this.list.set(i + 1, this.list.getFloat(i + 1) + y);
			}
			return this;
		}

		public Polygon build() {
			int size = this.list.size();
			if(size < 6) {
				throw new IllegalStateException("Polygon has too few points (" + size / 2 + ") < 3");
			}

			this.copy = true;
			float[] elements = this.list.elements();
			// sort vertexes clockwise
			Arrays.quickSort(0, size / 2, (k1, k2) -> {
				int a = k1 * 2, b = k2 * 2;
				float ax = elements[a], ay = elements[a+1], bx = elements[b], by = elements[b+1];
				float aa = ax / ay, ab = bx / by;
				return ((int)Math.signum(ay * by)) * Float.compare(aa, ab);
			}, (a, b) -> {
				int ai = a * 2, bi = b * 2;
				float bx = elements[bi], by = elements[bi+1];
				elements[bi] = elements[ai];
				elements[bi+1] = elements[ai+1];
				elements[ai] = bx;
				elements[ai+1] = by;
			});
			return new Polygon(size, elements);
		}

		public Builder copy() {
			return new Builder(this);
		}

		void doCopy() {
			if(copy) {
				list = new FloatArrayList(list);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < this.len; i+=2) {
			builder.append('[').append(this.points[i]).append(',').append(this.points[i+1]).append(']');
		}
		return builder.toString();
	}
}
