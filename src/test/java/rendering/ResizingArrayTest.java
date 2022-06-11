package rendering;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ResizingArrayTest {
	public static final VarHandle HANDLE;

	static {
		try {
			HANDLE = MethodHandles.lookup().findVarHandle(ResizingArrayTest.class, "arr", Object[].class);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	volatile Object[] arr;
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ResizingArrayTest test = new ResizingArrayTest();
		ExecutorService executor = Executors.newFixedThreadPool(16);
		Collection<Callable<Object>> futures = new ArrayList<>();
		for(int i = 0; i < 16384; i++) {
			int val = i;
			futures.add(() -> {
				test.set(val/9, val/9);
				return null;
			});
		}
		System.out.println("Yes");
		for(Future<Object> future : executor.invokeAll(futures)) {
			future.get();
		}
		System.out.println(executor.shutdownNow());
		System.out.println(Arrays.toString(test.arr));
	}

	public void set(int index, Object val) {
		Object[] arr, new_;
		do {
			new_ = arr = this.arr;
			if(arr == null) {
				new_ = new Object[index + 1];
			} else if(index >= arr.length) {
				new_ = Arrays.copyOf(arr, index + 1);
			}
			new_[index] = val;
		} while(!HANDLE.compareAndSet(this, arr, new_));
	}
}
