package net.devtech.jerraria.item;

import java.util.function.Function;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.devtech.jerraria.content.AirItem;
import net.devtech.jerraria.content.Items;
import net.devtech.jerraria.registry.DefaultIdentifiedObject;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.registry.IdentifiedObject;
import net.devtech.jerraria.registry.Registry;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.jerracode.element.JCElement;

/**
 * An Immutable variant of an item
 */
public abstract class Item {
	public static final Item.Stack EMPTY_STACK = null;
	private Item.Type<?> type;

	protected Item(Type<?> type) {
		this.type = type;
	}

	protected Item(Item inherited) {
		this(inherited.type);
	}

	protected int getMaxCount() {
		return 81_000;
	}

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

	protected final int hashCode0() {
		return super.hashCode();
	}

	/**
	 * @see #deserializeItem(JCElement)
	 * @see #serializeItem(Item)
	 */
	public Object getType() {
		return this.type;
	}

	protected Item setType(Type<?> type) {
		this.type = type;
		return this;
	}

	public final class Stack {
		final int count;

		public Stack(int count) {
			this.count = count;
		}

		public Item getItem() {
			return Item.this;
		}

		public int getCount() {
			return this.count;
		}

		public Stack withCount(int count) {
			return new Stack(count);
		}

		public Stack withItem(Item item) {
			return item.new Stack(this.count);
		}

		public boolean isEmpty() {
			return this.count == 0 || Item.this instanceof AirItem;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			return o instanceof Stack stack && (this.count == stack.count || this.isEmpty() && stack.isEmpty());
		}

		@Override
		public int hashCode() {
			if(this.isEmpty()) {
				return 0;
			}
			return this.count * 31 + Item.this.hashCode();
		}
	}

	public static <T> Type<T> createType(Function<Item, JCElement<T>> serializer, Function<JCElement<T>, Item> deserializer) {
		return new TypeImpl<>(serializer, deserializer);
	}

	public interface Type<T> extends IdentifiedObject {
		JCElement<T> serialize(Item instance);

		Item deserialize(JCElement<T> element);
	}

	public static Item deserializeItem(JCElement<?> element) {
		if(element.type() == NativeJCType.POOLED_PACKED_ID) {
			return Items.REGISTRY.getForId(element.castTo(NativeJCType.POOLED_PACKED_ID)).deserialize(null);
		} else {
			Pair<Id.Full, JCElement> pair = element.castTo(NativeJCType.ID_ANY);
			Type<?> id = Items.REGISTRY.getForId(pair.first());
			return id.deserialize(pair.second());
		}
	}

	public static JCElement<?> serializeItem(Item item) {
		Type<?> type = (Type<?>) item.getType();
		JCElement<?> serialize = type.serialize(item);
		Id.Full id = Items.REGISTRY.getId(type);
		if(serialize == null) {
			return JCElement.create(NativeJCType.POOLED_PACKED_ID, id);
		} else {
			Pair<Id.Full, JCElement> pair = new ObjectObjectImmutablePair<>(id, serialize);
			return JCElement.create(NativeJCType.ID_ANY, pair);
		}
	}

	public static final class TypeImpl<T> extends DefaultIdentifiedObject implements Type<T> {
		private final Function<Item, JCElement<T>> serializer;
		private final Function<JCElement<T>, Item> deserializer;

		public TypeImpl(Function<Item, JCElement<T>> serializer, Function<JCElement<T>, Item> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		@Override
		public JCElement<T> serialize(Item instance) {
			return serializer.apply(instance);
		}

		@Override
		public Item deserialize(JCElement<T> element) {
			return deserializer.apply(element);
		}

		@Override
		protected Registry<?> getValidRegistry() {
			return Items.REGISTRY;
		}
	}
}
