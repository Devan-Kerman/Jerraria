package net.devtech.jerraria.gui.impl.flags;

import java.util.Arrays;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.devtech.jerraria.gui.api.input.CursorType;
import net.devtech.jerraria.gui.api.input.Key;
import net.devtech.jerraria.gui.api.input.Modifier;
import net.devtech.jerraria.gui.api.input.MouseButton;
import net.devtech.jerraria.util.math.JMath;

public class GuiInternal {
	static final Modifier[] MODIFIERS = new Modifier[32];
	static MouseButton[] mouseButtons = MouseButton.Standard.values();
	static final MouseButton UNKNOWN = () -> -1;
	static final Int2ObjectMap<Key> GLFW_TO_KEY = new Int2ObjectOpenHashMap<>();
	public static final Long2ObjectMap<CursorType> HANDLE_TO_TYPE = new Long2ObjectOpenHashMap<>();

	public static int extractFlags(Set<Modifier> modifiers) {
		if(modifiers instanceof IntFlags<Modifier> i) {
			return i.flags;
		} else {
			return modifiers.stream().mapToInt(Modifier::flag).reduce(0, (a, b) -> a | b);
		}
	}

	static {
		GLFW_TO_KEY.put(-1, () -> -1);
		for(Key.Standard value : Key.Standard.values()) {
			GLFW_TO_KEY.put(value.glfwId(), value);
		}

		for(Modifier.Standard standard : Modifier.Standard.values()) {
			MODIFIERS[JMath.log2(standard.glfwFlag())] = standard;
		}
		for(int i = 0; i < MODIFIERS.length; i++) {
			Modifier modifier = MODIFIERS[i];
			if(modifier == null) {
				int flag = 1 << i;
				MODIFIERS[i] = () -> flag;
			}
		}
	}

	public static Set<Modifier> modifiersByGlfwFlags(int modifiers) {
		return new IntFlags<>(MODIFIERS, modifiers, false);
	}

	public static MouseButton mouseButtonByGlfwId(int glfwId) {
		MouseButton type;
		if(glfwId < 0) {
			return UNKNOWN;
		} else if(glfwId < mouseButtons.length && (type = mouseButtons[glfwId]) != null) {
			return type;
		} else {
			mouseButtons = Arrays.copyOf(mouseButtons, glfwId + 1);
			return mouseButtons[glfwId] = () -> glfwId;
		}
	}

	public static Key keyByGlfwId(int glfwId) {
		return GLFW_TO_KEY.computeIfAbsent(glfwId, i -> () -> i);
	}
}
