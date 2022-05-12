package net.devtech.jerraria.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Id implements Comparable<Id> {
	final long packedNamespace;
	int hash;

	Id(long namespace) {
		this.packedNamespace = namespace;
	}

	public static Id create(String namespace, String path) {
		Objects.requireNonNull(namespace, "namespace cannot be null!");
		Objects.requireNonNull(path, "path cannot be null!");
		long mod = IdentifierPacker.pack(namespace);
		IdentifierPacker.throwErr(path, mod);
		long packed = IdentifierPacker.pack(path);
		if(packed < 0) {
			return new Partial(mod, path);
		} else {
			return new Full(mod, packed);
		}
	}

	public static Id parse(String string) {
		int index = string.indexOf(':');
		if(index == -1) {
			throw new IllegalStateException(string + " is not a valid identifier! must come in form <namespace>:<path>");
		}
		return create(string.substring(0, index), string.substring(index+1));
	}

	public static Full createFull(String namespace, String path) {
		Objects.requireNonNull(namespace, "namespace cannot be null!");
		Objects.requireNonNull(path, "path cannot be null!");
		long mod = IdentifierPacker.pack(namespace);
		IdentifierPacker.throwErr(path, mod);
		long packed = IdentifierPacker.pack(path);
		IdentifierPacker.throwErr(path, packed);
		return new Full(mod, packed);
	}

	public static Full create(long namespace, long path) {
		IdentifierPacker.throwErr("<unknown>", namespace);
		IdentifierPacker.throwErr("<unknown>", path);
		return new Full(namespace, path);
	}

	public final long getNamespace() {
		return this.packedNamespace;
	}

	public final String unpackNamespace() {
		return IdentifierPacker.unpack(this.packedNamespace);
	}

	/**
	 * @return may return -1 if path cannot be packed
	 */
	public abstract long getPath();

	public abstract String getUnpackedPath();

	public long getPackedNamespace() {
		return packedNamespace;
	}

	public static final class Full extends Id {
		final long path;

		Full(long namespace, long path) {
			super(namespace);
			this.path = path;
		}

		@Override
		public long getPath() {
			return this.path;
		}

		@Override
		public String getUnpackedPath() {
			return IdentifierPacker.unpack(this.path);
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			return o instanceof Full full && this.path == full.path;
		}

		@Override
		public int hash0() {
			return (int) (this.path ^ (this.path >>> 32));
		}

		@Override
		public int compareTo(@NotNull Id o) {
			// todo proper compare to, this is not alphabetical
			if (o instanceof Full f) {
				long ln = o.packedNamespace - this.getPackedNamespace();
				if (ln < 0) {
					return -1;
				} else if (ln > 0) {
					return 1;
				}
				long l = f.path - this.path;
				if (l == 0) {
					return 0;
				} else if (l > 0) {
					return 1;
				}
			}
			return -1;
		}
	}

	public static final class Partial extends Id {
		final String packed;

		Partial(long namespace, String packed) {
			super(namespace);
			this.packed = packed;
		}

		@Override
		public long getPath() {
			return -1;
		}

		@Override
		public String getUnpackedPath() {
			return this.packed;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			return o instanceof Partial partial && this.packed.equals(partial.packed);
		}

		@Override
		public int hash0() {
			return this.packed.hashCode();
		}

		@Override
		public int compareTo(@NotNull Id o) {
			if(o instanceof Partial p) {
				long ln = p.getPackedNamespace() - this.getPackedNamespace();
				if(ln < 0) {
					return -1;
				} else if(ln == 0) {
					return p.packed.compareTo(this.packed);
				}
			}
			return 1;
		}
	}

	abstract int hash0();

	@Override
	public int hashCode() {
		int hash = this.hash;
		if(hash == 0) {
			hash = 32 * hash0() + (int) (this.packedNamespace ^ (this.packedNamespace >>> 32));
			if(hash == 0) {
				hash = 1;
			}

			this.hash = hash;
		}
		return hash;
	}

	@Override
	public String toString() {
		return this.unpackNamespace() + ":" + this.getUnpackedPath();
	}
}
