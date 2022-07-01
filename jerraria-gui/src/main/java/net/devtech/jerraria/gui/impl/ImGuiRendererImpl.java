package net.devtech.jerraria.gui.impl;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.devtech.jerraria.gui.api.ImGuiRenderer;
import net.devtech.jerraria.gui.api.TextRenderer;
import net.devtech.jerraria.gui.api.TopState;
import net.devtech.jerraria.gui.api.SubdivisionStack;
import net.devtech.jerraria.gui.api.SubdivisionState;
import net.devtech.jerraria.gui.api.input.InputState;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.render.api.batch.RenderListBatchedRenderer;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatView;
import net.devtech.jerraria.util.math.Mat2x3f;

public class ImGuiRendererImpl extends ImGuiRenderer {
	final Mat base;
	final Mat2x3f mat = new Mat2x3f(); // todo fix this matrix
	final Stack<SubdivisionEntry> entryStack = new ObjectArrayList<>();
	final SubdivisionStack stack = new SubdivisionStackImpl();
	final TopStateImpl topState = new TopStateImpl();
	final RenderListBatchedRenderer tooltipRenderer = new RenderListBatchedRenderer();
	final BatchedRenderer immediateRenderer;
	BatchedRenderer renderer;
	final TextRenderer<?> textRenderer;
	final InputState inputState;
	float drawSpaceWidth;
	float drawSpaceHeight;

	public ImGuiRendererImpl(Mat mat, TextRenderer<?> renderer) {
		this.textRenderer = renderer;
		this.renderer = this.immediateRenderer = BatchedRenderer.immediate();
		this.inputState = ImGuiController.CONTROLLER.createInputState(this);
		SubdivisionEntry vertical = new SubdivisionEntry();
		vertical.isVertical = true;
		this.entryStack.push(vertical);
		this.base = mat;
		this.mat.load(this.base);
	}

	final class SubdivisionEntry extends SubdivisionState {
		boolean isVertical;
		boolean absolute;
		float offsetX, offsetY;
		float width, height;

		SubdivisionEntry() {}
		SubdivisionEntry(SubdivisionEntry entry) {
			this.absolute = true;
			this.isVertical = entry.isVertical;
			this.offsetX = entry.offsetX;
			this.offsetY = entry.offsetY;
			this.width = entry.width;
			this.height = entry.height;
		}

		@Override
		public void assertOwnership(ImGuiRenderer renderer) {
			if(renderer != ImGuiRendererImpl.this) {
				throw new IllegalArgumentException("Subdivision Reference Exists for " + ImGuiRendererImpl.this + " not for " + renderer);
			}
		}
	}

	@Override
	public MatView mat() {
		return this.mat;
	}

	@Override
	public TextRenderer<?> getTextRenderer() {
		return this.textRenderer;
	}

	@Override
	public boolean isVertical() {
		return this.entryStack.top().isVertical;
	}

	@Override
	public SubdivisionStack absolute(float x, float y) {
		SubdivisionEntry entry = new SubdivisionEntry();
		SubdivisionEntry top = this.entryStack.top();
		entry.isVertical = top.isVertical;
		entry.offsetX = top.offsetX + x;
		entry.offsetY = top.offsetY + y;
		entry.absolute = true;
		this.entryStack.push(entry);
		return this.stack;
	}

	@Override
	public SubdivisionStack vertical() {
		this.addEntry(true);
		return this.stack;
	}

	@Override
	public SubdivisionStack horizontal() {
		this.addEntry(false);
		return this.stack;
	}

	@Override
	public TopState top() {
		return this.topState.push();
	}

	private void addEntry(boolean isVertical) {
		SubdivisionEntry entry = new SubdivisionEntry();
		entry.isVertical = isVertical;
		SubdivisionEntry top = this.entryStack.top();
		entry.offsetX = top.offsetX;
		entry.offsetY = top.offsetY;
		this.entryStack.push(entry);
	}

	@Override
	public void drawSpace(float width, float height) {
		SubdivisionEntry peek = this.entryStack.peek(0);
		this.mat.load(this.base);
		this.mat.offset(peek.offsetX, peek.offsetY);
		// todo lazy/deferred drawSpace
		this.drawSpaceWidth = width;
		this.drawSpaceHeight = height;
		if(peek.isVertical) {
			peek.offsetY += height;
			peek.height += height;
			peek.width = Math.max(peek.width, width);
		} else {
			peek.offsetX += width;
			peek.width += width;
			peek.height = Math.max(peek.height, height);
		}
	}

	@Override
	public float drawSpaceWidth() {
		return this.drawSpaceWidth;
	}

	@Override
	public float drawSpaceHeight() {
		return this.drawSpaceHeight;
	}

	@Override
	public InputState inputState() {
		return this.inputState;
	}

	@Override
	public void raise() {
	}

	@Override
	public SubdivisionStack gotoReference(SubdivisionState reference) {
		reference.assertOwnership(this);
		this.entryStack.push((SubdivisionEntry) reference);
		return this.stack;
	}

	@Override
	public SubdivisionState createReference() {
		return new SubdivisionEntry(this.entryStack.peek(0));
	}

	@Override
	public <T extends Shader<?>> T getBatch(ShaderKey<T> key) {
		return this.renderer.getBatch(key);
	}

	@Override
	public void drawKeep(Consumer<Shader<?>> configurator) {
		this.renderer.drawKeep(configurator);
	}

	@Override
	public void draw(Consumer<Shader<?>> consumer) {
		this.immediateRenderer.flush();
		this.tooltipRenderer.draw(consumer);
	}

	protected void pop() {
		SubdivisionEntry pop = this.entryStack.pop();
		if(pop.absolute) {
			return;
		}
		SubdivisionEntry parent = this.entryStack.peek(0);
		if(parent.isVertical) {
			parent.offsetY += pop.height;
			parent.height += pop.height;
			parent.width = Math.max(parent.width, pop.width);
		} else {
			parent.offsetX += pop.width;
			parent.width += pop.width;
			parent.height = Math.max(parent.height, pop.height);
		}
	}

	final class SubdivisionStackImpl extends SubdivisionStack {
		public SubdivisionStackImpl() {
		}

		@Override
		protected void pop() {
			ImGuiRendererImpl.this.pop();
		}
	}

	final class TopStateImpl extends TopState {
		int refCounter = 0;

		public TopState push() {
			if(this.refCounter++ == 0) {
				ImGuiRendererImpl.this.renderer = ImGuiRendererImpl.this.tooltipRenderer;
			}
			return this;
		}

		@Override
		protected void pop() {
			if(--this.refCounter <= 0) {
				ImGuiRendererImpl.this.renderer = ImGuiRendererImpl.this.immediateRenderer;
				this.refCounter = 0;
			}
		}
	}
}
