package net.devtech.jerraria.resource;

import net.devtech.jerraria.util.SearchOrder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Stream;

public interface VirtualFile {

	String name();

	@Nullable
	default String fileExtension() {
		String name = name();
		int index = name.indexOf('.');
		return index < 0 ? null : name.substring(0, index);
	}

	default Stream<VirtualFile> walk(SearchOrder order) {
		throw new UnsupportedOperationException();
	}

	default Directory asDirectory() {
		return (Directory) this;
	}

	default Regular asRegular() {
		return (Regular) this;
	}

	interface Directory extends VirtualFile {
		@Nullable
		VirtualFile resolve(String name);

		Collection<VirtualFile> children();
	}

	interface Regular extends VirtualFile {
		InputStream read() throws IOException;
	}
}
