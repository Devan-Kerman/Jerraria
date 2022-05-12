package net.devtech.jerraria.render.textures;

import static org.lwjgl.opengl.GL31.*;
import java.io.IOException;
import java.nio.ByteBuffer;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.resource.VirtualFile;

/**
 * A texture or fraction of a gl texture
 */
public final class Texture {
	final int glId;
	final float offX, offY, width, height;

	Texture(int id, float x, float y, float width, float height) {
		this.glId = id;
		this.offX = x;
		this.offY = y;
		this.width = width;
		this.height = height;
	}

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

		//tell opengl how to unpack bytes
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		//set the texture parameters, can be GL_LINEAR or GL_NEAREST
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		//upload texture
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return new Texture(id, 0, 0, 1, 1);
	}

	public Texture section(float offX, float offY, float width, float height) {
		return new Texture(
			this.glId,
			this.offX + offX * this.width,
			this.offY + offY * this.width,
			width * this.width,
			height * this.height
		);
	}

	public int getGlId() {
		return this.glId;
	}

	/**
	 * @return normalized [0-1] offset
	 */
	public float getOffX() {
		return this.offX;
	}

	public float getOffY() {
		return this.offY;
	}

	public float getFudgedOffX() {
		return this.offX + .001f;
	}

	public float getFudgedOffY() {
		return this.offY + .001f;
	}

	public float getFudgedWidth() {
		return this.width - .002f;
	}

	public float getFudgedHeight() {
		return this.height - .002f;
	}

	/**
	 * @return normalized [0-1] width
	 */
	public float getWidth() {
		return this.width;
	}

	public float getHeight() {
		return this.height;
	}
}
