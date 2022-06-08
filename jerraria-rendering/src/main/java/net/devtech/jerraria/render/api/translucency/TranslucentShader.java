package net.devtech.jerraria.render.api.translucency;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.ImageFormat;
import net.devtech.jerraria.render.api.types.AtomicCounter;
import net.devtech.jerraria.render.api.types.Out;
import net.devtech.jerraria.render.api.types.Tex;
import org.jetbrains.annotations.Nullable;

/**
 * A shader that can use multiple methods of translucency
 * @see TranslucencyRenderer
 */
public abstract class TranslucentShader<T extends GlValue<?> & GlValue.Attribute> extends Shader<T> {
	public final TranslucentShaderType type;

	TranslucentShader<?> secondPass;

	@Nullable
	public final LinkedList linkedListUniforms;

	@Nullable
	public final SinglePassWeighted singlePassWeighted;

	@Nullable
	public final DoublePassWeightedA doublePassWeightedA;

	@Nullable
	public final DoublePassWeightedB doublePassWeightedB;

	public final class LinkedList {
		public final AtomicCounter counter = uni(AtomicCounter.atomic_ui("counter", true));
		public final Tex imgListHead = uni(Tex.img("imgListHead", DataType.UINT_IMAGE_2D, ImageFormat.R32UI));
		public final Tex translucencyBuffer = uni(Tex.img("translucencyBuffer", DataType.UINT_IMAGE_BUFFER, ImageFormat.RGBA32UI));
	}

	public final class SinglePassWeighted {
		public final Out accum = imageOutput("accum");
		public final Out reveal = imageOutput("reveal");
	}

	public final class DoublePassWeightedA {
		public final Out accum = imageOutput("accum");
	}

	public final class DoublePassWeightedB {
		public final Out reveal = imageOutput("reveal");
	}

	protected TranslucentShader(VFBuilder<T> builder, Object context, TranslucentShaderType strat) {
		super(builder, context);
		this.type = strat;
		this.linkedListUniforms = strat.calcIf(TranslucentShaderType.LINKED_LIST, LinkedList::new);
		this.singlePassWeighted = strat.calcIf(TranslucentShaderType.SINGLE_PASS, SinglePassWeighted::new);
		this.doublePassWeightedA = strat.calcIf(TranslucentShaderType.DOUBLE_PASS_A, DoublePassWeightedA::new);
		this.doublePassWeightedB = strat.calcIf(TranslucentShaderType.DOUBLE_PASS_B, DoublePassWeightedB::new);
		this.addParameter("defines", strat.name());
		this.putParameter("version", strat.glslVers);
	}

	protected TranslucentShader(Shader<T> shader, SCopy method, TranslucentShaderType strat) {
		super(shader, method);
		if(method.preserveUniforms && shader instanceof TranslucentShader t && t.type != strat) {
			throw new UnsupportedOperationException("Cannot convert between " + strat + " and " + t.type);
		}
		this.type = strat;
		this.linkedListUniforms = strat.calcIf(TranslucentShaderType.LINKED_LIST, LinkedList::new);
		this.singlePassWeighted = strat.calcIf(TranslucentShaderType.SINGLE_PASS, SinglePassWeighted::new);
		this.doublePassWeightedA = strat.calcIf(TranslucentShaderType.DOUBLE_PASS_A, DoublePassWeightedA::new);
		this.doublePassWeightedB = strat.calcIf(TranslucentShaderType.DOUBLE_PASS_B, DoublePassWeightedB::new);
		this.secondPass = ((TranslucentShader<?>) shader).secondPass;
	}

	@Override
	public void flushFrameBuffer() {
		super.flushFrameBuffer();
		if(this.secondPass != null) {
			this.secondPass.flushFrameBuffer();
		}
	}
}
