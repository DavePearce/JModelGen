package jmodelgen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jmodelgen.core.Transformer;

public class IterativeGenerator<T> implements Iterable<T> {
	private final Transformer<T> transformer;
	private final int max;
	private final T seed;

	public IterativeGenerator(T seed, int max, Transformer<T> transformer) {
		this.transformer = transformer;
		this.max = max;
		this.seed = seed;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>(max,seed,transformer);
	}

	private static class Iterator<T> implements java.util.Iterator<T> {
		private final Transformer<T> transformer;
		private int count;
		private List<T> items;
		private int index;

		public Iterator(int max, T seed, Transformer<T> transformer) {
			this.transformer = transformer;
			this.count = max;
			this.items = Arrays.asList(seed);
		}

		@Override
		public boolean hasNext() {
			return index != items.size();
		}

		@Override
		public T next() {
			// First extract next item
			T item = items.get(index++);
			// Check whether need to roll over
			if (index >= items.size() && count > 0) {
				ArrayList<T> nitems = new ArrayList<>();
				for (int i = 0; i != items.size(); ++i) {
					nitems.addAll(transformer.transform(items.get(i)));
				}
				items = nitems;
				count = count - 1;
				index = 0;
			}
			return item;
		}
	}
}
