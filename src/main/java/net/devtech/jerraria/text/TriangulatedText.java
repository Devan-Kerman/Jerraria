package net.devtech.jerraria.text;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.Collections;

import net.devtech.jerraria.util.math.JMath;

/**
 * Uses AWT font rendering to convert a string into a ton of triangles.
 *
 * Is this unperformant? yes.   Do I care? no. Is this cursed?       yes.   Do I care? no.
 */
public final class TriangulatedText {
	static final Graphics2D IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	static final Font DEFAULT_FONT = IMAGE.getFont();
	static final FontMetrics DEFAULT_METRICS = metrics(DEFAULT_FONT);

	public static TriangulatedText text(String string) {
		return new TriangulatedText(string, 0xFFFFFFFF, DEFAULT_FONT, DEFAULT_METRICS);
	}

	public static TriangulatedText text(String string, int argb) {
		return new TriangulatedText(string, argb, DEFAULT_FONT, DEFAULT_METRICS);
	}

	final int color;
	final String text;
	final Font font;
	final FontMetrics metrics;
	final Triangulation[] triangulation;
	boolean isBold;

	TriangulatedText(String text, int color, Font font, FontMetrics metrics) {
		this.color = color;
		this.text = text;
		this.font = font;
		this.metrics = metrics;
		this.triangulation = new Triangulation[5];
	}

	TriangulatedText(
		String text, int color, Font font, FontMetrics metrics, Triangulation[] triangulation) {
		this.text = text;
		this.color = color;
		this.font = font;
		this.metrics = metrics;
		this.triangulation = triangulation;
	}

	public TriangulatedText withColor(int argb) {
		if(this.color == argb) {
			return this;
		}
		return new TriangulatedText(this.text, argb, this.font, this.metrics, this.triangulation);
	}

	public TriangulatedText withText(String text) {
		if(text.equals(this.text)) {
			return this;
		}
		return new TriangulatedText(text, this.color, this.font, this.metrics); // cannot re-use triangulation
	}

	public TriangulatedText withFont(Font font) {
		if(font.equals(this.font)) {
			return this;
		}
		return new TriangulatedText(this.text, this.color, font, metrics(font)); // cannot re-use triangulation
	}

	public TriangulatedText withBold() {
		if(this.isBold) {
			return this;
		}

		Font font = this.font.deriveFont(Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
		TriangulatedText text = this.withFont(font);
		text.isBold = true;
		return text;
	}

	public TriangulatedTextIcon asIcon() {
		return new TriangulatedTextIcon(this);
	}


	public float aspectRatio() {
		Triangulation triangulate = this.getTriangulation(1);
		return triangulate.width / triangulate.height;
	}

	public void forEach(VertexConsumer consumer, float flatness) {
		Triangulation triangulation = this.getTriangulation(flatness);
		float offX = triangulation.offX, offY = triangulation.offY, width = triangulation.width, height =
			                                                                                         triangulation.height;
		float[] triangles = triangulation.triangles;
		for(int i = 0; i < triangles.length; i += 2) {
			consumer.accept((triangles[i] + offX) / width, (triangles[i + 1] + offY) / height, this.color);
		}
	}

	private Triangulation getTriangulation(float flatness) {
		float clamped = JMath.clamp(flatness, 0, .999999f);
		int key = (int) (clamped * 5);
		Triangulation triangulation = this.triangulation[key];
		if(triangulation == null) {
			triangulation = new Triangulation();
			GlyphVector vector = this.font.createGlyphVector(this.metrics.getFontRenderContext(), this.text);
			Shape outline = vector.getOutline();
			ShapeTriangulator.triangulate(outline.getPathIterator(null, clamped), triangulation);
			this.triangulation[key] = triangulation;
		}
		return triangulation;
	}

	static synchronized FontMetrics metrics(Font font) {
		return IMAGE.getFontMetrics(font);
	}
}
