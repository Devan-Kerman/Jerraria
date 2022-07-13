package net.devtech.jerraria.client.render.textures;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL31.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL31.GL_NEAREST;
import static org.lwjgl.opengl.GL31.GL_RGBA;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL31.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL31.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL31.glBindTexture;
import static org.lwjgl.opengl.GL31.glCopyTexSubImage2D;
import static org.lwjgl.opengl.GL31.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL31.glGenFramebuffers;
import static org.lwjgl.opengl.GL31.glGenTextures;
import static org.lwjgl.opengl.GL31.glPixelStorei;
import static org.lwjgl.opengl.GL31.glTexImage2D;
import static org.lwjgl.opengl.GL31.glTexParameterf;
import static org.lwjgl.opengl.GL31.glTexSubImage2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.LoadRender;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.api.textures.Texture;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;

public class Atlas {
	static final ServiceLoader<DynamicAtlasTextureProvider> DYNAMIC_ATLAS_TEXTURE_PROVIDERS = ServiceLoader.load(
		DynamicAtlasTextureProvider.class);

	static List<DynamicAtlasTexture> textures(Atlas atlas, Id atlasId) {
		List<DynamicAtlasTexture> textures = new ArrayList<>();
		for(DynamicAtlasTextureProvider provider : DYNAMIC_ATLAS_TEXTURE_PROVIDERS) {
			provider.provideTextures(atlasId, texture -> {
				textures.add(texture);
				return () -> atlas.redrawQueue.add(texture);
			});
		}
		return textures;
	}

	private static final Executor RENDER_THREAD_EXECUTOR = Runnable::run;
	static int copyFrameBufferId = -1;
	static final Map<Id, Atlas> ATLASES = new ConcurrentHashMap<>();
	final int glId;
	final Map<String, ExactTexture> textureMap;
	final List<AnimatedTexture> animated;
	final int atlasWidth, atlasHeight;
	final Texture texture;
	final Set<DynamicAtlasTexture> redrawQueue = Collections.newSetFromMap(new ConcurrentHashMap<>());

