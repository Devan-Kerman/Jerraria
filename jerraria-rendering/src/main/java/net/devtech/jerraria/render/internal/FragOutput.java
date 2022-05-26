package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL33.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.Id;

public class FragOutput extends GlData {
	final int frameBuffer;
	final Map<String, OutputIndex> indices;
	final List<OutputBind> binds;

	public FragOutput(Map<String, BareShader.Field> outputs, int program, Id id) {
		int buffer = glGenFramebuffers();
		Map<String, OutputIndex> indices = new HashMap<>();
		List<OutputBind> binds = new ArrayList<>();
		for(BareShader.Field value : outputs.values()) {
			indices.put(value.name(), new OutputIndex(binds.size()));
			int location = glGetFragDataLocation(program, value.name());
			if(location == -1) {
				throw new UnsupportedOperationException("Could not find output by name \"" + value.name() + "\"");
			}
			binds.add(new OutputBind(value.type(), GL_COLOR_ATTACHMENT0 + location));
		}
		this.frameBuffer = buffer;
		this.indices = indices;
		this.binds = binds;
	}

	public FragOutput(FragOutput output) {
		GLReclamation.reclaimBuffers();
		int buffer = this.frameBuffer = glGenFramebuffers();
		this.indices = output.indices;
		this.binds = output.binds.stream().map(OutputBind::new).toList();
		GLReclamation.manageFrameBuffer(this, buffer);
	}

	@Override
	public Buf element(Element element) {
		OutputBind bind = this.binds.get(((OutputIndex) element).index);
		bind.rebind = true;
		return bind;
	}

	@Override
	public Element getElement(String name) {
		return this.indices.get(name);
	}

	public void copyDefaultDepth() {
		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		this.copyDepth(0, dims[2], dims[3]);
	}

	public void copyDepth(int frameBuffer, int width, int height) {
		GLContextState.bindReadFBO(frameBuffer);
		GLContextState.bindDrawFBO(this.frameBuffer);
		glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_DEPTH_BUFFER_BIT, GL_NEAREST);
	}

	public void bind() {
		GLContextState.bindFrameBuffer(this.frameBuffer);
		for(OutputBind bind : this.binds) {
			if(bind.rebind) {
				bind.attach();
			}
		}
	}

	public void flushBuffer() {
		GLContextState.bindFrameBuffer(this.frameBuffer);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}

	public record OutputIndex(int index) implements Element {}

	public static class OutputBind implements BufAdapter {
		final DataType imageType;
		final int attachment;
		boolean rebind;
		boolean clear;
		int texture;

		public OutputBind(DataType type, int attachment) {
			this.imageType = type;
			this.attachment = attachment;
		}

		public OutputBind(OutputBind outputBind) {
			this(outputBind.imageType, outputBind.attachment);
			this.rebind = true;
		}

		@Override
		public Buf i(int i) {
			this.texture = i;
			return this;
		}

		@Override
		public Buf bool(boolean b) {
			this.clear = b;
			return this;
		}

		public void attach() {
			glFramebufferTexture2D(GL_FRAMEBUFFER, this.attachment, this.imageType.elementType, this.texture, 0);
			this.rebind = false;
		}
	}
}
