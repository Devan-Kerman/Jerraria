package net.devtech.jerraria.gui.api.widgets;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.gui.api.ImGuiRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.util.math.JMath;

public class Menu {
	public static Builder vertical(float width) {
		return new Builder(width, true);
	}

	public static Builder horizontal(float height) {
		return new Builder(height, false);
	}

	record Setting(Button.Settings settings, float width, float height) {}

	public static final class Builder {
		final List<Setting> settings = new ArrayList<>();
		final float baseDimension;
		final boolean isVertical;

		Builder(float baseDimension, boolean vertical) {
			this.baseDimension = baseDimension;
			this.isVertical = vertical;
		}

		/**
		 * @param size if you are building a horizontal menu, the size is the width, and for vertical menus it's the
		 * 	height
		 */
		public Builder tab(Button.Settings settings, float size) {
			if(this.isVertical) {
				this.settings.add(new Setting(settings, this.baseDimension, size));
			} else {
				this.settings.add(new Setting(settings, size, this.baseDimension));
			}
			return this;
		}

		/**
		 * Uses {@link Icon#aspectRatio()} to automatically determine the size of the button
		 */
		public Builder tab(Button.Settings settings) {
			if(this.isVertical) {
				this.settings.add(new Setting(settings,
					this.baseDimension,
					this.baseDimension / settings.defaultState.aspectRatio()
				));
			} else {
				this.settings.add(new Setting(settings,
					this.baseDimension * settings.defaultState.aspectRatio(),
					this.baseDimension
				));
			}
			return this;
		}

		public void removeButton(int index) {
			this.settings.remove(index);
		}
	}

	public static int tabList(ImGuiRenderer gui, Builder builder, int currentIndex) {
		return tabList(gui, 1, builder, currentIndex);
	}

	/**
	 * <pre><b>Example Usage:</b></pre>
	 * <pre>{@code
	 *  switch(this.tab = Menu.tabList(gui, 10, MENU, this.tab)) {
	 *      case 0 -> {
	 *      	if(Button.button(gui, 80, 70, CONFIG2)) {
	 *      		System.out.println("Tab 1");
	 *          }
	 *      }
	 *      case 1 -> {
	 *      	if(Button.button(gui, 80, 70, CONFIG3)) {
	 *      		System.out.println("Tab 2");
	 *          }
	 *      }
	 *      case 2 -> {
	 *      	if(Button.button(gui, 80, 70, CONFIG4)) {
	 *      		System.out.println("Tab 3");
	 *          }
	 *      }
	 *  }
	 * }</pre>
	 *
	 * @param scale how big to scale the tab list
	 * @param currentIndex the currently selected tab
	 * @return the newly selected tab
	 */
	public static int tabList(ImGuiRenderer gui, float scale, Builder builder, int currentIndex) {
		currentIndex = JMath.clamp(currentIndex, 0, builder.settings.size());
		try((builder.isVertical ? gui.vertical() : gui.horizontal()).pop) {
			List<Setting> list = builder.settings;
			for(int i = 0; i < list.size(); i++) {
				Setting setting = list.get(i);
				if(Button.toggleButton(gui,
					setting.width * scale,
					setting.height * scale,
					setting.settings,
					i == currentIndex
				)) {
					currentIndex = i;
				}
			}
		}

		return currentIndex;
	}
}
