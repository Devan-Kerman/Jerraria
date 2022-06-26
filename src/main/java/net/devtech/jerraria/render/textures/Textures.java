package net.devtech.jerraria.render.textures;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.resource.VirtualFile;

public class Textures {
	/**
	 * @see Atlas
	 * @see JerrariaClient#MAIN_ATLAS
	 */
	public static Texture loadTexture(VirtualFile.Directory directory, String texture) throws IOException {
		// load png file
		VirtualFile.Regular regular = directory
			.resolveFile(texture);

		PNGDecoder decoder = new PNGDecoder(regular.read());

		//create a byte buffer big enough to store RGBA values
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());

		//decode
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);

		//flip the buffer so its ready to read
		buffer.flip();

		//create a texture
		int id = glGenTextures();

		//bind the texture
		glBindTexture(GL_TEXTURE_2D, id);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return new Texture(id, 0, 0, 1, 1);
	}
}
