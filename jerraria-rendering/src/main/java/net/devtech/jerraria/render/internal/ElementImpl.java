package net.devtech.jerraria.render.internal;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;

public record ElementImpl(int groupIndex, String name, DataType type, int location, int byteOffset)
	implements GlData.Element {}
