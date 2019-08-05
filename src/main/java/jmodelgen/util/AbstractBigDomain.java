package jmodelgen.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import jmodelgen.core.Domain;

public abstract class AbstractBigDomain<T> extends AbstractDomain<T> implements Domain.Static<T> {

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
	public static abstract class Adaptor<T, S> extends AbstractBigDomain<T> {
		protected final Domain.Static<S> subdomain;

		public Adaptor(Domain.Static<S> subdomain) {
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
	public static abstract class BinaryProduct<T, L, R> extends AbstractBigDomain<T> {
		private final Domain.Static<L> left;
		private final Domain.Static<R> right;
		private final BigInteger bigSize;

		public BinaryProduct(Domain.Static<L> left, Domain.Static<R> right) {
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
	public static abstract class TernaryProduct<T, P, Q, R> extends AbstractBigDomain<T> {
		private final Domain.Static<P> first;
		private final Domain.Static<Q> second;
		private final Domain.Static<R> third;
		private final BigInteger bigSize;

		public TernaryProduct(Domain.Static<P> first, Domain.Static<Q> second, Domain.Static<R> third) {
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
	public static abstract class NaryProduct<T, S> extends AbstractBigDomain<T> implements Domain.Static<T> {
		/**
		 * Determines the length of each element in this nary product.
		 */
		private final S[] element;
		/**
		 * The generator used for each element in this nary product.
		 */
		private final Domain.Static<S> generator;
		/**
		 * The actual size of the entire domain of this product.
		 */
		private final BigInteger size;

		public NaryProduct(S[] element, Domain.Static<S> generator) {
			this.size = generator.bigSize().pow(element.length);
			this.element = element;
			this.generator = generator;
		}

		@Override
		public BigInteger bigSize() {
			return size;
		}

		@Override
		public T get(BigInteger index) {
			final BigInteger generator_size = generator.bigSize();
			//
			for (int i = 0; i != element.length; ++i) {
				element[i] = generator.get(index.remainder(generator_size));
				index = index.divide(generator_size);
			}
			return generate(element);
		}

		/**
		 * Generate an element of the target domain from the given (completed) element
		 * array.
		 *
		 * @param items
		 * @return
		 */
		public abstract T generate(S[] element);
	}


	/**
	 * The union of one or more domains.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static class NarySum<T> extends AbstractBigDomain<T> {
		private final Domain.Static<? extends T>[] elements;
		private final BigInteger size;

		public NarySum(Domain.Static<? extends T>... elements) {
			this.elements = elements;
			BigInteger s = BigInteger.ZERO;
			// Compute sume
			for(int i=0;i!=elements.length;++i) {
				s = s.add(elements[i].bigSize());
			}
			this.size = s;
		}

		@Override
		public BigInteger bigSize() {
			return size;
		}

		@Override
		public T get(BigInteger index) {
			BigInteger sum = BigInteger.ZERO;
			for (int i = 0; i != elements.length; ++i) {
				Domain.Static<? extends T> ith = elements[i];
				BigInteger _sum = sum.add(ith.bigSize());
				if (index.compareTo(_sum) < 0) {
					return ith.get(index.subtract(sum));
				}
				sum = _sum;
			}
			throw new IllegalArgumentException("invalid index");
		}
	}

	/**
	 * A subdomain determine by a given set of "samples". Observe that a sampled
	 * domain is always "small".
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static class Sample<T> extends AbstractSmallDomain<T> {
		private final Domain.Static<T> domain;
		private final BigInteger[] samples;

		public Sample(Domain.Static<T> domain, BigInteger[] samples) {
			this.domain = domain;
			this.samples = samples;
		}

		@Override
		public int size() {
			return samples.length;
		}

		@Override
		public T get(int index) {
			return domain.get(samples[index]);
		}
	}
}
