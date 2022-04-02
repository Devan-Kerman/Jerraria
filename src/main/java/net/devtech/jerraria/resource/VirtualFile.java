package net.devtech.jerraria.resource;

import net.devtech.jerraria.util.SearchOrder;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface VirtualFile {
	/**
	 * @return '/' for root directories, all other paths will not contain a leading '/'
	 */
	String name();

	default boolean hasFileExtension(String extension) {
		String name = this.name();
		int lenA = name.length(), lenB = extension.length();
		return lenA > lenB && name.charAt(lenA - lenB - 1) == '.' && name.endsWith(extension);
	}

	@NotNull
	default String fileExtension() {
		String name = this.name();
		int index = name.lastIndexOf('.');
		return index < 0 ? "" : name.substring(index+1);
	}

	default Stream<VirtualFile> walk(SearchOrder order) {
		throw new UnsupportedOperationException();
	}

	default Directory asDirectory() {
		if(this instanceof Directory dir) {
			return dir;
		} else {
			throw new IllegalStateException(this.name() + " is not a directory!");
		}
	}

	default Regular asRegular() {
		if(this instanceof Regular file) {
			return file;
		} else {
			throw new IllegalStateException(this.name() + " is not a file!");
		}
	}

	interface Directory extends VirtualFile {
		@Nullable
		VirtualFile resolve(String name);

		@NotNull
		default VirtualFile resolveOrThrow(String name) {
			VirtualFile file = this.resolve(name);
			if(file == null) {
				throw Validate.rethrow(new FileNotFoundException(name + " in " + this.name()));
			}
			return file;
		}

		default VirtualFile.Directory resolveDirectory(String name) {
			return this.resolveOrThrow(name).asDirectory();
		}

		default VirtualFile.Regular resolveFile(String name) {
			return this.resolveOrThrow(name).asRegular();
		}

		default List<VirtualFile> resolveAll(String name) {
			VirtualFile file = this.resolve(name);
			return file == null ? List.of() : List.of(file);
		}

		Collection<? extends VirtualFile> children();
	}

	interface Regular extends VirtualFile {
		InputStream read() throws IOException;

		default String withoutExtension() {
			String name = this.name();
			int index = name.lastIndexOf('.');
			return index != -1 ? name.substring(0, index) : name;
		}
	}
}
