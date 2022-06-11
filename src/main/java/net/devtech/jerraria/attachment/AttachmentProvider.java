package net.devtech.jerraria.attachment;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.devtech.jerraria.attachment.impl.ArrayAttachmentProvider;

public interface AttachmentProvider<O, B extends AttachmentSetting> {
	static <E, B extends AttachmentSetting> AttachmentProvider<E, B> simple(Function<E, Object[]> arrayGetter, BiConsumer<E, Object[]> arraySetter) {
		return new ArrayAttachmentProvider<>(arrayGetter, arraySetter, false);
	}

	/**
	 * Creates an attachment provider that can be accessed from multiple threads. This however does not allow for CAS
	 * operations on attached data. For that your AttachmentSetting generic must implement {@link
	 * AttachmentSetting.HasConcurrent} and your attachment must have {@link AttachmentSetting.Concurrency#VOLATILE}.
	 * <br>
	 * <b>The array field must be marked `volatile`!</b>
	 */
	static <E, B extends AttachmentSetting> AttachmentProvider<E, B> concurrent(Function<E, Object[]> arrayGetter, BiConsumer<E, Object[]> arraySetter) {
		return new ArrayAttachmentProvider<>(arrayGetter, arraySetter, true);
	}

	<T> Attachment<O, T> registerAttachment(B... behavior);

	List<AttachmentPair<O, B>> getAttachments();

	<T> Set<B> getBehavior(Attachment<O, T> attachment);

	void registerListener(AttachmentRegistrationListener<O, B> listener);

	default void registerAndRunListener(AttachmentRegistrationListener<O, B> listener) {
		this.registerListener(listener);
		for(AttachmentPair<O, B> attachment : this.getAttachments()) {
			listener.accept(attachment.attachment, attachment.behavior);
		}
	}

	interface AttachmentRegistrationListener<E, B extends AttachmentSetting> {
		/**
		 * @param attachment the attachment being registered
		 * @param behavior the passed set does preserve the order of the original array
		 */
		void accept(Attachment<E, ?> attachment, Set<B> behavior);
	}

	/**
	 * @param behavior the passed set does preserve the order of the original array
	 */
	record AttachmentPair<E, B extends AttachmentSetting>(Attachment<E, ?> attachment, Set<B> behavior) {}
}
