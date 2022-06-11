package net.devtech.jerraria.attachment.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentProvider;
import net.devtech.jerraria.attachment.AttachmentSettings;

public abstract class AbstractAttachmentProvider<E, B extends AttachmentSettings> implements AttachmentProvider<E, B> {
	protected final List<AttachmentPair<E, B>> attachments = new ArrayList<>();
	protected final List<AttachmentRegistrationListener<E, B>> listeners = new ArrayList<>();

	@Override
	public <T> Attachment<E, T> registerAttachment(B... behaviors) {
		Set<B> behaviorList = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(behaviors)));
		Attachment<E, T> impl = this.createAttachment(behaviorList);
		this.attachments.add(new AttachmentPair<>(impl, behaviorList));
		for(AttachmentRegistrationListener<E, B> listener : listeners) {
			listener.accept(impl, behaviorList);
		}
		return impl;
	}

	protected abstract <T> Attachment<E, T> createAttachment(Set<B> behaviors);

	@Override
	public List<AttachmentPair<E, B>> getAttachments() {
		return this.attachments;
	}

	@Override
	public void registerListener(AttachmentRegistrationListener<E, B> listener) {
		this.listeners.add(listener);
	}
}
