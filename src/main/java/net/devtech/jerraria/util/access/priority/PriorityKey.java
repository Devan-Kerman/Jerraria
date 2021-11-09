package net.devtech.jerraria.util.access.priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Lists;
import net.devtech.jerraria.util.access.RegisterOnlyAccess;

public final class PriorityKey {
	static final Map<String, PriorityKey> KEY_IDS = new HashMap<>();
	static final Set<String> BUILT = new HashSet<>();
	private static final PriorityKey[] EMPTY = new PriorityKey[0];
	final String name;
	PriorityKey[] before = EMPTY, after = EMPTY;

	/**
	 * The default apis in the access
	 */
	public static final PriorityKey DEFAULT = new Builder().build("default");
	/**
	 * What the {@link RegisterOnlyAccess#andThen(Object)} uses by default
	 */
	public static final PriorityKey STANDARD = new Builder().after(DEFAULT).build("standard");

	public static Builder builder() {
		return new Builder();
	}

	PriorityKey(String name) {
		this.name = name;
	}

	public static List<PriorityKey> sort(Iterable<PriorityKey> keys) {
		return sort(keys, key -> {
			ArrayList<PriorityKey> list = new ArrayList<>(Arrays.asList(key.after));
			for(PriorityKey k : keys) {
				if(Arrays.asList(k.before).contains(key)) {
					list.add(k);
				}
			}
			return list;
		});
	}

	public static <T> List<T> sort(Iterable<T> source, Function<T, List<T>> dependencies) {
		List<T> sorted = new ArrayList<>();
		Set<T> visited = new HashSet<>();

		for(T t : source) {
			visit(t, visited, sorted, dependencies);
		}
		return sorted;
	}

	private static <T> void visit(T item, Set<T> visited, List<T> sorted, Function<T, List<T>> dependencies) {
		if(visited.add(item)) {
			for(T t : dependencies.apply(item)) {
				visit(t, visited, sorted, dependencies);
			}

			sorted.add(item);
		} else if(!sorted.contains(item)) {
			throw new UnsupportedOperationException("Cyclic dependency found");
		}
	}

	static String append(PriorityKey key, boolean isAfter) {
		return (isAfter ? "runsAfter " : "runsBefore ") + key.name;
	}

	private boolean verify(List<String> info) {
		Set<PriorityKey> keys = new HashSet<>();
		for(PriorityKey key : this.after) {
			if(key.verify(keys, info, this, true)) {
				info.add(append(key, true));
				info.add(0, append(this, true));
				return true;
			}
		}
		keys.clear();
		for(PriorityKey key : this.before) {
			if(key.verify(keys, info, this, false)) {
				info.add(append(key, false));
				info.add(0, append(this, false));
				return true;
			}
		}
		return false;
	}

	private boolean verify(Set<PriorityKey> visited, List<String> info, PriorityKey key, boolean isAfter) {
		for(PriorityKey k : this.before) {
			if(k == key && !isAfter) {
				return true;
			} else if(visited.add(k) && k.verify(visited, info, key, isAfter)) {
				info.add(append(k, false));
				return true;
			}
		}

		for(PriorityKey k : this.after) {
			if(k == key && isAfter) {
				return true;
			} else if(visited.add(k) && k.verify(visited, info, key, isAfter)) {
				info.add(append(k, true));
				return true;
			}
		}

		return false;
	}

	public static final class Builder {
		final List<PriorityKey> before = new ArrayList<>(), after = new ArrayList<>();

		public Builder before(PriorityKey key) {
			this.before.add(key);
			return this;
		}

		public Builder after(PriorityKey key) {
			this.after.add(key);
			return this;
		}

		public Builder before(String id) {
			return this.before(KEY_IDS.computeIfAbsent(id, PriorityKey::new));
		}

		public Builder after(String id) {
			return this.after(KEY_IDS.computeIfAbsent(id, PriorityKey::new));
		}

		public PriorityKey build(String id) {
			if(BUILT.add(id)) {
				PriorityKey key = KEY_IDS.computeIfAbsent(id, PriorityKey::new);
				key.before = this.before.toArray(PriorityKey[]::new);
				key.after = this.after.toArray(PriorityKey[]::new);
				List<String> info = new ArrayList<>();
				if(key.verify(info)) {
					info.add(key.name);
					throw new UnsupportedOperationException("Circular priority order " + String.join(" ", Lists.reverse(info)));
				}
				return key;
			} else {
				throw new IllegalStateException("priority already exists with name " + id);
			}
		}
	}

	void toString(StringBuilder builder, String indent) {
		builder.append(this.name).append('\n');
		for(PriorityKey key : this.before) {
			builder.append(indent).append("runsBefore ");
			key.toString(builder, indent + "  ");
		}
		for(PriorityKey key : this.after) {
			builder.append(indent).append("runsAfter ");
			key.toString(builder, indent + "  ");
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("key: ");
		this.toString(builder, "");
		return builder.toString();
	}
}
