package net.devtech.jerraria.gui.api.input;

import java.util.List;
import java.util.function.LongSupplier;

import net.devtech.jerraria.gui.impl.flags.GuiInternal;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

public interface CursorType {
	enum Standard implements CursorType {
		ARROW(GLFW.GLFW_ARROW_CURSOR),
		/**
		 * the cursor for text boxes basically
		 */
		IBEAM(GLFW.GLFW_IBEAM_CURSOR),
		CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
		HAND(GLFW.GLFW_HAND_CURSOR),
		/**
		 * horizontal resize (like in ms paint)
		 */
		HRESIZE(GLFW.GLFW_HRESIZE_CURSOR),
		/**
		 * vertical resize (like in ms paint)
		 */
		VRESIZE(GLFW.GLFW_VRESIZE_CURSOR);
		public static final List<Standard> VALUES = List.of(values());

		final int typeId;
		final LongSupplier handle;
		private long memoized;

		Standard(int typeId) {
			this.typeId = typeId;
			this.handle = () -> GLFW.glfwCreateStandardCursor(typeId);
		}

		@Override
		public void bind(long window) {
			GLFW.glfwSetCursor(window, this.getHandle());
		}

		private long getHandle() {
			long handle = this.memoized;
			if(handle == 0) {
				handle = this.handle.getAsLong();
			}
			return handle;
		}

		@Deprecated
		@ApiStatus.Internal
		public void setHandle(long handle) {
			this.memoized = handle;
			GuiInternal.HANDLE_TO_TYPE.put(handle, this);
		}
	}

	void bind(long window);
}
