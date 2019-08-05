package jmodelgen.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;

import jmodelgen.util.AbstractBigDomain;
import jmodelgen.util.AbstractSmallDomain;
import jmodelgen.util.AbstractSmallDomain.BinaryProduct;
import jmodelgen.util.AbstractSmallDomain.NaryProduct;

public class Domains {
	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * A simple constant representing the empty domain.
	 */
	public static final Domain.Static EMPTY = new AbstractSmallDomain() {

		@Override
		public int size() {
			return 0;
		}

		@Override
		public Object get(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public BigInteger bigSize() {
			return BigInteger.ZERO;
		}

		@Override
		public Object get(BigInteger index) {
			throw new UnsupportedOperationException();
		}

	};

	/**
	 * Constraint a domain which contains exactly <code>n</code> elements of a
	 * domain chosen uniformly at random according to Knuth's algorithm S. The key
	 * advantage of this algorithm is that it is guaranteed to sample <code>m</code>
	 * unique elements uniformly at random. The disadvantage, however, is that its
	 * execution time is linear in <code>domain.size()</code>. Hence, for large
	 * domains, this algorithm can be expensive.
	 *
	 * @param domain The domain being sampled over.
	 * @param m      The number of samples to record.
	 */
	public static <T> Domain.Small<T> Sample(Domain.Static<T> domain, int m) {
		if (m < 0) {
			throw new IllegalArgumentException("negative sample size");
		} else if (domain instanceof Domain.Small) {
			Domain.Small<T> small = (Domain.Small<T>) domain;
			int[] samples = generateSamples(small.size(), m);
			return new AbstractSmallDomain.Sample<>(small, samples);
		} else {
			BigInteger[] samples = generateSamples(domain.bigSize(), m);
			return new AbstractBigDomain.Sample<>(domain, samples);
		}
	}

	/**
	 * Generate m sample indices from n potential elements.
	 *
	 * @param n
	 * @param m
	 * @return
	 */
	private static int[] generateSamples(int n, int m) {
		int k = n < m ? n : m;
		// Guaranteed to be an integer
		int[] samples = new int[k];
		// Do the sampling using Algorithm S
		int j = 0;
		for (int i = 0; i != n; ++i) {
			int s = random.nextInt(n - i);
			if (s < k) {
				samples[j++] = i;
				k = k - 1;
			}
		}
		return samples;
	}

	private static BigInteger[] generateSamples(BigInteger n, int m) {
		throw new UnsupportedOperationException("implement me");
	}

	/**
	 * This is a fast approximate sampling algorithm. Being approximate means that
	 * it's not guaranteed to generate unique samples. Specifically, there is a
	 * chance duplicate samples will be recorded (though as the domain size
	 * increases, this becomes increasingly unlikely). The advantage of this
	 * algorithm is that it is linear in <code>m</code> rather than
	 * <code>domain.size()</code>. For large domains, this makes it considerably
	 * faster.
	 *
	 * @param <T>
	 * @param domain The domain being sampled over.
	 * @param m      The number of samples to record.
	 * @return
	 */
	public static <T> Domain.Small<T> FastApproximateSample(Domain.Static<T> domain, int m) {
		if (m < 0) {
			throw new IllegalArgumentException("negative sample size");
		} else if (domain instanceof Domain.Small) {
			Domain.Small<T> small = (Domain.Small<T>) domain;
			int[] samples = generateApproximateSamples(small.size(), m);
			return new AbstractSmallDomain.Sample<>(small, samples);
		} else {
			BigInteger[] samples = generateApproximateSamples(domain.bigSize(), m);
			return new AbstractBigDomain.Sample<>(domain, samples);
		}
	}

	/**
	 * Generate m sample indices from n potential elements. This algorithm is linear
	 * in the <code>m</code>.
	 *
	 * @param n
	 * @param m
	 * @return
	 */
	private static int[] generateApproximateSamples(int n, int m) {
		int k = n < m ? n : m;
		// Guaranteed to be an integer
		int[] samples = new int[k];
		// Do the fast sampling
		for (int i = 0; i != m; ++i) {
			samples[i] = random.nextInt(n);
		}
		// Done
		return samples;
	}

	private static BigInteger[] generateApproximateSamples(BigInteger n, int m) {
		throw new UnsupportedOperationException("implement me");
	}

	/**
	 * A simple domain for boolean values.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static Domain.Small<Boolean> Bool() {
		return new AbstractSmallDomain<Boolean>() {
			@Override
			public int size() {
				return 2;
			}

			@Override
			public Boolean get(int index) {
				return index == 0;
			}
		};
	}

	/**
	 * A domain for integers based on a range which includes all values between a
	 * given lower bound and a given upper bound (inclusive).
	 *
	 * @param lower The lower bound of integers to generate
	 * @param upper The upper bound of integers to generate
	 * @author David J. Pearce
	 *
	 */
	public static Domain.Small<Integer> Int(final int lower, final int upper) {
		if(upper < lower) {
			throw new IllegalArgumentException("negative domain size");
		} else {
			return new AbstractSmallDomain<Integer>() {
				@Override
				public int size() {
					return upper - lower + 1;
				}

				@Override
				public Integer get(int index) {
					return lower + index;
				}
			};
		}
	}

	/**
	 * Return a domain which generates lists whose length is between a given minimum
	 * and maximum value.
	 *
	 * @author David J. Pearce
	 *
	 */
	@SuppressWarnings("unchecked")
	public static <T> Domain.Static<T[]> Array(int min, T[] element, Domain.Static<T> generator) {
		final int max = element.length;
		//
		if (min == max) {
			return Array(element, generator);
		} else {
			Domain.Static<T[]>[] statics = new Domain.Static[max - min];
			for (int i = 0; i != statics.length; ++i) {
				statics[i] = Array(Arrays.copyOf(element, i + min), generator);
			}
			return Domains.<T[]>Union(statics);
		}
	}

	public static <T> Domain.Static<T[]> Array(T[] element, Domain.Static<T> generator) {
		// FIXME: this is totally broken!
		if(generator instanceof Domain.Small) {
			Domain.Small<T> small = (Domain.Small<T>) generator;
			if (AbstractSmallDomain.hasIntegerPower(small.size(), element.length)) {
				return new AbstractSmallDomain.NaryProduct<T[], T>(element, small) {
					@Override
					public T[] generate(T[] element) {
						// FIXME: can we avoid this somehow?
						return Arrays.copyOf(element, element.length);
					}
				};
			}
		}
		// default to big domain
		return new AbstractBigDomain.NaryProduct<T[], T>(element, generator) {
			@Override
			public T[] generate(T[] element) {
				// FIXME: can we avoid this somehow?
				return Arrays.copyOf(element, element.length);
			}
		};
	}

	/**
	 * A domain constructed from the union of one or more other domain. The size of
	 * this domain is thus sum of the size of all included domains.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public static <T> Domain.Static<T> Union(final Domain.Static<? extends T>... subdomains) {
		if(subdomains.length == 1) {
			// Easy case
			return (Domain.Static<T>) subdomains[0];
		}
		Domain.Small[] smallDomains = toSmallDomains(subdomains);
		if (smallDomains != null && AbstractSmallDomain.hasIntegerSum(smallDomains)) {
			return new AbstractSmallDomain.NarySum<T>(smallDomains);
		} else {
			return new AbstractBigDomain.NarySum<T>(subdomains);
		}
	}

	/**
	 * A domain constructed from the union of one or more other domain. The size of
	 * this domain is thus sum of the size of all included domains.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public static <T> Domain.Static<T> Union(final Domain.Small<? extends T>... subdomains) {
		if (subdomains.length == 1) {
			// Easy case
			return (Domain.Static<T>) subdomains[0];
		} else if (AbstractSmallDomain.hasIntegerSum(subdomains)) {
			return new AbstractSmallDomain.NarySum<T>(subdomains);
		} else {
			return new AbstractBigDomain.NarySum<T>(subdomains);
		}
	}

	/**
	 * Construct the product of two domains. For example, consider the domain of
	 * ints <code>0..1</code> and the domain of booleans. Then, the product domain
	 * has the following elements:
	 *
	 * <pre>
	 * 0,true
	 * 0,false
	 * 1,true
	 * 1,false
	 * </pre>
	 *
	 * The supplied mapping function is responsible for turning a given combination
	 * of elements from each domain into a single element in the resulting domain.
	 */
	public static <T, L, R> Domain.Static<T> Product(final Domain.Static<L> left, Domain.Static<R> right,
			BiFunction<L, R, T> mapping) {
		// Attempt to construct a small domain for efficiency reasons.
		if (left instanceof Domain.Small && right instanceof Domain.Small) {
			Domain.Small<L> l = (Domain.Small<L>) left;
			Domain.Small<R> r = (Domain.Small<R>) right;
			// Both left and right are small, but is their combination still small?
			if (AbstractSmallDomain.hasIntegerProduct(l.size(), r.size())) {
				// Yes, it is!
				return new AbstractSmallDomain.BinaryProduct<T, L, R>((Domain.Small<L>) left, (Domain.Small<R>) right) {

					@Override
					public T get(L left, R right) {
						return mapping.apply(left, right);
					}

				};
			}
		}
		// Big domain required
		return new AbstractBigDomain.BinaryProduct<T, L, R>(left, right) {
			@Override
			public T get(L left, R right) {
				return mapping.apply(left, right);
			}
		};
	}

	/**
	 * A domain construct from the cartesian product of a given set of fields. The
	 * size of the domain is the product of all fields.
	 *
	 * @param fields The subdomains for each field of the product.
	 * @param dummy  This can be left empty (it is used to aid creating generated
	 *               areas).
	 * @return
	 */
	public static <T> Domain.Static<T[]> Product(final Domain.Static<? extends T>[] fields, T... dummy) {
		throw new IllegalArgumentException("implement me");
	}

	/**
	 * A domain constructed from a finite set of values. The size of the domain is
	 * thus exactly the number of elements in the set.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static <T> Domain.Small<T> Finite(T... items) {
		return new AbstractSmallDomain<T>() {
			@Override
			public int size() {
				return items.length;
			}

			@Override
			public T get(int index) {
				return items[(int) index];
			}
		};
	}

	private static Domain.Small[] toSmallDomains(Domain.Static... domains) {
		Domain.Small[] smalls = new Domain.Small[domains.length];
		for (int i = 0; i != domains.length; ++i) {
			Domain.Static s = domains[i];
			if (s instanceof Domain.Small) {
				smalls[i] = (Domain.Small) s;
			} else {
				return null;
			}
		}
		return smalls;
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Domain.Static<Integer> is = Domains.Int(0, 5000);
		Domain.Small<Integer> s1 = Domains.Sample(is, 20);
		long split = System.currentTimeMillis();
		Domain.Small<Integer> s2 = Domains.FastApproximateSample(is, 20);
		long end = System.currentTimeMillis();
		System.out.println("(" + (split-start) + "ms)" + s1);
		System.out.println("(" + (end-split) + "ms)" + s2);
	}
}
