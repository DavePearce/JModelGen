package jmodelgen.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import jmodelgen.core.Domain;

public abstract class AbstractSmallDomain<T> extends AbstractDomain<T> implements Domain.Big<T>, Domain.Small<T> {

	@Override
	public BigInteger bigSize() {
		return BigInteger.valueOf(size());
	}

	@Override
	public T get(BigInteger index) {
		return get(index.intValueExact());
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index = 0;
			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public T next() {
				return get(index++);
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
	public static abstract class Adaptor<T, S> extends AbstractSmallDomain<T>  {
		protected final Domain.Small<S> subdomain;

		public Adaptor(Domain.Small<S> subdomain) {
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
	public static abstract class BinaryProduct<T, L, R> extends AbstractSmallDomain<T> implements Domain.Small<T> {
		private final Domain.Small<L> left;
		private final Domain.Small<R> right;
		private final long size;

		public BinaryProduct(Domain.Small<L> left, Domain.Small<R> right) {
			this.size = determineIntegerProduct(left, right);
			this.left = left;
			this.right = right;
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
	public static abstract class TernaryProduct<T, P, Q, R> extends AbstractSmallDomain<T> {
		private final Domain.Small<P> first;
		private final Domain.Small<Q> second;
		private final Domain.Small<R> third;
		private final long size;

		public TernaryProduct(Domain.Small<P> first, Domain.Small<Q> second, Domain.Small<R> third) {
			this.size = determineIntegerProduct(first, second, third);
			this.first = first;
			this.second = second;
			this.third = third;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			long first_size = first.size();
			long second_size = second.size();
			//
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
	public static abstract class NarySequence<T, S> extends AbstractSmallDomain<T> implements Domain.Big<T> {
		/**
		 * Determines the length of each element in this nary product.
		 */
		private final S[] element;
		/**
		 * The generator used for each element in this nary product.
		 */
		private final Domain.Small<? extends S> generator;
		/**
		 * The actual size of the entire domain of this product.
		 */
		private final long size;

		public NarySequence(int n, Domain.Small<? extends S> generator, S... dummy) {
			if (dummy.length != n) {
				dummy = Arrays.copyOf(dummy, n);
			}
			this.size = determineIntegerPower(generator.size(), n);
			this.element = dummy;
			this.generator = generator;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			final long generator_size = generator.size();
			for (int i = 0; i != element.length; ++i) {
				element[i] = generator.get(index % generator_size);
				index = index / generator_size;
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

	public static abstract class NaryProduct<T, S> extends AbstractSmallDomain<T> implements Domain.Big<T> {
		/**
		 * Determines the length of each element in this nary product.
		 */
		private final S[] element;
		/**
		 * The generator used for each element in this nary product.
		 */
		private final Domain.Small<? extends S>[] generators;
		/**
		 * The actual size of the entire domain of this product.
		 */
		private final long size;

		public NaryProduct(Domain.Small<? extends S>[] generators, S... dummy) {
			if(dummy.length != generators.length) {
				dummy = Arrays.copyOf(dummy, generators.length);
			}
			this.size = determineIntegerProduct(generators);
			this.element = dummy;
			this.generators = generators;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			for (int i = 0; i != element.length; ++i) {
				final Domain.Small<? extends S> generator = generators[i];
				final long generator_size = generator.size();
				element[i] = generator.get(index % generator_size);
				index = index / generator_size;
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
	public static class NarySum<T> extends AbstractSmallDomain<T> {
		private final Domain.Small<? extends T>[] elements;
		private final long size;

		public NarySum(Domain.Small<? extends T>... elements) {
			this.size = determineIntegerSum(elements);
			this.elements = elements;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public T get(long index) {
			long sum = 0;
			for (int i = 0; i != elements.length; ++i) {
				Domain.Small<? extends T> ith = elements[i];
				long size = ith.size();
				if (index < (sum + size)) {
					return ith.get(index - sum);
				}
				sum = sum + size;
			}
			throw new IllegalArgumentException("invalid index");
		}
	}

	/**
	 * A subdomain determine by a given set of "samples".
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static class Sample<T> extends AbstractSmallDomain<T> {
		private final Domain.Small<T> domain;
		private final long[] samples;

		public Sample(Domain.Small<T> domain, long[] samples) {
			this.domain = domain;
			this.samples = samples;
		}

		@Override
		public long size() {
			return samples.length;
		}

		@Override
		public T get(long index) {
			return domain.get(samples[(int) index]);
		}
	}

	/**
	 * Determine the integer product of one or more integer elements. If the
	 * resulting value won't fit into an integer, then an
	 * <code>IllegalArgumentException</code> is thrown.
	 *
	 * @param sizes
	 * @return
	 */
	private static long determineIntegerProduct(Domain.Small... domains) {
		if (domains.length > 0) {
			long size = domains[0].size();
			for (int i = 1; i != domains.length; ++i) {
				size = size * domains[i].size();
				if (size < 0) {
					// NOTE: integer overflow
					throw new IllegalArgumentException("invalid integer product");
				}
			}
			return size;
		} else {
			return 0;
		}
	}

	public static long determineIntegerSum(Domain.Small... domains) {
		if (domains.length > 0) {
			long size = domains[0].size();
			for (int i = 1; i != domains.length; ++i) {
				size = size + domains[i].size();
				if (size < 0) {
					// NOTE: integer overflow
					throw new IllegalArgumentException("invalid integer product");
				}
			}
			return (int) size;
		} else {
			return 0;
		}
	}

	/**
	 * Determine whether the sum of the sizes of a set of zero or more domains fits
	 * in an integer or not.
	 *
	 * @param domains
	 * @return
	 */
	public static boolean hasIntegerSum(Domain.Small<?>... domains) {
		if(domains.length > 0) {
			long size = domains[0].size();
			for (int i = 1; i != domains.length; ++i) {
				size = size + domains[i].size();
				if (size < 0) {
					// NOTE: integer overflow
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Determine the integer product of one or more integer elements. If the
	 * resulting value won't fit into an integer, then an
	 * <code>IllegalArgumentException</code> is thrown.
	 *
	 * @param sizes
	 * @return
	 */
	public static boolean hasIntegerProduct(Domain.Small<?>... domains) {
		long size = domains[0].size();
		for (int i = 1; i != domains.length; ++i) {
			size = size * domains[i].size();
			if (size < 0) {
				// NOTE: integer overflow
				return false;
			}
		}
		return true;
	}


	/**
	 * Determine a given integer taken to a given power. If the resulting value
	 * won't fit into an integer, then an <code>IllegalArgumentException</code> is
	 * thrown.
	 *
	 * @param sizes
	 * @return
	 */
	private static long determineIntegerPower(long base, int power) {
		if(power == 0) {
			return 1;
		} else {
			long r = base;
			for (int i = 1; i < power; ++i) {
				r = r * base;
				if (r < 0) {
					// NOTE: integer overflow
					throw new IllegalArgumentException("invalid integer power");
				}
			}
			return r;
		}
	}

	/**
	 * Determine a given integer taken to a given power. If the resulting value
	 * won't fit into an integer, then an <code>IllegalArgumentException</code> is
	 * thrown.
	 *
	 * @param sizes
	 * @return
	 */
	public static boolean hasIntegerPower(long base, int power) {
		if (power == 0) {
			return true;
		} else {
			long r = base;
			for (int i = 1; i < power; ++i) {
				r = r * base;
				if (r < 0) {
					// NOTE: integer overflow
					return false;
				}
			}
			return true;
		}
	}
}
