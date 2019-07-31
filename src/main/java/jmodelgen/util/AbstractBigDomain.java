package jmodelgen.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import jmodelgen.core.BigDomain;
import jmodelgen.core.Domain;

public abstract class AbstractBigDomain<T> implements BigDomain<T> {

	@Override
	public BigDomain<T> slice(final BigInteger start, final BigInteger end) {
		return new Slice<>(this,start,end);
	}

	@Override
	public String toString() {
		String r = "{";
		boolean firstTime = true;
		for(T t : this) {
			if(!firstTime) {
				r += ",";
			}
			firstTime=false;
			r += t.toString();
		}
		return r + "}";
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private BigInteger index = BigInteger.ZERO;
			@Override
			public boolean hasNext() {
				return index.compareTo(bigSize()) < 0;
			}

			@Override
			public T next() {
				T r = get(index);
				index = index.add(BigInteger.ONE);
				return r;
			}

		};
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
	public static abstract class Unary<T, S> extends AbstractBigDomain<T> {
		protected final BigDomain<S> subdomain;

		public Unary(BigDomain<S> subdomain) {
			this.subdomain = subdomain;
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
	public static abstract class Binary<T, L, R> extends AbstractBigDomain<T> {
		private final BigDomain<L> left;
		private final BigDomain<R> right;
		private final BigInteger bigSize;

		public Binary(BigDomain<L> left, BigDomain<R> right) {
			this.left = left;
			this.right = right;
			this.bigSize = left.bigSize().multiply(right.bigSize());
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
	public static abstract class Ternary<T, P, Q, R> extends AbstractBigDomain<T> {
		private final BigDomain<P> first;
		private final BigDomain<Q> second;
		private final BigDomain<R> third;
		private final BigInteger bigSize;

		public Ternary(BigDomain<P> first, BigDomain<Q> second, BigDomain<R> third) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.bigSize = first.bigSize().multiply(second.bigSize()).multiply(third.bigSize());
		}

		@Override
		public BigInteger bigSize() {
			return bigSize;
		}

		@Override
		public T get(BigInteger index) {
			BigInteger first_size = first.bigSize();
			BigInteger second_size = second.bigSize();
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
	public static abstract class Nary<T, S> extends AbstractBigDomain<T> {
		private final int min;
		private final int max;
		private final BigDomain<S> generator;
		private final BigInteger bigSize;

		public Nary(int max, BigDomain<S> generator) {
			this(0,max,generator);
		}

		public Nary(int min, int max, BigDomain<S> generator) {
			this.min = min;
			this.max = max;
			this.generator = generator;
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
		public BigInteger bigSize() {
			return bigSize;
		}

		@Override
		public T get(BigInteger index) {
			ArrayList<S> items = new ArrayList<>();
			final BigInteger generator_size = generator.bigSize();
			// Yes, this one is a tad complex
			for (int i = min; i <= max; ++i) {
				BigInteger delta = delta(generator_size, i);
				if (index.compareTo(delta) < 0) {
					for (int j = 0; j < i; ++j) {
						items.add(generator.get(index.remainder(generator_size)));
						index = index.divide(generator_size);
					}
					break;
				}
				index = index.subtract(delta);
			}
			return generate(items);
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

	private static class Slice<T> extends AbstractBigDomain<T> {
		private final AbstractBigDomain<T> parent;
		private final BigInteger start;
		private final BigInteger size;

		public Slice(AbstractBigDomain<T> parent, BigInteger start, BigInteger end) {
			this.parent = parent;
			this.start = start;
			this.size = end.subtract(start);
		}

		@Override
		public BigInteger bigSize() {
			return size;
		}

		@Override
		public T get(BigInteger index) {
			return parent.get(index.add(start));
		}

		@Override
		public BigDomain<T> slice(BigInteger start, BigInteger end) {
			BigInteger size = bigSize();
			// Sanity check parameters
			if(start.compareTo(BigInteger.ZERO) < 0 || start.compareTo(size) > 0) {
				throw new IllegalArgumentException("invalid start");
			} else if(end.compareTo(start) < 0 || end.compareTo(size) > 0) {
				throw new IllegalArgumentException("invalid end");
			}
			// Create slice directly on parent
			BigInteger this_end = this.start.add(this.size);
			return new Slice<>(parent, start.add(this.start), end.add(this_end));
		}
	};
}
