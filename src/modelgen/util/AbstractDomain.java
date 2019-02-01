package modelgen.util;

import java.util.ArrayList;
import java.util.List;

import modelgen.core.Domain;

public abstract class AbstractDomain {

	/**
	 * An abstract domain for values composed from exactly one child. For example,
	 * if generating random logical expressions then the "not" operator would extend
	 * this class. The size of this domain exactly matches that of the subdomain.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 * @param <S>
	 */
	public static abstract class Unary<T, S> implements Domain<T> {
		protected final Domain<S> subdomain;

		public Unary(Domain<S> subdomain) {
			this.subdomain = subdomain;
		}

		@Override
		public long size() {
			return subdomain.size();
		}

		@Override
		public T get(long index) {
			S s = subdomain.get(index);
			return get(s);
		}

		public abstract T get(S s);
	}

	/**
	 * An abstract domain for values composed from exactly two children. For
	 * example, if generating random mathematical expressions then the "add"
	 * operator could extend this class. The size of this domain is the product of
	 * the two subdomains.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 * @param <S>
	 */
	public static abstract class Binary<T, L, R> implements Domain<T> {
		private final Domain<L> left;
		private final Domain<R> right;

		public Binary(Domain<L> left, Domain<R> right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public long size() {
			return left.size() * right.size();
		}

		@Override
		public T get(long index) {
			long left_size = left.size();
			L l = left.get(index % left_size);
			R r = right.get(index / left_size);
			return get(l, r);
		}

		public abstract T get(L left, R right);
	}

	/**
	 * An abstract generator for values composed from an arbitrary number of
	 * children. For example, if generating random logical expressions then the "or"
	 * operator would extend this class. The size of this domain is the size of the
	 * subdomain taken to the power of the maximum number of elements permitted.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 * @param <S>
	 */
	public static abstract class Nary<T, S> implements Domain<T> {
		private final int max;
		private final Domain<S> generator;

		public Nary(int max, Domain<S> generator) {
			this.max = max;
			this.generator = generator;
		}

		@Override
		public long size() {
			long generator_size = generator.size();
			long size = 0;
			for (int i = 0; i <= max; ++i) {
				long delta = delta(generator_size, i);
				size = size + delta;
			}
			return size;
		}

		@Override
		public T get(long index) {
			ArrayList<S> items = new ArrayList<>();
			final long generator_size = generator.size();
			// Yes, this one is a tad complex
			for (int i = 0; i <= max; ++i) {
				long delta = delta(generator_size, i);
				if (index < delta) {
					for (int j = 0; j < i; ++j) {
						items.add(generator.get(index % generator_size));
						index = index / generator_size;
					}
					break;
				}
				index = index - delta;
			}
			return generate(items);
		}

		private static long delta(long base, int power) {
			if (power == 0) {
				// special case as only one empty sequence
				return 1;
			} else {
				long r = base;
				for (int i = 1; i < power; ++i) {
					r = r * base;
				}
				return r;
			}
		}

		public abstract T generate(List<S> items);
	}

}
