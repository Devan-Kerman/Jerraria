package resource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.devtech.jerraria.resource.OverlayDirectory;
import net.devtech.jerraria.resource.PathVirtualFile;
import net.devtech.jerraria.resource.VirtualFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class OverlayTest {

	@Test
	public void overlayRead() throws IOException {
		try (FileSystem a = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build());
			 FileSystem b = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build())) {
			Files.writeString(a.getPath("a.txt"), "Hello world!");
			Files.writeString(b.getPath("b.txt"), "Bonjour monde!"); // in the name of science we shall bear such impurities

			VirtualFile.Directory directory = new OverlayDirectory("/", List.of(PathVirtualFile.of(a.getPath("/")).asDirectory(), PathVirtualFile.of(b.getPath("/")).asDirectory()));

			{
				VirtualFile fileA = directory.resolve("a.txt");
				Assertions.assertNotNull(fileA, "File A could not be found in overlay filesystem");
				Assertions.assertArrayEquals(fileA.asRegular().read().readAllBytes(), "Hello world!".getBytes());
			}

			{
				VirtualFile fileB = directory.resolve("b.txt");
				Assertions.assertNotNull(fileB, "File B could not be found in overlay filesystem");
				Assertions.assertArrayEquals(fileB.asRegular().read().readAllBytes(), "Bonjour monde!".getBytes());
			}
		}
	}

	@Test
	public void overlayList() throws IOException {
		try (FileSystem a = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build());
			 FileSystem b = Jimfs.newFileSystem(Configuration.unix().toBuilder().setWorkingDirectory("/").build())) {
			Files.createFile(a.getPath("a.txt"));
			Files.createFile(b.getPath("b.txt"));

			VirtualFile.Directory directoryA = PathVirtualFile.of(a.getPath("/")).asDirectory();
			VirtualFile.Directory directoryB = PathVirtualFile.of(b.getPath("/")).asDirectory();
			VirtualFile.Directory directory = new OverlayDirectory("/", List.of(directoryA, directoryB));

			Assertions.assertEquals(Set.of(directoryA.resolve("a.txt"), directoryB.resolve("b.txt")), Set.copyOf(directory.children()));
		}
	}
}
