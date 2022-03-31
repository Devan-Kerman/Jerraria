package net.devtech.jerraria.resource;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public record OverlayDirectory(String name, List<Directory> directories) implements VirtualFile.Directory {
	public static OverlayDirectory overlay(String name, List<Directory> directories) {
		return new OverlayDirectory(name, directories);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<VirtualFile> resolveAll(String name) {
		if(name.charAt(0) == '/') {
			throw new IllegalArgumentException("resolution query cannot start with '/': \"" + name + "\"");
		}

		List<VirtualFile> files = List.of();
		for (Directory directory : directories) {
			VirtualFile resolved = directory.resolve(name);
			if(resolved != null) {
				if(files.isEmpty()) {
					files = new ArrayList<>();
				}
				files.add(resolved);
			}
		}
		return files;
	}

	@Override
	public @Nullable VirtualFile resolve(String name) {
		if(name.charAt(0) == '/') {
			throw new IllegalArgumentException("resolution query cannot start with '/': \"" + name + "\"");
		}

		List<Directory> innerDirectories = new ArrayList<>();

		for (Directory directory : directories) {
			VirtualFile resolved = directory.resolve(name);

			if (resolved instanceof Directory inner) {
				innerDirectories.add(inner);
			} else if (resolved != null) {
				return resolved;
			}
		}

		if (!innerDirectories.isEmpty()) {
			return new OverlayDirectory(name, innerDirectories);
		} else {
			return null;
		}
	}

	@Override
	public Collection<VirtualFile> children() {
		Map<String, OverlayDirectory> overlays = new HashMap<>();
		List<VirtualFile> files = new ArrayList<>();

		for (Directory directory : this.directories) {
			for (VirtualFile child : directory.children()) {
				String name = child.name();

				if (child instanceof VirtualFile.Directory d) {
					overlays.computeIfAbsent(name, n -> new OverlayDirectory(n, new ArrayList<>())).directories().add(d);
				} else {
					// todo: how do we handle duplicate names?
					files.add(child);
				}
			}
		}

		files.addAll(overlays.values());
		return files;
	}
}
