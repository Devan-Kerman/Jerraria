package net.devtech.jerraria.resource;

import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public class PathVirtualFile implements VirtualFile {
	protected final Path path;

	private PathVirtualFile(Path path) {
		this.path = path;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PathVirtualFile that = (PathVirtualFile) o;
		return Objects.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		return "PathVirtualFile{" +
			   "path=" + path +
			   '}';
	}

	public static VirtualFile of(Path path) {
		if (Files.isRegularFile(path)) {
			return new PathRegular(path);
		} else {
			return new PathDirectory(path);
		}
	}

	@Override
	public String name() {
		return path.getName(path.getNameCount() - 1).toString();
	}

	private static class PathDirectory extends PathVirtualFile implements VirtualFile.Directory {

		PathDirectory(Path path) {
			super(path);
		}

		@Override
		public @Nullable VirtualFile resolve(String name) {
			return PathVirtualFile.of(path.resolve(name));
		}

		@Override
		public Collection<VirtualFile> children() {
			try {
				return Files.list(path).map(PathVirtualFile::of).toList();
			} catch (IOException exception) {
				throw Validate.rethrow(exception);
			}
		}
	}

	private static class PathRegular extends PathVirtualFile implements VirtualFile.Regular {

		PathRegular(Path path) {
			super(path);
		}

		@Override
		public InputStream read() throws IOException {
			return Files.newInputStream(path);
		}
	}
}
