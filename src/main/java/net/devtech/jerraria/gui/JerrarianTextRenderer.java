package net.devtech.jerraria.gui;

import net.devtech.jerraria.client.render.text.TriangulatedText;
import net.devtech.jerraria.gui.api.TextRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;

public class JerrarianTextRenderer implements TextRenderer<TriangulatedText> {
	@Override
	public Icon createIcon(TriangulatedText text) {
		return text.asIcon();
	}
}
