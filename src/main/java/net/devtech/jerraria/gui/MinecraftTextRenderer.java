package net.devtech.jerraria.gui;

import net.devtech.jerraria.gui.api.TextRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;

import net.minecraft.text.Text;

public class MinecraftTextRenderer implements TextRenderer<Text> {
	@Override
	public Icon createIcon(Text text, int argb) {
		return TextIcon.builder().color(argb).build(text);
	}

	@Override
	public Text createText(String string) {
		return Text.literal(string);
	}
}
