package net.devtech.jerraria.attachment.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentProvider;
import net.devtech.jerraria.attachment.AttachmentSetting;

public abstract class AbstractAttachmentProvider<E, B extends AttachmentSetting> implements AttachmentProvider<E, B> {
	protected final List<AttachmentPair<E, B>> attachments = new ArrayList<>();
	protected final List<AttachmentRegistrationListener<E, B>> listeners = new ArrayList<>();

	@Override
	public <T> Attachment<E, T> registerAttachment(B... behavior) {
		return this.registerAttachment_(behavior, false);
	}

	public <T> Attachment.Atomic<E, T> registerAtomicAttachment(B... behavior) {
		return (Attachment.Atomic<E, T>) this.registerAttachment_(behavior, true);
	}

	protected <T> Attachment<E, T> registerAttachment_(B[] behavior, boolean atomic) {
		Set<B> behaviorList = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(behavior)));
		Attachment<E, T> impl = this.createAttachment(behaviorList, atomic);
		this.attachments.add(new AttachmentPair<>(impl, behaviorList));
		for(AttachmentRegistrationListener<E, B> listener : listeners) {
			listener.accept(impl, behaviorList);
		}
		return impl;
	}

	protected abstract <T> Attachment<E, T> createAttachment(Set<B> behaviors, boolean atomic);

	@Override
	public List<AttachmentPair<E, B>> getAttachments() {
		return this.attachments;
	}

	@Override
	public void registerListener(AttachmentRegistrationListener<E, B> listener) {
		this.listeners.add(listener);
	}
}
