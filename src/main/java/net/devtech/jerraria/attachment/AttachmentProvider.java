package net.devtech.jerraria.attachment;

import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.devtech.jerraria.attachment.impl.ArrayAttachmentProvider;
import net.devtech.jerraria.attachment.impl.ConcurrentAttachmentProvider;

public interface AttachmentProvider<O, B extends AttachmentSetting> {
	static <E, B extends AttachmentSetting> AttachmentProvider<E, B> simple(
		Function<E, Object[]> arrayGetter,
		BiConsumer<E, Object[]> arraySetter) {
		return new ArrayAttachmentProvider<>(arrayGetter, arraySetter);
	}

	static <E extends AttachableObject, B extends AttachmentSetting> AttachmentProvider<E, B> simple() {
		return new ArrayAttachmentProvider<>(a -> a.attachedData, (e, a) -> e.attachedData = a);
	}

	/**
	 * Creates an attachment provider that can be accessed from multiple threads.
	 * <br>
	 * <b>The array field must be marked `volatile`!</b>
	 */
	static <E, B extends AttachmentSetting> Atomic<E, B> atomic(
		Function<E, Object[]> getVolatile,
		CompareAndSet<E> compareAndSet) {
		return new ConcurrentAttachmentProvider<>(getVolatile, compareAndSet);
	}

	static <E extends AttachableObject, B extends AttachmentSetting> Atomic<E, B> atomic() {
		return new ConcurrentAttachmentProvider<>(a -> (Object[]) AttachableObject.HANDLE.getVolatile(a), AttachableObject.HANDLE::weakCompareAndSet);
	}

	/**
	 * This method is similar to {@link #atomic(Function, CompareAndSet)} however it <b>may</b> be slower as the jvm might have a harder time inlining it
	 * @param handle the varhandle of an Object[] field in E
	 */
	static <E, B extends AttachmentSetting> Atomic<E, B> atomic(VarHandle handle) {
		return new ConcurrentAttachmentProvider<>(handle);
	}

	interface CompareAndSet<E> {
		boolean compareAndSet(E obj, Object[] expected, Object[] set);
	}

	interface Atomic<O, B extends AttachmentSetting> extends AttachmentProvider<O, B> {
		<T> Attachment.Atomic<O, T> registerAtomicAttachment(B... behavior);
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
