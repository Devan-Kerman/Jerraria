package net.devtech.jerraria.gui.api.icons;

import java.awt.Shape;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatrixCache;

public interface Icon {
	MatrixCache CACHE = new MatrixCache();

	int DEFAULT_DARKEN = 0x44111111;
	int DEFAULT_LIGHTEN = 0x66FFFFFF;
	int DEFAULT_HIGHLIGHTED = 0x667777FF;

	static Icon tex(Texture texture) {
		return new TextureIcon(texture);
	}

	static Icon tex(Texture texture, int mixColor) {
		return new TextureIcon(texture, mixColor);
	}

	static Icon color(int argb) {
		return new ColorIcon(argb);
	}

	static Icon shape(Shape shape, int argb) {
		return new ShapeIcon(shape, .1f, argb);
	}

	/**
	 * @param flatness the accuracy of the triangulation of this shape, lower values = more accurate & less performance
	 */
	static Icon shape(Shape shape, float flatness, int argb) {
		return new ShapeIcon(shape, flatness, argb);
	}

	/**
	 * Render the icon with width and height 1 and at offset 0. The matrix will do the relevant transforms for you.
	 */
	void draw(MatrixBatchedRenderer renderer);

	/**
	 * @return If this icon was to be rendered with a height of one, return the width in pixels of said hypothetical
	 * 	icon This is purely a recommendation, and describes approximately how long the component is, you can
	 * 	technically render an icon in any dimensions you please. Essentially it is <pre>width/height</pre>
	 */
	default float aspectRatio() {
		return 1;
	}

	default void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
		Mat mat = CACHE.copy(renderer.mat());
		mat.scale(dimX, dimY);
		this.draw(renderer.withMat(mat));
	}

	default void draw(MatrixBatchedRenderer renderer, float height) {
		this.draw(renderer, this.aspectRatio() * height, height);
	}

	default Icon darkened() {
		return this.overlay(new ColorIcon(DEFAULT_DARKEN));
	}

	default Icon lightened() {
		return this.overlay(new ColorIcon(DEFAULT_LIGHTEN));
	}

	default Icon highlighted() {
		return this.overlay(new ColorIcon(DEFAULT_HIGHLIGHTED));
	}

	/**
	 * @param scale how big relative to the current component to draw this in the center [0-1]
	 */
	default Icon centered(Icon icon, float scale) {
		return this.centered(icon, scale, scale/icon.aspectRatio());
	}

	/**
	 * @param width how big to draw the component relative to the other [0-1]
	 */
	default Icon centered(Icon icon, float width, float height) {
		return new Icon() {
			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				Icon.this.draw(renderer);
				renderer.raise();
				Mat copy = renderer.mat().copy().offset((1-width)/2, (1-height)/2).scale(width, height);
				icon.draw(renderer.withMat(copy));
			}

			@Override
			public float aspectRatio() {
				return Math.max(Icon.this.aspectRatio(), width/height);
			}
		};
	}

	default Icon overlay(Icon icon) {
		return new Icon() {
			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				Icon.this.draw(renderer);
				renderer.raise();
				icon.draw(renderer);
			}

			@Override
			public float aspectRatio() {
				return Math.max(Icon.this.aspectRatio(), icon.aspectRatio());
			}
		};
	}
}
