package net.devtech.jerraria.gui;

import net.devtech.jerraria.client.render.text.TriangulatedText;
import net.devtech.jerraria.gui.api.TextRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;

public class JerrarianTextRenderer implements TextRenderer<TriangulatedText> {
	public static final TextRenderer<TriangulatedText> TEXT_RENDERER = new JerrarianTextRenderer();

	@Override
	public Icon createIcon(TriangulatedText text, int argb) {
		return text.withColor(argb).asIcon();
	}

	@Override
	public TriangulatedText createText(String string) {
		return TriangulatedText.cached(string);
	}
}
