package net.devtech.jerraria.attachment;

public interface AttachmentSetting {
	interface HasConcurrent extends AttachmentSetting {}

	enum Concurrency implements HasConcurrent {
		VOLATILE
	}
}
