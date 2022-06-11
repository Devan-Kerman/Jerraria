package net.devtech.jerraria.attachment.impl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentProvider;
import net.devtech.jerraria.attachment.AttachmentSetting;

public class ConcurrentAttachmentProvider<E, B extends AttachmentSetting> extends AbstractAttachmentProvider<E, B>
	implements AttachmentProvider.Atomic<E, B> {
	private static final VarHandle ARRAY_ELEMENT_VAR_HANDLE = MethodHandles.arrayElementVarHandle(Object[].class);
	final Function<E, Object[]> getVolatile;
	final CompareAndSet<E> cas;

	public ConcurrentAttachmentProvider(VarHandle handle) {
		this(e -> (Object[]) handle.getVolatile(e), handle::compareAndSet);
	}

	public ConcurrentAttachmentProvider(
		Function<E, Object[]> get, CompareAndSet<E> set) {
		this.getVolatile = get;
		this.cas = set;
	}

	@Override
	public <T> Set<B> getBehavior(Attachment<E, T> attachment) {
		return this.attachments.get(((CASAttachmentImpl) attachment).index).behavior();
	}

	@Override
	protected <T> Attachment<E, T> createAttachment(Set<B> behaviors, boolean isAtomic) {
		if(isAtomic) {
			return new AtomicAttachmentImpl<>(this.attachments.size());
		} else {
			return new CASAttachmentImpl<>(this.attachments.size());
		}
	}

	public class CASAttachmentImpl<T> implements Attachment<E, T> {
		final int index;

		public CASAttachmentImpl(int index) {
			this.index = index;
		}

		@Override
		public T getValue(E object) {
			Object[] apply = getVolatile.apply(object);
			if(apply == null) {
				return null;
			} else {
				//noinspection unchecked
				return (T) apply[this.index];
			}
		}

		@Override
		public void setValue(E object, T value) {
			int index = this.index;
			Object[] arr, new_;
			do {
				new_ = arr = getVolatile.apply(object);
				if(arr == null) {
					new_ = new Object[index + 1];
				} else if(index >= arr.length) {
					new_ = Arrays.copyOf(arr, index + 1);
				}
				new_[index] = value;
			} while(!cas.compareAndSet(object, arr, new_));
		}

		@Override
		public AttachmentProvider<E, ?> getProvider() {
			return ConcurrentAttachmentProvider.this;
		}
	}

	public class AtomicAttachmentImpl<T> extends CASAttachmentImpl<T> implements Attachment.Atomic<E, T> {
		public AtomicAttachmentImpl(int index) {
			super(index);
		}

		@Override
		public void setValue(E object, T value) {
			int index = this.index;
			Object[] arr, new_;
			do {
				new_ = arr = getVolatile.apply(object);
				if(arr == null) {
					new_ = new Object[index + 1];
				} else if(index >= arr.length) {
					new_ = Arrays.copyOf(arr, index + 1);
				}

				ARRAY_ELEMENT_VAR_HANDLE.setVolatile(new_, index, value);
			} while(!cas.compareAndSet(object, arr, new_));
		}

		@Override
		public boolean weakCompareAndSet(E object, T expected, T value) {
			int index = this.index;
			Object[] arr, new_;
			new_ = arr = getVolatile.apply(object);
			if(arr == null) {
				new_ = new Object[index + 1];
			} else if(index >= arr.length) {
				new_ = Arrays.copyOf(arr, index + 1);
			}

			if(ARRAY_ELEMENT_VAR_HANDLE.compareAndSet(new_, index, expected, value)) {
				return cas.compareAndSet(object, arr, new_);
			} else {
				return false;
			}
		}
	}
}
