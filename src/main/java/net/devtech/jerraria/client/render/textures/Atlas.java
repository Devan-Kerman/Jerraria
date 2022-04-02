package net.devtech.jerraria.client.render.textures;

import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.devtech.jerraria.client.ClientMain;
import net.devtech.jerraria.client.LoadRender;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;

public class Atlas {
	private static final Executor RENDER_THREAD_EXECUTOR = Runnable::run;
	static int copyFrameBufferId = -1;
	final int glId;
	final Map<String, ExactTexture> textureMap;
	final List<AnimatedTexture> animated;
	final int atlasWidth, atlasHeight;
	final Texture texture;

	protected Atlas(LoadRender render, Executor executor, VirtualFile.Directory source, Id atlasId) throws IOException {
		Map<String, RawImageSprite> imageSprites = new HashMap<>();
		List<RawAnimatedSprite> animatedSprites = new ArrayList<>();
		render.setTitleText("Building atlas " + atlasId + "[%d/%d]");
		render.setTaskSize(4);

		this.pullFromResources(render, source, atlasId, imageSprites, animatedSprites);

		Comparator<RawSprite> comp = Comparator
			.<RawSprite>comparingInt(s -> -s.width())
			.thenComparingInt(s -> -s.height());
		LoadRender sorting = render.substage("Sorting sprites", 1);
		List<RawSprite> sprites = new ArrayList<>(imageSprites.values());
		sprites.addAll(animatedSprites);
		sprites.sort(comp);
		sorting.setToComplete();
		render.complete(1);

		record Rect(int offX, int offY, int width, int height) {
			boolean canContain(RawSprite sprite) {
				return sprite.width() <= Rect.this.width & sprite.height() <= Rect.this.height;
			}
		}

		LoadRender organizing = render.substage("Organizing sprites [%d/%d]", sprites.size());
		List<Rect> space = new ArrayList<>();
		int atlasWidth = 0, atlasHeight = 0;
		Map<RawSprite, Rect> spriteMap = new HashMap<>();
		for(RawSprite sprite : sprites) {
			boolean spaceFound = false;
			for(int i = space.size() - 1; i >= 0; i--) {
				Rect rect = space.get(i);
				if(rect.canContain(sprite)) {
					space.remove(i); // remove used space
					atlasWidth = Math.max(atlasWidth, rect.offX + sprite.width());
					atlasHeight = Math.max(atlasHeight, rect.offY + sprite.height());
					if(rect.width > sprite.width()) {
						Rect spaceA = new Rect(rect.offX + sprite.width(),
							rect.offY,
							rect.width - sprite.width(),
							sprite.height());
						space.add(i, spaceA);
					}
					if(rect.height > sprite.height()) {
						Rect spaceB = new Rect(rect.offX,
							rect.offY + sprite.height(),
							rect.width,
							rect.height - sprite.height());
						space.add(i, spaceB);
					}

					spriteMap.put(sprite, new Rect(rect.offX, rect.offY, sprite.width(), sprite.height()));
					spaceFound = true;
					break;
				}
			}
			if(!spaceFound) {
				if(atlasHeight < atlasWidth) { // use up space below
					if(atlasWidth > sprite.width()) {
						Rect bottomSpace = new Rect(sprite.width(),
							atlasHeight,
							atlasWidth - sprite.width(),
							sprite.height());
						space.add(0, bottomSpace);
					} else if(atlasWidth < sprite.width()) {
						Rect sideSpace = new Rect(atlasWidth, 0, sprite.width() - atlasWidth, atlasHeight);
						space.add(0, sideSpace);
					}

					spriteMap.put(sprite, new Rect(0, atlasHeight, sprite.width(), sprite.height()));
					atlasHeight += sprite.height();
					atlasWidth = Math.max(atlasWidth, sprite.width());
				} else { // use up space on the side
					if(atlasHeight > sprite.height()) {
						Rect sideSpace = new Rect(atlasWidth,
							sprite.height(),
							sprite.width(),
							atlasHeight - sprite.height());
						space.add(0, sideSpace);
					} else if(atlasHeight < sprite.height()) {
						Rect bottomSpace = new Rect(0, atlasHeight, atlasWidth, sprite.height() - atlasHeight);
						space.add(0, bottomSpace);
					}

					spriteMap.put(sprite, new Rect(atlasWidth, 0, sprite.width(), sprite.height()));
					atlasHeight = Math.max(atlasHeight, sprite.height());
					atlasWidth += sprite.width();
				}
			}
			organizing.complete(1);
		}
		organizing.setToComplete();
		render.complete(1);

		int finalAtlasWidth = atlasWidth;
		int finalAtlasHeight = atlasHeight;
		CompletableFuture<Integer> glFuture = CompletableFuture.supplyAsync(() -> {
			int glId = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, glId);
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D,
				0,
				GL_RGBA,
				finalAtlasWidth,
				finalAtlasHeight,
				0,
				GL_RGBA,
				GL_UNSIGNED_BYTE,
				(ByteBuffer) null);
			return glId;
		}, executor);

		LoadRender uploading = render.substage("Uploading sprites [%d/%d]", spriteMap.size());
		Map<String, ExactTexture> textureMap = new HashMap<>();
		List<AnimatedTexture> animated = new ArrayList<>();
		float fwidth = atlasWidth;
		float fheight = atlasHeight;
		List<CompletableFuture<?>> futures = new ArrayList<>();
		spriteMap.forEach((sprite, rect) -> {
			if(sprite instanceof RawImageSprite s) {
				CompletableFuture<Void> after = glFuture.thenAcceptAsync(glId -> {
					Texture texture = new Texture(glId,
						rect.offX / fwidth,
						rect.offY / fheight,
						rect.width / fwidth,
						rect.height / fheight);
					textureMap.put(s.name, new ExactTexture(texture, rect.offX, rect.offY, rect.width, rect.height));
					glBindTexture(GL_TEXTURE_2D, glId);
					glTexSubImage2D(GL_TEXTURE_2D,
						0,
						rect.offX,
						rect.offY,
						rect.width,
						rect.height,
						GL_RGBA,
						GL_UNSIGNED_BYTE,
						s.buffer);
					uploading.complete(1);
				}, executor);
				futures.add(after);
			}
		});

		CompletableFuture<Void> images = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
		List<CompletableFuture<Void>> animations = new ArrayList<>();
		spriteMap.forEach((sprite, rect) -> {
			if(sprite instanceof RawAnimatedSprite s) {
				animations.add(images.thenRunAsync(() -> {
					int glId = glFuture.join();
					Texture texture = new Texture(glId,
						rect.offX / fwidth,
						rect.offY / fheight,
						rect.width / fwidth,
						rect.height / fheight);
					ExactTexture exact = new ExactTexture(texture, rect.offX, rect.offY, rect.width, rect.height);
					ExactTexture put = textureMap.put(s.originalImage.name, exact);
					if(put != null) {
						textureMap.put(s.originalImage.name + "_atlas", put);
					} else {
						throw new IllegalStateException("Animated Sprite with no animation atlas image? " + s.originalImage.name);
					}

					AnimatedTexture animatedTexture = new AnimatedTexture(put,
						exact,
						s.frames,
						s.msPerFrame,
						Arrays.stream(s.msPerFrame).sum());
					glBindTexture(GL_TEXTURE_2D, glId);
					animatedTexture.update(glId, 0);
					animated.add(animatedTexture);
					uploading.complete(1);
				}, executor));
			}
		});

		this.glId = glFuture.join();
		this.textureMap = textureMap;
		this.animated = animated;
		this.atlasWidth = atlasWidth;
		this.atlasHeight = atlasHeight;
		CompletableFuture.allOf(animations.toArray(CompletableFuture[]::new)).join();
		uploading.setToComplete();
		render.setToComplete();
		this.texture = new Texture(this.glId, 0, 0, 1, 1);
	}

	public static Atlas createAtlas(Id atlasId) {
		try {
			return new Atlas(LoadRender.create("Stitching " + atlasId, 1),
				RENDER_THREAD_EXECUTOR,
				ClientMain.clientResources,
				atlasId);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public static Atlas createAtlas(LoadRender render, Executor renderThreadExecutor, Id atlasId) {
		try {
			return new Atlas(render, renderThreadExecutor, ClientMain.clientResources, atlasId);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public void updateAnimation(long timeSrc) {
		glBindTexture(GL_TEXTURE_2D, this.glId);
		for(AnimatedTexture animatedTexture : this.animated) {
			animatedTexture.update(glId, timeSrc);
		}
	}

	public Texture getTexture(String name) {
		return this.textureMap.get(name).texture;
	}

	public Texture asTexture() {
		return this.texture;
	}

	public int glId() {
		return this.glId;
	}

	private void pullFromResources(
		LoadRender render,
		VirtualFile.Directory source,
		Id atlasId,
		Map<String, RawImageSprite> imageSprites,
		List<RawAnimatedSprite> animatedSprites) throws IOException {
		List<VirtualFile.Regular> animations = new ArrayList<>(); // process at end
		List<VirtualFile> toProcess = new ArrayList<>();
		List<VirtualFile> atlasDefinitions = source
			.resolveDirectory(atlasId.unpackNamespace())
			.resolveDirectory("atlas")
			.resolveAll(atlasId.getUnpackedPath() + ".txt");

		LoadRender definitions = render.substage("Reading definitions [%d/%d]", atlasDefinitions.size());
		for(VirtualFile definition : atlasDefinitions) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(definition.asRegular().read()))) {
				reader.lines().map(source::resolve).forEach(toProcess::add);
			}
			definitions.complete(1);
		}

		LoadRender reading = render.substage("Parsing Atlas Assets [%d/%d]", toProcess.size());
		int taskSize = 0;
		while(!toProcess.isEmpty()) {
			taskSize += toProcess.size();
			reading.setTaskSize(taskSize);
			for(int i = toProcess.size() - 1; i >= 0; i--) {
				VirtualFile process = toProcess.get(i);
				if(process instanceof VirtualFile.Directory d) {
					toProcess.addAll(d.children());
				} else {
					VirtualFile.Regular file = process.asRegular();
					if(file.hasFileExtension(Validate.PROPERTIES_FILE_EXTENSION)) {
						animations.add(file);
					} else if(file.hasFileExtension("png")) {
						try(InputStream input = file.read()) {
							PNGDecoder decoder = new PNGDecoder(input);
							ByteBuffer buffer =
								ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
							decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
							buffer.flip();
							String name = file.withoutExtension();
							imageSprites.put(name, new RawImageSprite(name, buffer, decoder.getWidth(), decoder.getHeight()));
						} catch(UnsupportedOperationException e) {
							System.out.println(e.getLocalizedMessage() + " " + file.name());
						}
					}
				}
				toProcess.remove(i);
				reading.complete(1);
			}
		}

		taskSize += animations.size();
		reading.setTaskSize(taskSize);
		for(VirtualFile.Regular animation : animations) {
			try(InputStream stream = animation.read()) {
				Properties properties = new Properties();
				properties.load(stream);
				String name = animation.withoutExtension();
				RawImageSprite sprite = imageSprites.get(name);
				int frames = Integer.parseInt(properties.getProperty("frames"));
				int[] msPerFrame = Arrays
					.stream(properties.getProperty("msPerFrame").split(","))
					.mapToInt(Integer::parseInt)
					.toArray();
				if(sprite.height % frames != 0) {
					throw new IllegalStateException(name + "'s height (" + sprite.height + ") is not divisible by the number of frames (" + frames + ")");
				}
				RawAnimatedSprite animated = new RawAnimatedSprite(sprite, frames, msPerFrame);
				animatedSprites.add(animated);
			}
			reading.complete(1);
		}
	}

	interface RawSprite {
		int width();

		int height();
	}

	record AnimatedTexture(ExactTexture source,
	                       ExactTexture destination,
	                       int frames,
	                       int[] msPerFrame,
	                       int msPerRotation) {
		public void update(int glId, long timeSrc) {
			long time = timeSrc % this.msPerRotation;
			int frame = 0;
			for(int ms : msPerFrame) {
				if(ms > time) {
					break;
				}
				time += ms;
				frame++;
			}

			if(copyFrameBufferId == -1) {
				copyFrameBufferId = glGenFramebuffers();
			}

			glBindFramebuffer(GL_READ_FRAMEBUFFER, copyFrameBufferId);
			glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glId, 0);
			glCopyTexSubImage2D(GL_TEXTURE_2D,
				0,
				destination.offX,
				destination.offY,
				source.offX,
				source.offY + destination.height * frame,
				destination.width,
				destination.height);
		}
	}

	record RawImageSprite(String name, ByteBuffer buffer, int width, int height) implements RawSprite {}

	record RawAnimatedSprite(RawImageSprite originalImage, int frames, int[] msPerFrame) implements RawSprite {
		@Override
		public int width() {
			return RawAnimatedSprite.this.originalImage.width();
		}

		@Override
		public int height() {
			return RawAnimatedSprite.this.originalImage.height() / RawAnimatedSprite.this.frames;
		}
	}

	static class ExactTexture {
		final Texture texture;
		final int offX, offY, width, height;

		ExactTexture(Texture texture, int x, int y, int width, int height) {
			this.texture = texture;
			this.offX = x;
			this.offY = y;
			this.width = width;
			this.height = height;
		}
	}
}
