package net.devtech.jerraria.attachment.impl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentProvider;
import net.devtech.jerraria.attachment.AttachmentSetting;

public class ArrayAttachmentProvider<E, B extends AttachmentSetting>
	extends AbstractAttachmentProvider<E, B> {
	final Function<E, Object[]> arrayGetter;
	final BiConsumer<E, Object[]> arraySetter;
	final boolean concurrent;

	public ArrayAttachmentProvider(
		Function<E, Object[]> arrayGetter, BiConsumer<E, Object[]> arraySetter, boolean concurrent) {
		this.arrayGetter = arrayGetter;
		this.arraySetter = arraySetter;
		this.concurrent = concurrent;
	}

	@Override
	public <T> Set<B> getBehavior(Attachment<E, T> attachment) {
		return this.attachments.get(((AttachmentImpl)attachment).index).behavior();
	}

	@Override
	protected <T> Attachment<E, T> createAttachment(Set<B> list) {
		if(list.contains(AttachmentSetting.Concurrency.VOLATILE)) {
			if(this.concurrent) {
				return new ConcurrentAttachmentImpl<>(this.attachments.size());
			} else {
				throw new UnsupportedOperationException("AttachmentProvider must be Concurrent to use Volatile Attachments!");
			}
		} else {
			if(this.concurrent) {
				return new CASAttachmentImpl<>(this.attachments.size());
			} else {
				return new AttachmentImpl<>(this.attachments.size());
			}
		}
	}

	public class AttachmentImpl<T> implements Attachment<E, T> {
		final int index;

		public AttachmentImpl(int index) {
			this.index = index;
		}

		@Override
		public T getValue(E object) {
			return (T) ArrayAttachmentProvider.this.arrayGetter.apply(object)[this.index];
		}

		@Override
		public void setValue(E object, T value) {
			int index = this.index;
			Object[] arr = ArrayAttachmentProvider.this.arrayGetter.apply(object);
			if(index >= arr.length) {
				arr = Arrays.copyOf(arr, index + 1);
				ArrayAttachmentProvider.this.arraySetter.accept(object, arr);
			}
			arr[index] = value;
		}

		@Override
		public AttachmentProvider<E, ?> getProvider() {
			return ArrayAttachmentProvider.this;
		}
	}

	public class CASAttachmentImpl<T> extends AttachmentImpl<T> {
		public CASAttachmentImpl(int index) {
			super(index);
		}

		@Override
		public T getValue(E object) {
			return (T) ArrayAttachmentProvider.this.arrayGetter.apply(object)[this.index];
		}

		@Override
		public void setValue(E object, T value) {
			int index = this.index;
			Object[] arr;
			do {
				arr = ArrayAttachmentProvider.this.arrayGetter.apply(object);
				if(index >= arr.length) {
					arr = Arrays.copyOf(arr, index + 1);
					ArrayAttachmentProvider.this.arraySetter.accept(object, arr);
				}
				arr[index] = value;
			} while(arr != ArrayAttachmentProvider.this.arrayGetter.apply(object));
		}
	}

	private static final VarHandle ARRAY_ELEMENT_VAR_HANDLE = MethodHandles.arrayElementVarHandle(Object[].class);

	public class ConcurrentAttachmentImpl<T> extends AttachmentImpl<T> {
		public ConcurrentAttachmentImpl(int index) {
			super(index);
		}

		@Override
		public T getValue(E object) {
			return (T) ARRAY_ELEMENT_VAR_HANDLE.getVolatile(ArrayAttachmentProvider.this.arrayGetter.apply(object));
		}

		@Override
		public void setValue(E object, T value) {
			int index = this.index;
			Object[] arr;
			do {
				arr = ArrayAttachmentProvider.this.arrayGetter.apply(object);
				if(index >= arr.length) {
					arr = Arrays.copyOf(arr, index + 1);
					ArrayAttachmentProvider.this.arraySetter.accept(object, arr);
				}

				ARRAY_ELEMENT_VAR_HANDLE.setVolatile(arr, index, value);
			} while(arr != ArrayAttachmentProvider.this.arrayGetter.apply(object));
		}
	}
}
