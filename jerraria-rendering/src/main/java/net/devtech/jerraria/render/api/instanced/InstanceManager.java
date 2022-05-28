package net.devtech.jerraria.render.api.instanced;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Computes the minimum number of copy actions necessary to convert a sparse array into a compact one. This is useful
 * for instancing where particular instances may be removed or added back in the course of a frame, and then must be
 * rendered as one sequential array.
 */
public class InstanceManager {
	/**
	 * Stores unused ids in reverse order
	 */
	final IntList unused;
	/**
	 * Stores currently active instance ids in reverse order
	 */
	final IntList active;
	int maxInstances;

	public InstanceManager(int maxInstances) {
		this.unused = new IntArrayList(maxInstances);
		for(int instance = maxInstances - 1; instance >= 0; instance--) {
			this.unused.add(instance);
		}

		this.active = new IntArrayList(maxInstances);
		this.maxInstances = maxInstances;
	}

	public void returnId(int id) {
		binaryInsert(this.unused, id, -1);
		binaryRemove(this.active, id, -1);
	}

	public int allocateId() {
		int from = this.unused.removeInt(this.unused.size() - 1);
		binaryInsert(this.active, from, -1);
		return from;
	}

	public void copyAction(CopyHandler handler) {
		int size = this.active.size();
		int usedId = 0, counter = size - 1;
		for(int instanceId = 0; instanceId < (size - usedId); instanceId++) {
			int last = this.active.getInt(counter);
			if(last != instanceId) {
				int id = usedId++;
				int replacement = this.active.getInt(id);
				handler.relocate(replacement, instanceId);
				this.active.set(id, instanceId);
				binaryInsert(this.unused, replacement, -1);
			} else {
				counter--;
			}
		}
	}

	/**
	 * This won't change whether it is compressed or not
	 */
	public int getActiveInstances() {
		return this.active.size();
	}

	/**
	 * SSBO instanced rendering has an "unlimited" size
	 */
	public static class Resizable extends InstanceManager {
		public Resizable(int expectedInstances) {
			super(expectedInstances);
		}

		@Override
		public int allocateId() {
			if(this.unused.isEmpty()) {
				for(int i = this.maxInstances*2; i > 0; i--) {
					this.unused.add((i-1) + this.maxInstances);
				}
				this.maxInstances *= 2;
			}
			return super.allocateId();
		}
	}

	/**
	 * @param mul -1 for reversed-order, 1 for in-order
	 */
	public static void binaryInsert(IntList ids, int id, int mul) {
		int low = 0;
		int high = ids.size() - 1;
		while(low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = mul * ids.getInt(mid);
			if(midVal < id * mul) {
				low = mid + 1;
			} else if(midVal > id * mul) {
				high = mid - 1;
			} else {
				return;
			}
		}
		ids.add(low, id);
	}

	public static void binaryRemove(IntList ids, int id, int mul) {
		int low = 0;
		int high = ids.size() - 1;
		while(low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = mul * ids.getInt(mid);
			if(midVal < id * mul) {
				low = mid + 1;
			} else if(midVal > id * mul) {
				high = mid - 1;
			} else {
				ids.removeInt(mid);
				return;
			}
		}
	}

	public interface CopyHandler {
		void relocate(int originalId, int newId);
	}
}
