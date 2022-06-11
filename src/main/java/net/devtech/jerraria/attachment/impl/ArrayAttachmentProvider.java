package net.devtech.jerraria.attachment.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentProvider;
import net.devtech.jerraria.attachment.AttachmentSettings;

public class ArrayAttachmentProvider<E, B extends AttachmentSettings>
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
		if(this.concurrent) {
			return new ConcurrentAttachmentImpl<>(this.attachments.size());
		} else {
			return new AttachmentImpl<>(this.attachments.size());
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

	public class ConcurrentAttachmentImpl<T> extends AttachmentImpl<T> {
		public ConcurrentAttachmentImpl(int index) {
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
}
