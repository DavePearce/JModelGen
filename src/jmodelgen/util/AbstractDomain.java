package jmodelgen.util;

import java.util.ArrayList;
import java.util.List;

import jmodelgen.core.Domain;

public abstract class AbstractDomain<T> implements Domain<T> {

	@Override
	public Domain<T> slice(final long start, final long end) {
		return new Slice<>(this,start,end);
	}

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
	public static abstract class Unary<T, S> extends AbstractDomain<T> implements Domain<T> {
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
	public static abstract class Binary<T, L, R> extends AbstractDomain<T> implements Domain<T> {
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
	 * An abstract domain for values composed from exactly three children. For
	 * example, if generating random mathematical expressions then the C-like
	 * ternary "?" operator could extend this class. The size of this domain is the
	 * product of the three subdomains.
	 *
	 * @author David J. Pearce
	 *
	 * @param <P>
	 * @param <Q>
	 * @param <R>
	 */
	public static abstract class Ternary<T, P, Q, R> extends AbstractDomain<T> implements Domain<T> {
		private final Domain<P> first;
		private final Domain<Q> second;
		private final Domain<R> third;

		public Ternary(Domain<P> first, Domain<Q> second, Domain<R> third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		@Override
		public long size() {
			return first.size() * second.size() * third.size();
		}

		@Override
		public T get(long index) {
			// NOTE: these could be precomputed and cached
			long first_size = first.size();
			long second_size = first.size();
			P p = first.get(index % first_size);
			index = index / first_size;
			Q q = second.get(index % second_size);
			index = index / second_size;
			R r = third.get(index);
			return get(p, q, r);
		}

		public abstract T get(P first, Q second, R third);
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
	public static abstract class Nary<T, S> extends AbstractDomain<T> implements Domain<T> {
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

		public abstract T generate(java.util.List<S> items);
	}

	// =======================================================================
	// Helper methdos / classes
	// =======================================================================

	private static class Slice<T> implements Domain<T> {
		private final AbstractDomain<T> parent;
		private final long start;
		private final long end;

		public Slice(AbstractDomain<T> parent, long start, long end) {
			this.parent = parent;
			this.start = start;
			this.end = end;
		}

		@Override
		public long size() {
			return end - start;
		}

		@Override
		public T get(long index) {
			return parent.get(index+start);
		}

		@Override
		public Domain<T> slice(long start, long end) {
			long size = size();
			// Sanity check parameters
			if(start < 0 || start > size) {
				throw new IllegalArgumentException("invalid start");
			} else if(end < start || end > size) {
				throw new IllegalArgumentException("invalid end");
			}
			// Create slice directly on parent
			return new Slice<>(parent, start + this.start, end + this.end);
		}
	};

}
