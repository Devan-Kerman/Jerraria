package rendering.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.batik.ext.awt.g2d.AbstractGraphics2D;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.xmlgraphics.java2d.AbstractGraphicsConfiguration;

public class CustomGraphics2D extends AbstractGraphics2D {
	protected Graphics2D dummyGraphics;

	{
		BufferedImage bi
			= new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		dummyGraphics = bi.createGraphics();
	}


	public CustomGraphics2D() {
		super(true);
		this.gc = new GraphicContext();
	}

	@Override
	public void draw(Shape s) {
		System.out.println("draw " + s);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		System.out.println("draw img " + img);
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		System.out.println("draw img unbaked " + img);
	}

	@Override
	public void drawString(String str, float x, float y) {
		GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), str);
		drawGlyphVector(gv, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), iterator);
		drawGlyphVector(gv, x, y);
	}

	@Override
	public void fill(Shape s) {
		System.out.println("fill " + s);
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return null;
	}

	@Override
	public Graphics create() {
		return new CustomGraphics2D();
	}

	@Override
	public void setXORMode(Color c1) {
		System.out.println("xor");
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return this.dummyGraphics.getFontMetrics(f);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		System.out.println("copy");
	}

	class Test extends GraphicsConfiguration {

		@Override
		public GraphicsDevice getDevice() {
			return null;
		}

		@Override
		public ColorModel getColorModel() {
			return null;
		}

		@Override
		public ColorModel getColorModel(int transparency) {
			return null;
		}

		@Override
		public AffineTransform getDefaultTransform() {
			return null;
		}

		@Override
		public AffineTransform getNormalizingTransform() {
			return null;
		}

		@Override
		public Rectangle getBounds() {
			return null;
		}
	}

	Set<Image> test = new HashSet<>();
	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {

		if(test.add(img))
		System.out.println("img ");
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		if(test.add(img))
		System.out.println("img ");
		return false;
	}

	@Override
	public void dispose() {

	}
}
