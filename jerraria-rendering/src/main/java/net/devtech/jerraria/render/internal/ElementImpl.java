package net.devtech.jerraria.render.internal;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;

/**
 * @param arrayIndex only used by variable length arrays, is -1 for everything, -2 for fixed size SSBOs and >-1 for actual elements
 */
public record ElementImpl(int groupIndex, String name, DataType type, int location, int byteOffset, int arrayIndex)
	implements GlData.Element {

	public ElementImpl(ElementImpl element, int index) {
		this(element.groupIndex, element.name, element.type, element.location, element.byteOffset, index);
	}
}
