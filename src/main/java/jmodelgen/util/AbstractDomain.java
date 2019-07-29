package jmodelgen.util;

import java.math.BigInteger;
import java.util.ArrayList;

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

		@Override
		public BigInteger bigSize() {
			return subdomain.bigSize();
		}

		@Override
		public T get(BigInteger index) {
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
		private final long size;
		private final BigInteger bigSize;

		public Binary(Domain<L> left, Domain<R> right) {
			this.left = left;
			this.right = right;
			this.size = left.size() * right.size();
			this.bigSize = left.bigSize().multiply(right.bigSize());
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			long left_size = left.size();
			L l = left.get(index % left_size);
			R r = right.get(index / left_size);
			return get(l, r);
		}

		@Override
		public BigInteger bigSize() {
			return bigSize;
		}

		@Override
		public T get(BigInteger index) {
			BigInteger left_size = left.bigSize();
			L l = left.get(index.remainder(left_size));
			R r = right.get(index.divide(left_size));
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
		private final long size;
		private final BigInteger bigSize;

		public Ternary(Domain<P> first, Domain<Q> second, Domain<R> third) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.size = first.size() * second.size() * third.size();
			this.bigSize = first.bigSize().multiply(second.bigSize()).multiply(third.bigSize());
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			long first_size = first.size();
			long second_size = first.size();
			P p = first.get(index % first_size);
			index = index / first_size;
			Q q = second.get(index % second_size);
			index = index / second_size;
			R r = third.get(index);
			return get(p, q, r);
		}

		@Override
		public BigInteger bigSize() {
			return bigSize;
		}

		@Override
		public T get(BigInteger index) {
			BigInteger first_size = first.bigSize();
			BigInteger second_size = first.bigSize();
			P p = first.get(index.remainder(first_size));
			index = index.divide(first_size);
			Q q = second.get(index.remainder(second_size));
			index = index.divide(second_size);
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
		private final int min;
		private final int max;
		private final Domain<S> generator;
		private final long size;
		private final BigInteger bigSize;

		public Nary(int max, Domain<S> generator) {
			this(0,max,generator);
		}

		public Nary(int min, int max, Domain<S> generator) {
			this.min = min;
			this.max = max;
			this.generator = generator;
			// Long size calculation
			long generator_size = generator.size();
			long acc = 0;
			for (int i = min; i <= max; ++i) {
				long delta = delta(generator_size, i);
				acc = acc + delta;
			}
			this.size = acc;
			// BigInteger size calculation
			BigInteger generator_bigsize = generator.bigSize();
			BigInteger bigacc = BigInteger.ZERO;
			for (int i = min; i <= max; ++i) {
				BigInteger delta = delta(generator_bigsize, i);
				bigacc = bigacc.add(delta);
			}
			this.bigSize = bigacc;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			ArrayList<S> items = new ArrayList<>();
			final long generator_size = generator.size();
			// Yes, this one is a tad complex
			for (int i = min; i <= max; ++i) {
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

		@Override
		public BigInteger bigSize() {
			return bigSize;
		}

		@Override
		public T get(BigInteger index) {
			throw new UnsupportedOperationException();
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

		private static BigInteger delta(BigInteger base, int power) {
			if (power == 0) {
				// special case as only one empty sequence
				return BigInteger.ONE;
			} else {
				BigInteger r = base;
				for (int i = 1; i < power; ++i) {
					r = r.multiply(base);
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
		public BigInteger bigSize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public T get(BigInteger index) {
			throw new UnsupportedOperationException();
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
