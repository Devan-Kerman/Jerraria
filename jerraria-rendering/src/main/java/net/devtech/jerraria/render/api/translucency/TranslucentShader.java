package net.devtech.jerraria.render.api.translucency;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.ImageFormat;
import net.devtech.jerraria.render.api.types.FrameOut;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.render.api.types.V;
import net.devtech.jerraria.util.Id;
import org.jetbrains.annotations.Nullable;

/**
 * A shader that can use multiple methods of translucency
 */
public class TranslucentShader<T extends GlValue<?> & GlValue.Attribute> extends Shader<T> {
	public final TranslucencyStrategy strategy;

	@Nullable
	public final LinkedList linkedListUniforms;

	@Nullable
	public final SinglePassWeighted singlePassWeighted;

	public class LinkedList {
		public final V.UI<?> counter = uni(V.atomic_ui("counter"));
		public final Tex imgListHead = uni(Tex.img("imgListHead", DataType.UINT_IMAGE_2D, ImageFormat.R32UI));
		public final Tex translucencyBuffer = uni(Tex.img("translucencyBuffer", DataType.UINT_IMAGE_BUFFER, ImageFormat.RGBA32UI));
	}

	public class SinglePassWeighted {
		public final FrameOut accum = imageOutput("accum");
		public final FrameOut reveal = imageOutput("reveal");
	}

	protected TranslucentShader(Id id, VFBuilder<T> builder, Object context, TranslucencyStrategy strat) {
		super(id, builder, context);
		this.strategy = strat;
		this.linkedListUniforms = strat.calcIf(TranslucencyStrategy.LINKED_LIST, LinkedList::new);
		this.singlePassWeighted = strat.calcIf(TranslucencyStrategy.SINGLE_PASS_WEIGHTED_BLENDED, SinglePassWeighted::new);
		this.addParameter("defines", strat.name());
		if(strat == TranslucencyStrategy.LINKED_LIST) {
			this.putParameter("version", "430");
		} else if(strat == TranslucencyStrategy.SINGLE_PASS_WEIGHTED_BLENDED) {
			this.putParameter("version", "330");
		}
	}

	protected TranslucentShader(
		Shader<T> shader, SCopy method, TranslucencyStrategy strat) {
		super(shader, method);
		if(method.preserveUniforms && shader instanceof TranslucentShader t && t.strategy != strat) {
			throw new UnsupportedOperationException("Cannot convert between " + strat + " and " + t.strategy);
		}
		this.strategy = strat;
		this.linkedListUniforms = strat.calcIf(TranslucencyStrategy.LINKED_LIST, LinkedList::new);
		this.singlePassWeighted = strat.calcIf(TranslucencyStrategy.SINGLE_PASS_WEIGHTED_BLENDED, SinglePassWeighted::new);
	}
}
