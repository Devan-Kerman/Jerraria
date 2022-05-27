package net.devtech.jerraria.render.api.translucency;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Deprecated
@ApiStatus.Internal
public class TranslucentInternal {
	public static void setSecondPass(TranslucentShader<?> start, TranslucentShader<?> secondPass) {
		start.secondPass = secondPass;
	}

	@Nullable
	public static TranslucentShader<?> getSecondPass(TranslucentShader<?> shader) {
		return shader.secondPass;
	}
}
