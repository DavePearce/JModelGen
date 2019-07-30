package jmodelgen.util;

import java.util.Iterator;

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

		public abstract T get(L left, R right);
	}
}
