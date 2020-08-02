package sorting;

import java.util.Arrays;

public class SelectionSort {
	public static void selectionSort(int[] vals) {
		for (int i = 0; i < vals.length - 1; i++) {
			int indexMin = i;
			for (int j = i+1; j < vals.length; j++) {
				if (vals[j] < vals[indexMin]) {
					indexMin = j;
				}
			}
			swap(vals, indexMin, i);
		}
	}
	
	private static void swap(int[] vals, int indexMin, int i) {
		int intMin = vals[indexMin];
		int intI = vals[i];
		vals[indexMin] = intI;
		vals[i] = intMin;
	}
	
	
	public static void main(String[] args) {
		int[] vals = {4, 3, 5, 9, 7};
		selectionSort(vals);
		System.out.println(Arrays.toString(vals));
	}
}
