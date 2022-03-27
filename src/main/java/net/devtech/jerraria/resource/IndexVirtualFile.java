package net.devtech.jerraria.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class IndexVirtualFile implements VirtualFile {
	final String currentPath;

	IndexVirtualFile(String path) {
		this.currentPath = path;
	}

	@Override
	public String name() {
		return this.currentPath;
	}

	public static VirtualFile.Directory from(Class<?> source) {
		InputStream stream = source.getResourceAsStream("/index.txt");
		if(stream == null) {
			throw new IllegalArgumentException("No index.txt found for " + source);
		}
		Directory root = new Directory("/");
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while((line = reader.readLine()) != null && !line.equals("%%end of dirs%%")) {
				Directory dir = root;
				int last = 0, current;
				while((current = line.indexOf('/', last)) != -1) {
					IndexVirtualFile create = dir.getOrCreateDir(line.substring(last, current));
					dir = (Directory) create;
					last = current;
				}
				dir.getOrCreateDir(line.substring(last));
			}

			while((line = reader.readLine()) != null) {
				int index = line.lastIndexOf('/');
				String name;
				Directory dir;
				if(index == -1) {
					name = line;
					dir = root;
				} else {
					name = line.substring(index+1);
					dir = (Directory) root.resolve(line.substring(0, index));
				}

				if(dir == null) {
					throw new IllegalArgumentException("Incomplete indexing, missing directory for \"" + line + "\"");
				}

				dir.index.put(name, new IndexVirtualFile.File(name, source));
			}
		} catch(IOException e) {
			throw Validate.rethrow(e);
		}
		return root;
	}

	static class Directory extends IndexVirtualFile implements VirtualFile.Directory {
		final Map<String, IndexVirtualFile> index = new HashMap<>();

		Directory(String path) {
			super(path);
		}

		IndexVirtualFile getOrCreateDir(String name) {
			return this.index.computeIfAbsent(name, IndexVirtualFile.Directory::new);
		}

		@Override
		public @Nullable VirtualFile resolve(String name) {
			int first = name.indexOf('/');
			if(first == -1) {
				return index.get(name);
			} else if(this.index.get(name.substring(0, first)) instanceof VirtualFile.Directory v) {
				return v.resolve(name.substring(first + 1));
			} else {
				return null;
			}
		}

		@Override
		public Collection<? extends VirtualFile> children() {
			return index.values();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			String name = this.name();
			builder.append(name);
			if(!name.equals("/")) {
				builder.append('/');
			}
			builder.append('\n');
			this.index.forEach((s, file) -> {
				if(file instanceof IndexVirtualFile.Directory d) {
					builder.append(d.toString().indent(2)).append('\n');
				} else if(file instanceof IndexVirtualFile.File f) {
					builder.append("  ").append(s).append(" @ ").append(f.type).append('\n');
				}
			});
			return builder.toString();
		}
	}

	static class File extends IndexVirtualFile implements VirtualFile.Regular {
		final Class<?> type;
		File(String path, Class<?> type) {
			super(path);
			this.type = type;
		}

		@Override
		public InputStream read() throws IOException {
			return this.type.getResourceAsStream("/" + this.name());
		}

		@Override
		public String toString() {
			return this.name() + " @ " + this.type.getSimpleName();
		}
	}

	@Override
	public String toString() {
		return "gras://" + this.name();
	}
}
