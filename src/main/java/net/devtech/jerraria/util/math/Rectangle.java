package net.devtech.jerraria.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public record Rectangle(double offX, double offY, double width, double height) implements Comparable<Rectangle> {
	public Rectangle {
		if(width < 0) {
			throw new IllegalArgumentException("width < 0");
		}
		if(height < 0) {
			throw new IllegalArgumentException("height < 0");
		}
	}

	public Rectangle(double width, double height) {
		this(0, 0, width, height);
	}

	/**
	 * @return true if the shape is entirely enclosed by the given bounding box
	 */
	public boolean isEnclosed(double fromX, double fromY, double toX, double toY) {
		return this.offX >= fromX && this.offY >= fromY && (this.offX + this.width) <= toX && (this.offY + this.height) <= toY;
	}

	/**
	 * @return true if the shape contacts the given bounding box
	 */
	public boolean doesIntersect(double fromX, double fromY, double toX, double toY) {
		double l1x = this.offX;
		if(l1x >= toX || fromX >= (l1x + this.width)) {
			return false;
		}

		double l1y = this.offY;
		return !((l1y + this.height) >= fromY) && !(toY >= l1y);
	}

	public double endX() {
		return this.offX + this.width;
	}

	public double endY() {
		return this.offY + this.height;
	}

	public double midX() {
		return this.offX + this.width / 2;
	}

	public double midY() {
		return this.offY + this.height / 2;
	}

	/**
	 * sorts rectangles counterclockwise based on their midpoint
	 */
	@Override
	public int compareTo(@NotNull Rectangle o) {
		double r1mx = this.midX(), r1my = this.midY();
		double r2mx = o.midX(), r2my = o.midY();
		//double aa = r1mx / r1my, ab = r2mx / r2my;
		double aa = r1mx * r2my, ab = r2mx * r1my;
		int siga = r1my >= 0 ? 1 : -1, sigb = r2my >= 0 ? 1 : -1;
		return siga * sigb * Double.compare(aa, ab);
	}
}
