package rendering;

import java.util.Arrays;

public class SortTest {
	record uvec2(float a, float b) {}
	static void sort(uvec2[] fragments, int n) {
		for(int i = 1; i < n; i++) {
			uvec2 current = fragments[i];
			float depthA = current.b();
			int r = i-1;
			for(; r >= 0; r--) {
				uvec2 comp = fragments[r];
				float depthB = comp.b();
				if(depthA < depthB) {
					fragments[r+1] = comp;
				} else {
					break;
				}
			}
			fragments[r+1] = current;
		}
	}

	public static void main(String[] args) {
		uvec2[] arr = new uvec2[100];
		for(int i = 0; i < arr.length; i++) {
			arr[i] = new uvec2(i, (float) Math.random());
		}
		sort(arr, 100);
		System.out.println(Arrays.toString(arr).replace("uvec2", "\n"));
	}
}
