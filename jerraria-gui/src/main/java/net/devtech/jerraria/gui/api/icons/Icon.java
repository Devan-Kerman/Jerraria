package net.devtech.jerraria.gui.api.icons;

import java.awt.Shape;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.borders.BorderFactory;
import net.devtech.jerraria.gui.api.icons.borders.SolidColorBorder;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatCacheEntry;
import net.devtech.jerraria.util.math.MatrixPoolStack;

public interface Icon {
	MatrixPoolStack POOL = new MatrixPoolStack(10);

	SolidColorIcon DEFAULT_DARKEN = new SolidColorIcon(0x44111111);
	SolidColorIcon DEFAULT_LIGHTEN = new SolidColorIcon(0x66FFFFFF);
	SolidColorIcon DEFAULT_HIGHLIGHTED = new SolidColorIcon(0x667777FF);

	static TextureIcon tex(Texture texture, float height) {
		return new TextureIcon(texture, height);
	}

	static TextureIcon tex(Texture texture, float width, float height) {
		return new TextureIcon(texture, width, height);
	}

	static TextureIcon texMix(Texture texture, int mixColor, float width, float height) {
		return new TextureIcon(texture, mixColor, width, height);
	}

	static TextureIcon texMix(Texture texture, int mixColor, float height) {
		return new TextureIcon(texture, mixColor, height);
	}

	static Icon color(int argb, float width, float height) {
		return new SolidColorIcon(argb, width, height);
	}

	static Icon color(int argb) {
		return new SolidColorIcon(argb);
	}

	static Icon shape(Shape shape, int argb) {
		return new ShapeIcon(shape, .1f, argb);
	}

	/**
	 * @param flatness the accuracy of the triangulation of this shape, lower values = more accurate & less
	 * 	performance
	 */
	static Icon shape(Shape shape, float flatness, int argb) {
		return new ShapeIcon(shape, flatness, argb);
	}

	/**
	 * Render the icon with width and height 1 and at offset 0. The matrix will do the relevant transforms for you.
	 */
	default void drawNormalized(MatrixBatchedRenderer renderer) {
		try(MatCacheEntry entry = POOL.copy(renderer.mat())) {
			Mat mat = entry.instance();
			mat.scale(1 / this.width(), 1 / this.height());
			this.draw(renderer.withMat(mat));
		}
	}

	/**
	 * @return If this icon was to be rendered with a height of one, return the width in pixels of said hypothetical
	 * 	icon This is purely a recommendation, and describes approximately how long the component is, you can
	 * 	technically
	 * 	render an icon in any dimensions you please. Essentially it is <pre>width/height</pre>
	 */
	default float aspectRatio() {
		return this.width() / this.height();
	}

	float width();

	float height();

	void draw(MatrixBatchedRenderer renderer);

	/**
	 * Draw the component with the given dimensions, icons can optionally override this method to scale more
	 * effectively.
	 */
	default void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
		try(MatCacheEntry entry = POOL.copy(renderer.mat())) {
			Mat mat = entry.instance();
			mat.scale(dimX / this.width(), dimY / this.height());
			this.draw(renderer.withMat(mat));
		}
	}

	default void draw(MatrixBatchedRenderer renderer, float height) {
		this.draw(renderer, this.aspectRatio() * height, height);
	}

	default Icon darkened() {
		return this.overlay(DEFAULT_DARKEN);
	}

	default Icon lightened() {
		return this.overlay(DEFAULT_LIGHTEN);
	}

	default Icon highlighted() {
		return this.overlay(DEFAULT_HIGHLIGHTED);
	}

	default Icon centered(Icon icon) {
		return this.centered(icon, icon.width(), icon.height());
	}

	abstract class Forwarding implements Icon {
		public Icon inherit;

		protected Forwarding(Icon inherit) {
			this.inherit = inherit;
		}

		@Override
		public float width() {
			return this.inherit.width();
		}

		@Override
		public float height() {
			return this.inherit.height();
		}
	}

	/**
	 * @param scale how big relative to the current component to draw this in the center [0-1]
	 */
	default Icon centered(Icon icon, float scale) {
		float width, height;
		if(icon.aspectRatio() > 1) {
			width = scale;
			height = scale / icon.aspectRatio();
		} else {
			width = icon.aspectRatio() * scale;
			height = scale;
		}

		return this.centered(icon, width, height);
	}

	/**
	 * @param width how big to draw the component relative to the other [0-1]
	 */
	default Icon centered(Icon icon, float width, float height) {
		return new Forwarding(this) {
			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				Icon.this.draw(renderer);
				renderer.raise();
				this.draw0(renderer, this.width(), this.height());
			}

			@Override
			public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
				Icon.this.draw(renderer, dimX, dimY);
				renderer.raise();
				this.draw0(renderer, dimX, dimY);
			}

			private void draw0(MatrixBatchedRenderer renderer, float dimX, float dimY) {
				float min = Math.min(dimX, dimY);
				try(MatCacheEntry entry = POOL.copy(renderer.mat())) {
					Mat mat = entry.instance().offset((dimX - width * min) / 2, (dimY - height * min) / 2);
					icon.draw(renderer.withMat(mat), width * min, height * min);
				}
			}
		};
	}

	default Icon overlay(Icon icon) {
		return new Forwarding(this) {
			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				Icon.this.draw(renderer);
				renderer.raise();
				icon.draw(renderer, Icon.this.width(), Icon.this.height());
			}

			@Override
			public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
				Icon.this.draw(renderer, dimX, dimY);
				renderer.raise();
				icon.draw(renderer, dimX, dimY);
			}
		};
	}

	default Icon scale(float scaleX, float scaleY) {
		return new Icon() {
			@Override
			public float width() {
				return Icon.this.width() * scaleX;
			}

			@Override
			public float height() {
				return Icon.this.height() * scaleY;
			}

			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				Icon.this.draw(renderer, this.width(), this.height());
			}

			@Override
			public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
				Icon.this.draw(renderer, dimX, dimY);
			}

			@Override
			public Icon scale(float scaleX0, float scaleY0) {
				return Icon.this.scale(scaleX*scaleX0, scaleY*scaleY0);
			}
		};
	}

	default <T> Icon bordered(BorderFactory<T> factory, T settings) {
		return factory.createBordered(this, settings);
	}

	default Icon bordered(int argb) {
		return this.bordered(SolidColorBorder.BORDERED, new SolidColorBorder.Settings(argb));
	}

	default Icon bordered(int argb, float thickness) {
		return this.bordered(SolidColorBorder.BORDERED, new SolidColorBorder.Settings(argb, thickness));
	}
}
