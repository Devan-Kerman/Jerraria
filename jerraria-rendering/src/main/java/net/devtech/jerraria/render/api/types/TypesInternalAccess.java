package net.devtech.jerraria.render.api.types;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.Internal
public class TypesInternalAccess {
	public static void setVertexId(End end, int id) {
		end.vertexId = id;
	}
}
