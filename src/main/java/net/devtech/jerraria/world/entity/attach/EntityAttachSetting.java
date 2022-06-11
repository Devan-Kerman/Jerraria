package net.devtech.jerraria.world.entity.attach;

import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.attachment.AttachmentSetting;
import net.devtech.jerraria.jerracode.JCType;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EntityAttachSetting extends AttachmentSetting.HasConcurrent {

	static <T, N> Serializer<Entity, T, N> serializer(Id uniqueName, JCType<T, N> type) {
		return serializer(uniqueName, type, false);
	}

	/**
	 * @param uniqueName a unique id for this attachment
	 * @param type the type serializer
	 * @param <T> the data type being serialized
	 * @param <N> the serialized form of the type
	 * @return a serializer for the attachment
	 */
	static <T, N> Serializer<Entity, T, N> serializer(Id uniqueName, JCType<T, N> type, boolean serializeNulls) {
		return new Serializer<>() {
			@Override
			public Id getKey() {
				return uniqueName;
			}

			@Override
			public JCType<?, N> getType() {
				return type;
			}

			@Override
			public JCElement<N> serialize(Attachment<Entity, T> attachment, Entity entity) {
				T value = attachment.getValue(entity);
				if(value != null || serializeNulls) {
					return JCElement.create(type, value);
				} else {
					return null;
				}
			}

			@Override
			public void deserialize(Attachment<Entity, T> attachment, Entity entity, JCElement<N> data) {
				attachment.setValue(entity, type.convertFromNative(data.value()));
			}
		};
	}

	interface Serializer<E, T, N> extends EntityAttachSetting {
		/**
		 * A unique id for this attachment
		 */
		Id getKey();

		JCType<?, N> getType();

		@Nullable
		JCElement<N> serialize(Attachment<E, T> attachment, E entity);

		void deserialize(Attachment<E, T> attachment, E entity, JCElement<N> data);
	}

	enum PlayerDeath implements EntityAttachSetting {
		COPY_ON_DEATH,
		COPY_IF_KEEP_INVENTORY
	}
}
