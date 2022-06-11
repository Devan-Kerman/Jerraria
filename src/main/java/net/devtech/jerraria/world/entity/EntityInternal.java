package net.devtech.jerraria.world.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.attachment.Attachment;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.jerraria.Entities;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.attach.EntityAttachSetting;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EntityInternal {
	static final Map<Id, SerializableAttachment<?>> SERIALIZABLE_ATTACHMENTS = new HashMap<>();

	static {
		Entity.PROVIDER.registerListener((attachment, behavior) -> {
			boolean first = true;
			for(EntityAttachSetting settings : behavior) {
				if(settings instanceof EntityAttachSetting.Serializer s) {
					if(first) {
						SERIALIZABLE_ATTACHMENTS.put(s.getKey(), new SerializableAttachment<>(attachment, s));
						first = false;
					} else {
						throw new UnsupportedOperationException("Cannot have multiple serializers for an attachment");
					}
				}
			}
		});
	}

	public static Entity deserialize(World world, Id id, SerializedEntity element) {
		Entity.Type<?> type = Entities.REGISTRY.getForId(id);
		List<Pair<Id, JCElement>> data = element.data.as(NativeJCType.ID_ANY_LIST);
		JCElement custom = null;
		for(Pair<Id, JCElement> datum : data) {
			Id left = datum.left();
			if(DEFAULT_DATA.equals(left)) { // should be first
				custom = datum.right();
				break;
			}
		}

		Entity entity = type.deserialize(custom, world, element.x, element.y);
		for(Pair<Id, JCElement> datum : data) {
			Id left = datum.left();
			if(!DEFAULT_DATA.equals(left)) { // should be first
				SerializableAttachment<?> attachment = SERIALIZABLE_ATTACHMENTS.get(left);
				attachment.deserialize(entity, datum.right());
			}
		}
		return entity;
	}

	private static final Id DEFAULT_DATA = Id.create("jerraria", "custom");
	public static SerializedEntity serialize(Entity entity) {
		JCElement<?> serialize = entity.type.serialize(entity);
		List<Pair<Id, JCElement>> list = new ArrayList<>();
		list.add(Pair.of(DEFAULT_DATA, serialize));
		Set<Id> names = new HashSet<>();
		names.add(DEFAULT_DATA);
		SERIALIZABLE_ATTACHMENTS.forEach((id, attachment) -> {
			if(!names.add(id)) {
				throw new IllegalStateException("Attachment has non-unique serializer id " + id);
			}
			JCElement<?> decode = attachment.serialize(entity);
			if(decode != null) {
				list.add(Pair.of(id, decode));
			}
		});

		return new SerializedEntity(entity.x(), entity.y(), JCElement.create(NativeJCType.ID_ANY_LIST, list));
	}

	public static void setWorld(Entity entity, World world) {
		entity.moveGroup(world);
	}

	public static void tickPos(Entity entity) {
		entity.tickPosition();
	}

	public static void setHomeChunk(Entity entity, Chunk chunk) {
		entity.setHomeChunk(chunk);
	}

	public static boolean isHomeChunk(Entity entity, Chunk chunk) {
		return entity.isHomeChunk(chunk);
	}

	public static void tick(Entity entity) {
		entity.tick();
	}

	record SerializableAttachment<T>(
		Attachment<Entity, T> attachment,
		EntityAttachSetting.Serializer<Entity, T, ?> serializer
	) {
		public JCElement<?> serialize(Entity entity) {
			return this.serializer.serialize(this.attachment, entity);
		}

		public void deserialize(Entity entity, JCElement element) {
			this.serializer.deserialize(this.attachment, entity, element);
		}
	}
}
