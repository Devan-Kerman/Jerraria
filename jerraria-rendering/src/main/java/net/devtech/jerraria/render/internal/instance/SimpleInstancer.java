package net.devtech.jerraria.render.internal.instance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.render.api.instanced.InstanceKey;
import net.devtech.jerraria.render.api.instanced.InstanceManager;
import net.devtech.jerraria.render.api.instanced.InstanceRelocator;
import net.devtech.jerraria.render.api.instanced.Instancer;

public abstract class SimpleInstancer<T> implements Instancer<T> {
	final List<BlockImpl> blocks = new ArrayList<>();
	final List<InstanceKey<T>> livingKeys = new ArrayList<>();
	final InstanceRelocator<T> relocator;
	final T model;
	final int instancesPerBlock;

	public SimpleInstancer(InstanceRelocator<T> relocator, T model, int block) {
		this.relocator = relocator;
		this.model = model;
		this.instancesPerBlock = block;
	}

	@Override
	public InstanceKey<T> getOrAllocateId() {
		for(BlockImpl block : this.blocks) {
			if(block.manager.hasVacancy()) {
				return block.allocateKey();
			}
		}
		InstanceManager manager;
		if(this.instancesPerBlock > 16384) {
			manager = new InstanceManager.Resizable(16384, this.instancesPerBlock);
		} else {
			manager = new InstanceManager(this.instancesPerBlock);
		}
		BlockImpl impl = new BlockImpl(this.copy(this.model), manager);
		this.blocks.add(impl);
		return impl.allocateKey();
	}

	protected abstract T copy(T model);

	@Override
	public List<? extends Block<T>> compactAndGetBlocks() {
		Iterator<InstanceKey<T>> living = this.livingKeys.iterator();
		while(living.hasNext()) {
			InstanceKey<T> key = living.next();
			if(key.isValid()) {
				for(Predicate<InstanceKey<T>> heartbeat : ((InstanceKeyImpl) key).heartbeat) {
					if(!heartbeat.test(key)) {
						key.invalidate();
						living.remove();
						break;
					}
				}
			} else {
				living.remove();
			}
		}

		// compact the blocks
		List<BlockImpl> list = this.blocks;
		list.sort(Comparator.comparingInt(b -> -b.manager.getActiveInstances())); // insertion sort would be better but
		// idc
		int last = list.size();
		for(int src = 0; src < last; src++) {
			BlockImpl block = list.get(src);
			for(int from = last - 1; from > src && block.manager.hasVacancy(); from--) {
				BlockImpl pull = list.get(from);
				int instances = pull.manager.getActiveInstances();
				if(instances == 0) {
					last = from;
				} else {
					for(int i = 0; i < instances && block.manager.hasVacancy(); i++) {
						var iterator = pull.keys.values().iterator();
						InstanceKeyImpl stolen = iterator.next(), copy = stolen.copy();
						iterator.remove();
						stolen.id = block.manager.allocateId();
						stolen.block = block;
						block.keys.put(stolen.id, stolen);
						this.relocator.copy(copy, stolen);
					}
				}
			}
		}

		// compact arrays with instance manager
		for(int i = 0; i < last; i++) {
			BlockImpl block = list.get(i);
			InstanceManager manager = block.manager;
			manager.copyAction((originalId, newId) -> {
				InstanceKeyImpl to = block.keys.remove(originalId), from = to.copy();
				to.id = newId;
				block.keys.put(newId, to);
				this.relocator.copy(from, to);
			});
		}

		return this.blocks.subList(0, last);
	}

	class BlockImpl implements Block<T> {
		final Int2ObjectMap<InstanceKeyImpl> keys = new Int2ObjectOpenHashMap<>();
		final T block;
		final InstanceManager manager;

		BlockImpl(T block, InstanceManager manager) {
			this.block = block;
			this.manager = manager;
		}

		@Override
		public T block() {
			return this.block;
		}

		@Override
		public int instances() {
			return this.manager.getActiveInstances();
		}

		public InstanceKeyImpl allocateKey() {
			InstanceKeyImpl key = new InstanceKeyImpl(this, this.manager.allocateId());
			this.keys.put(key.id, key);
			return key;
		}
	}

	class InstanceKeyImpl implements InstanceKey<T> {
		BlockImpl block;
		int id;
		List<Predicate<InstanceKey<T>>> heartbeat;
		boolean isValid = true;

		InstanceKeyImpl(BlockImpl block, int id) {
			this.block = block;
			this.id = id;
		}

		@Override
		public T block() {
			this.assertValidity();
			return this.block.block;
		}

		@Override
		public int id() {
			this.assertValidity();
			return this.id;
		}

		@Override
		public void invalidate() {
			if(this.isValid) {
				this.isValid = false;
				this.block.manager.returnId(this.id);
				if(this.block.keys.remove(this.id) != this) {
					throw new IllegalStateException("Weird invalidation!");
				}
			}
		}

		@Override
		public void addHeartbeat(Predicate<InstanceKey<T>> predicate) {
			this.assertValidity();
			var heartbeat = this.heartbeat;
			if(heartbeat == null) {
				this.heartbeat = heartbeat = new ArrayList<>();
				SimpleInstancer.this.livingKeys.add(this);
			}
			heartbeat.add(predicate);
		}

		@Override
		public boolean isValid() {
			return this.isValid;
		}

		public InstanceKeyImpl copy() {
			return new InstanceKeyImpl(this.block, this.id);
		}
	}
}
