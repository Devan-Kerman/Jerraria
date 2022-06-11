package net.devtech.jerraria.attachment;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.devtech.jerraria.attachment.impl.ArrayAttachmentProvider;

public interface AttachmentProvider<O, B extends AttachmentSettings> {
	static <E, B extends AttachmentSettings> AttachmentProvider<E, B> simple(
		Function<E, Object[]> arrayGetter,
		BiConsumer<E, Object[]> arraySetter) {
		return new ArrayAttachmentProvider<>(arrayGetter, arraySetter, false);
	}

	/**
	 * The array field must be marked `volatile`
	 */
	static <E, B extends AttachmentSettings> AttachmentProvider<E, B> concurrent(
		Function<E, Object[]> arrayGetter,
		BiConsumer<E, Object[]> arraySetter) {
		return new ArrayAttachmentProvider<>(arrayGetter, arraySetter, true);
	}

	<T> Attachment<O, T> registerAttachment(B... behaviors);

	List<AttachmentPair<O, B>> getAttachments();

	<T> Set<B> getBehavior(Attachment<O, T> attachment);

	void registerListener(AttachmentRegistrationListener<O, B> listener);

	default void registerAndRunListener(AttachmentRegistrationListener<O, B> listener) {
		this.registerListener(listener);
		for(AttachmentPair<O, B> attachment : this.getAttachments()) {
			listener.accept(attachment.attachment, attachment.behavior);
		}
	}

	/**
	 * @param behavior the passed set does preserve the order of the original array
	 */
	record AttachmentPair<E, B extends AttachmentSettings>(Attachment<E, ?> attachment, Set<B> behavior) {}

	interface AttachmentRegistrationListener<E, B extends AttachmentSettings> {
		/**
		 * @param attachment the attachment being registered
		 * @param behavior the passed set does preserve the order of the original array
		 */
		void accept(Attachment<E, ?> attachment, Set<B> behavior);
	}
}
