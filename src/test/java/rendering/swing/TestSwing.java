package rendering.swing;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import org.apache.batik.ext.awt.g2d.DefaultGraphics2D;

public class TestSwing {
	public static void main(String[] args) throws IOException {
		JButton button = new JButton("The Test-Button");
		button.setSize(200, 70);
		button.setLocation(200, 150);
		BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		image.addTileObserver((source, tileX, tileY, willBeWritable) -> {
			System.out.println("update!");
		});
		Graphics2D graphics = image.createGraphics();
		button.paint(graphics);
		graphics.dispose();
		image.flush();
		ImageIO.write(image, "PNG", new File("test.png"));
		// Write BufferedImage to a PNG file
		//try {
		//
		//} catch(IOException e) {
		//	throw new RuntimeException(e);
		//}
	}
}
