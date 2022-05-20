package net.devtech.jerraria.client;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.JCommander;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.jerraria.entity.PlayerEntity;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.shaders.InstancedSolidColorShader;
import net.devtech.jerraria.resource.IndexVirtualFile;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.util.Validate;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.client.ClientChunk;
import net.devtech.jerraria.world.internal.client.ClientWorld;
import net.devtech.jerraria.world.internal.client.ClientWorldServer;
import org.lwjgl.glfw.GLFW;

public class ClientMain {

	public static void main(String[] argv) {
		Bootstrap.startClient(argv, () -> {




			return null;
		});
	}

	public static Matrix3f cartesianToAWTIndexGrid(float scale) {
		int[] dims = ClientInit.dims;
		Matrix3f cartToIndexMat = new Matrix3f();
		cartToIndexMat.offset(-1, 1);
		cartToIndexMat.scale(2, -2);
		cartToIndexMat.scale(dims[1] / (dims[0] * scale), 1 / scale);
		return cartToIndexMat;
	}
}
