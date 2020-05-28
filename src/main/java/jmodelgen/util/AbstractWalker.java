package jmodelgen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import jmodelgen.core.Walker;

public abstract class AbstractWalker<T> implements Walker<T> {

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return !finished();
			}

			@Override
			public T next() {
				T r = get();
				AbstractWalker.this.next();
				return r;
			}

		};
	}

	public abstract static class Unary<T, S> extends AbstractWalker<T> {
		private final Walker<S> walker;

		public Unary(Walker<S> walker) {
			this.walker = walker;
		}

		@Override
		public void reset() {
			walker.reset();
		}

		@Override
		public boolean finished() {
			return walker.finished();
		}

		@Override
		public T get() {
			return get(walker.get());
		}

		@Override
		public void next() {
			walker.next();
		}

		@Override
		public long advance(long n) {
			return walker.advance(n);
		}

		public abstract T get(S item);
	}

	public abstract static class Binary<T, L, R> extends AbstractWalker<T> {
		private final Walker<L> left;
		private final Walker<R> right;

		public Binary(Walker<L> left, Walker<R> right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public void reset() {
			left.reset();
			right.reset();
		}

		@Override
		public boolean finished() {
			return right.finished();
		}

		@Override
		public T get() {
			return get(left.get(),right.get());
		}

		@Override
		public void next() {
			left.next();
			if(left.finished()) {
				left.reset();
				right.next();
			}
		}

		@Override
		public long advance(long n) {
			long m = n;
			while(n > 0 && !right.finished()) {
				n = n - left.advance(n);
				if(left.finished()) {
					left.reset();
					right.next();
				}
			}
			return m - n;
		}

		public abstract T get(L left, R right);
	}

	public abstract static class Nary<T, S> extends AbstractWalker<T> {
		private final int min;
		private final Walker<S>[] walkers;
		private int size;

		public Nary(int min, Walker<S>... walkers) {
			this.min = min;
			this.size = min;
			this.walkers = walkers;
		}

		@Override
		public void reset() {
			this.size = min;
			for (int i = 0; i != walkers.length; ++i) {
				walkers[i].reset();
			}
		}

		@Override
		public boolean finished() {
			return size > walkers.length;
		}

		@Override
		public T get() {
			ArrayList<S> items = new ArrayList<>();
			for (int i = 0; i != size; ++i) {
				items.add(walkers[i].get());
			}
			return get(items);
		}

		@Override
		public void next() {
			for (int i = 0; i < size; ++i) {
				Walker<S> walker = walkers[i];
				walker.next();
				if (walker.finished()) {
					walker.reset();
				} else {
					return;
				}
			}
			size = size + 1;
		}

		@Override
		public long advance(long n) {
			if(n == 0) {
				return 0;
			} else if(size == 0) {
				size = size + 1;
				return 1 + advance(n - 1);
			} else {
				Walker<S> walker = walkers[0];
				long m = walker.advance(n);
				// Propagate carry
				if(walker.finished()) {
					walker.reset();
					for (int i = 1; i < size; ++i) {
						walker = walkers[i];
						walker.next();
						if (walker.finished()) {
							walker.reset();
						} else {
							return m + advance(n - m);
						}
					}
					size = size + 1;
					return m + advance(n - m);
				}
				// Done
				return m;
			}
		}

		abstract public T get(List<S> items);
	}

	public abstract static class LazyNary<T, S> extends AbstractWalker<T> {
		private final int min;
		private final Walker<S>[] walkers;
		private final State<S>[] state;
		private int size;

		@SuppressWarnings("unchecked")
		public LazyNary(int min, int max, State<S> seed) {
			this.min = min;
			this.size = min;
			this.walkers = new Walker[max];
			this.state = new State[max + 1];
			// Initialise minimum number of walkers
			initialise(min,seed);
		}

		@Override
		public void reset() {
			this.size = min;
			for (int i = 0; i != walkers.length; ++i) {
				walkers[i] = null;
				if(i > 0) {
					state[i] = null;
				}
			}
			// Initialise minimum number of walkers
			initialise(min,state[0]);
		}

		@Override
		public boolean finished() {
			return size > walkers.length;
		}

		@Override
		public T get() {
			ArrayList<S> items = new ArrayList<>();
			for (int i = 0; i != size; ++i) {
				items.add(walkers[i].get());
			}
			return get(items);
		}

		@Override
		public void next() {
			// Increment walker
			int index = size - 1;
			// Move leftwards invalidating completed walkers
			while(index >= 0) {
				// Get next walker
				Walker<S> walker = walkers[index];
				// Move walker
				walker.next();
				// Check if has reset
				if (walker.finished()) {
					// Destroy walker
					walkers[index] = null;
					// and transfer state
					state[index + 1] = null;
				} else {
					// Update transfer state
					state[index + 1] = state[index].transfer(walker.get());
					// done
					break;
				}
				// Continue down
				index = index - 1;
			}
			// Increase number of walkers (if appropriate)
			if(index < 0) {
				size = size + 1;
			}
			// Reconstruct walkers as necessary
			for (int i = index + 1; i < Math.min(walkers.length, size); i++) {
				Walker<S> ith = state[i].construct();
				walkers[i] = ith;
				state[i+1] = state[i].transfer(ith.get());
			}
		}

		@Override
		public long advance(long n) {
			final long m = n;
			// Increment walker
			int index = size - 1;
			// Move leftwards invalidating completed walkers
			while(index >= 0) {
				// Get next walker
				Walker<S> walker = walkers[index];
				// Move walker
				n = n - walker.advance(n);
				// Check if has reset
				if (walker.finished()) {
					// Destroy walker
					walkers[index] = null;
					// and transfer state
					state[index + 1] = null;
				} else {
					// Update transfer state
					state[index + 1] = state[index].transfer(walker.get());
					// done
					break;
				}
				// Continue down
				index = index - 1;
			}
			// Increase number of walkers (if appropriate)
			if(index < 0) {
				size = size + 1;
			}
			// Reconstruct walkers as necessary
			for (int i = index + 1; i < Math.min(walkers.length, size); i++) {
				Walker<S> ith = state[i].construct();
				walkers[i] = ith;
				state[i+1] = state[i].transfer(ith.get());
			}
			// Decide whether to do more or not
			if(n == 0) {
				// Have successfully advanced as requested
				return m;
			} else if(size > walkers.length) {
				// Have run out of things to advance through
				return m - n;
			} else {
				// Haven't finished advancing yet!
				return (m - n) + advance(n);
			}
		}

		abstract public T get(List<S> items);

		private void initialise(int min, State<S> seed) {
			state[0] = seed;
			for (int i = 0; i != min; ++i) {
				walkers[i] = state[i].construct();
				state[i+1] = state[i].transfer(walkers[i].get());
			}
		}

		@Override
		public String toString() {
			String r = "[" + state[0] + "]";
			for(int i=0;i!=walkers.length;++i) {
				if(walkers[i] != null) {
					r += "[" + walkers[i].get() + ";" + state[i+1] + "]";
				} else {
					r += "[]";
				}
			}
			return r;
		}
	}

	public static class Option<T> extends AbstractWalker<T> {
		private final Walker<T>[] walkers;
		private int index;

		public Option(Walker<T>... walkers) {
			this.walkers = walkers;
		}

		@Override
		public void reset() {
			this.index = 0;
			for(int i=0;i!=walkers.length;++i) {
				walkers[i].reset();
			}
		}

		@Override
		public boolean finished() {
			return index >= walkers.length;
		}

		@Override
		public T get() {
			return walkers[index].get();
		}

		@Override
		public void next() {
			walkers[index].next();
			// Look for next usable walker
			index = next(index,walkers);
		}

		@Override
		public long advance(long n) {
			final long m = n;
			while(index < walkers.length) {
				Walker<T> w = walkers[index];
				n = n - w.advance(n);
				if(n == 0) {
					// Look for next usable walker
					index = next(index,walkers);
					// Done
					return m;
				}
				index = index + 1;
			}
			return m - n;
		}

		private static <T> int next(int index, Walker<T>[] walkers) {
			while(index < walkers.length && walkers[index].finished()) {
				index = index + 1;
			}
			return index;
		}
	}
}