	protected Atlas(LoadRender render, Executor executor, VirtualFile.Directory source, Id atlasId) throws IOException {
		Map<String, RawImageSprite> imageSprites = new HashMap<>();
		List<RawAnimatedSprite> animatedSprites = new ArrayList<>();
		render.setTitleText("Building atlas " + atlasId + "[%d/%d]");
		render.setTaskSize(4);

		List<DynamicAtlasTexture> textures = textures(this, atlasId); // todo implement for button noise
		this.pullFromResources(render, source, atlasId, imageSprites, animatedSprites);

		Comparator<RawSprite> comp = Comparator
			                             .<RawSprite>comparingInt(s -> -s.width())
			                             .thenComparingInt(s -> -s.height());
		LoadRender sorting = render.substage("Sorting sprites", 1);
		List<RawSprite> sprites = new ArrayList<>(imageSprites.size() + animatedSprites.size() + textures.size());
		sprites.addAll(imageSprites.values());
		sprites.addAll(animatedSprites);
		sprites.addAll(textures);

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
							sprite.height()
						);
						space.add(i, spaceA);
					}
					if(rect.height > sprite.height()) {
						Rect spaceB = new Rect(rect.offX,
							rect.offY + sprite.height(),
							rect.width,
							rect.height - sprite.height()
						);
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
							sprite.height()
						);
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
							atlasHeight - sprite.height()
						);
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
		CompletableFuture<Integer> atlasFuture = CompletableFuture.supplyAsync(() -> {
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
				(ByteBuffer) null
			);
			return glId;
		}, executor);

		LoadRender uploading = render.substage("Uploading sprites [%d/%d]", spriteMap.size());
		Map<String, ExactTexture> textureMap = new HashMap<>();
		List<AnimatedTexture> animated = new ArrayList<>();
		float fwidth = atlasWidth;
		float fheight = atlasHeight;
		List<CompletableFuture<?>> imageList = new ArrayList<>();
		spriteMap.forEach((sprite, rect) -> {
			if(sprite instanceof RawImageSprite s) {
				CompletableFuture<Void> after = atlasFuture.thenAcceptAsync(glId -> {
					Texture texture = new Texture(glId, rect.offX / fwidth, // it just works:tm:
						rect.offY / fheight, rect.width / fwidth, rect.height / fheight
					);
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
						s.buffer
					);
					uploading.complete(1);
				}, executor);
				imageList.add(after);
			}
		});

		CompletableFuture<Void> staticImages = CompletableFuture.allOf(imageList.toArray(CompletableFuture[]::new));
		spriteMap.forEach((sprite, rect) -> {
			if(sprite instanceof RawAnimatedSprite s) {
				int glId = atlasFuture.join();
				Texture texture = new Texture(glId,
					rect.offX / fwidth,
					rect.offY / fheight,
					rect.width / fwidth,
					rect.height / fheight
				);
				ExactTexture exact = new ExactTexture(texture, rect.offX, rect.offY, rect.width, rect.height);
				ExactTexture unanimated = textureMap.put(s.originalImage.name, exact);
				if(unanimated != null) {
					textureMap.put(s.originalImage.name + "_atlas", unanimated);
				} else {
					throw new IllegalStateException("Animated Sprite with no animation atlas image? "
					                                + s.originalImage.name);
				}

				AnimatedTexture animatedTexture = new AnimatedTexture(unanimated,
					exact,
					s.frames,
					s.msPerFrame,
					Arrays.stream(s.msPerFrame).sum()
				);
				animated.add(animatedTexture);
				uploading.complete(1);
			} else if(sprite instanceof DynamicAtlasTexture s) {
				int glId = atlasFuture.join();
				Texture texture = new Texture(glId,
					rect.offX / fwidth,
					rect.offY / fheight,
					rect.width / fwidth,
					rect.height / fheight
				);
				ExactTexture exact = new ExactTexture(texture, rect.offX, rect.offY, rect.width, rect.height);
				textureMap.put(s.id(), exact);
				this.redrawQueue.add(s);
				uploading.complete(1);
			}
		});

		this.glId = atlasFuture.join();
		staticImages.join();
		this.animated = animated;
		this.textureMap = textureMap;
		CompletableFuture.runAsync(() -> this.updateAnimation(0, () -> uploading.complete(1)), executor).join();
		uploading.setToComplete();
		render.setToComplete();
		this.atlasWidth = atlasWidth;
		this.atlasHeight = atlasHeight;
		this.texture = new Texture(this.glId, 0, 0, 1, 1);
		ATLASES.put(atlasId, this);
	}

	public static Atlas createAtlas(Id atlasId) {
		try {
			return new Atlas(LoadRender.create("Stitching " + atlasId, 1),
				RENDER_THREAD_EXECUTOR,
				Bootstrap.clientResources,
				atlasId
			);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public static Atlas createAtlas(LoadRender render, Executor renderThreadExecutor, Id atlasId) {
		try {
			return new Atlas(render, renderThreadExecutor, Bootstrap.clientResources, atlasId);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
	}

	public static Map<Id, Atlas> getAtlases() {
		return Collections.unmodifiableMap(ATLASES);
	}

	public static Atlas getById(Id atlasId) {
		return ATLASES.get(atlasId);
	}

	public void updateAnimation(long timeSrc, Runnable callback) {
		bindFramebuffer(this.glId);
		for(AnimatedTexture animatedTexture : this.animated) {
			animatedTexture.update(timeSrc);
			callback.run();
		}

		int framebuffer = GLContextState.getDefaultFramebuffer();
		GLContextState.setAndBindDefaultFrameBuffer(copyFrameBufferId);
		try {
			Mat mat = Mat.create();
			BatchedRenderer renderer = BatchedRenderer.immediate();
			for(DynamicAtlasTexture texture : this.redrawQueue) {
				Texture tex = this.textureMap.get(texture.id()).texture;
				mat.identity().offset(tex.getOffX(), tex.getOffY());
				texture.drawer().draw(renderer, mat);
				callback.run();
			}
			renderer.flush();
			this.redrawQueue.removeIf(DynamicAtlasTexture::isStatic);
		} finally {
			GLContextState.setDefaultFrameBuffer(framebuffer);
		}
	}

	public Texture getTexture(String name) {
		ExactTexture exactTexture = this.textureMap.get(name);
		if(exactTexture == null) {
			throw new IllegalArgumentException("Texture with name "
			                                   + name
			                                   + " not found, must be one of "
			                                   + this.textureMap.keySet());
		}
		return exactTexture.texture;
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
			                                     .resolveDirectory(atlasId.mod())
			                                     .resolveDirectory("atlas")
			                                     .resolveAll(atlasId.path() + ".txt");

		LoadRender definitions = render.substage("Reading definitions [%d/%d]", atlasDefinitions.size());
		for(VirtualFile definition : atlasDefinitions) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(definition.asRegular().read()))) {
				reader.lines().map(source::resolve).filter(Objects::nonNull).forEach(toProcess::add);
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
							int width = decoder.getWidth();
							int capacity = 4 * width * decoder.getHeight();
							ByteBuffer buf = ByteBuffer.allocateDirect(capacity);
							decoder.decode(buf, width * 4, PNGDecoder.Format.RGBA);
							buf.flip();
							String name = file.withoutExtension();
							imageSprites.put(name, new RawImageSprite(name, buf, width, decoder.getHeight()));
						} catch(Exception e) {
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
					throw new IllegalStateException(name
					                                + "'s height ("
					                                + sprite.height
					                                + ") is not divisible by the number of frames ("
					                                + frames
					                                + ")");
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

	record AnimatedTexture(
		ExactTexture source, ExactTexture destination, int frames, int[] msPerFrame, int msPerRotation
	) {
		public void update(long timeSrc) {
			long time = timeSrc % this.msPerRotation;
			int frame = 0;
			for(int ms : this.msPerFrame) {
				if(ms > time) {
					break;
				}
				time += ms;
				frame++;
			}

			glCopyTexSubImage2D(GL_TEXTURE_2D,
				0,
				this.destination.offX,
				this.destination.offY,
				this.source.offX,
				this.source.offY + this.destination.height * frame,
				this.destination.width,
				this.destination.height
			);
		}
	}

	static void bindFramebuffer(int glId) {
		glBindTexture(GL_TEXTURE_2D, glId);
		if(copyFrameBufferId == -1) {
			copyFrameBufferId = glGenFramebuffers();
		}
		GLContextState.bindFrameBuffer(copyFrameBufferId);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, glId, 0);
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
