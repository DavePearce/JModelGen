package jmodelgen.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;

import jmodelgen.core.Domain;

public class Domains {
	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * A simple constant representing the empty domain.
	 */
	public static final Domain EMPTY = new Domain() {

		@Override
		public long size() {
			return 0;
		}

		@Override
		public Object get(long index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Domain slice(long start, long end) {
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
	 * Apply a given lambda consumer to exactly <code>n</code> elements of a domain
	 * chosen uniformly at random according to Knuth's algorithm S.
	 *
	 * @param domain
	 * @param n
	 * @param consumer
	 */
	public static <T> void apply(Domain<T> domain, long n, Consumer<T> consumer) {
		// FIXME: this is a problem which needs to be fixed!
		final int size = (int) domain.size();
		//
		for(int i=0;i!=size;++i) {
			int s = random.nextInt(size - i);
			if(s < n) {
				consumer.accept(domain.get(i));
				n = n - 1;
			}
		}
	}

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
	public static <T> Domain<T> Sample(Domain<T> domain, int m) {
		if(m < 0) {
			throw new IllegalArgumentException("Sample size cannot be negative");
		}
		// NOTE: check whether domain smaller than n since this will limit overall size
		// of sampled domain. This is tricky because domain size is a long.
		final long n = domain.size();
		long k = m;
		// Compute the min
		if(n < Integer.MAX_VALUE && n < k) {
			// Domain size smaller than n.
			k = (int) n;
		}
		// Guaranteed to be an integer
		long[] samples = new long[(int) k];
		// Do the sampling using Algorithm S
		int j = 0;
		for (long i = 0; i != n; ++i) {
			long s = random.nextLong(n - i);
			if (s < k) {
				samples[j++] = i;
				k = k - 1;
			}
		}
		// Done
		return new Domain<T>() {

			@Override
			public long size() {
				return samples.length;
			}

			@Override
			public T get(long index) {
				return domain.get(samples[(int) index]);
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
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				String r="";
				for(int i=0;i<samples.length;++i) {
					if (i != 0) {
						r += ",";
					}
					r +=domain.get(samples[i]);
				}
				return "{" + r + "}";
			}
		};
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
	public static <T> Domain<T> FastApproximateSample(Domain<T> domain, int m) {
		final long n = domain.size();
		long k = m;
		// Compute the min
		if (n <= Integer.MAX_VALUE && n < k) {
			// Domain size smaller than n.
			k = (int) n;
		}
		// Guaranteed to be an integer
		long[] samples = new long[(int) k];
		//
		for (int i = 0; i != m; ++i) {
			samples[i] = random.nextLong(n);
		}
		// Done
		return new Domain<T>() {

			@Override
			public long size() {
				return samples.length;
			}

			@Override
			public T get(long index) {
				return domain.get(samples[(int) index]);
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
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				String r="";
				for(int i=0;i<samples.length;++i) {
					if (i != 0) {
						r += ",";
					}
					r +=domain.get(samples[i]);
				}
				return "{" + r + "}";
			}
		};
	}

	/**
	 * Apply a given lambda consumer to all elements matching a given condition of a
	 * domain.
	 *
	 * @param domain
	 * @param n
	 * @param consumer
	 */
	public static <T> void apply(Domain<T> domain, Predicate<T> condition, Consumer<T> consumer) {
		// FIXME: this is a problem which needs to be fixed!
		final int size = (int) domain.size();
		//
		for(int i=0;i!=size;++i) {
			T item = domain.get(i);
			if(condition.test(item)) {
				consumer.accept(item);
			}
		}
	}


	/**
	 * A simple domain for boolean values.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static Domain<Boolean> Bool() {
		return new AbstractDomain<Boolean>() {
			@Override
			public long size() {
				return 2;
			}

			@Override
			public Boolean get(long index) {
				return index == 0;
			}

			@Override
			public BigInteger bigSize() {
				return BigInteger.valueOf(2);
			}

			@Override
			public Boolean get(BigInteger index) {
				return get(index.longValue());
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
	public static Domain<Integer> Int(final int lower, final int upper) {
		return new Domain<Integer>() {
			@Override
			public long size() {
				return ((long)upper) - lower + 1;
			}

			@Override
			public Integer get(long index) {
				return lower + (int) index;
			}

			@Override
			public BigInteger bigSize() {
				// NOTE: this is safe whilst the range of integers remains 32bits.
				return BigInteger.valueOf(size());
			}

			@Override
			public Integer get(BigInteger index) {
				return get(index.longValue());
			}


			@Override
			public Domain<Integer> slice(long start, long end) {
				long size = size();
				if(start < 0 || start > size) {
					throw new IllegalArgumentException("invalid start");
				} else if(end < start || end > size) {
					throw new IllegalArgumentException("invalid end");
				}
				return Int((int) start, (int) end);
			}

			@Override
			public String toString() {
				String r ="";
				for(int i=lower;i<=upper;++i) {
					if(i!=lower) {
						r +=",";
					}
					r += Integer.toString(i);
				}
				return "{"+r+"}";
			}
		};
	}

	/**
	 * Return a domain which generates lists whose length is between a given minimum
	 * and maximum value.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static <T> Domain<java.util.List<T>> List(int min, int max, Domain<T> generator) {
		return new AbstractDomain.Nary<java.util.List<T>, T>(min,max,generator) {
			@Override
			public java.util.List<T> generate(java.util.List<T> items) {
				return items;
			}
		};
	}

	/**
	 * Map every element in a given domain of lists to a corresponding domain of
	 * lists where each element has a given item appended onto the end. For example,
	 * when appending <code>1</code> to the domain of integer lists gives the
	 * following mappings.
	 *
	 * <pre>
	 *  [] => [1]
	 *  [0] => [0,1]
	 *  [0,1] => [0,1,1]
	 * </pre>
	 *
	 * @param domain
	 * @param item
	 * @return
	 */
	public static <T> Domain<List<T>> append(Domain<List<T>> domain, T item) {
		return new Domain<List<T>>() {

			@Override
			public long size() {
				return domain.size();
			}

			@Override
			public List<T> get(long index) {
				List<T> l = domain.get(index);
				l.add(item);
				return l;
			}

			@Override
			public BigInteger bigSize() {
				return domain.bigSize();
			}

			@Override
			public List<T> get(BigInteger index) {
				List<T> l = domain.get(index);
				l.add(item);
				return l;
			}

			@Override
			public Domain<List<T>> slice(long start, long end) {
				// FIXME: to be implemented
				throw new UnsupportedOperationException();
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
	public static <T> Domain<T> Union(final Domain<? extends T>... subdomains) {
		// Long size calculation
		long _sum = 0;
		for (int i = 0; i != subdomains.length; ++i) {
			_sum = _sum + subdomains[i].size();
		}
		final long sum = _sum;
		// BigInteger size calculation
		BigInteger _bigsum = BigInteger.ZERO;
		for (int i = 0; i != subdomains.length; ++i) {
			_bigsum = _bigsum.add(subdomains[i].bigSize());
		}
		final BigInteger bigsum = _bigsum;
		//
		return new AbstractDomain<T>() {
			@Override
			public long size() {
				return sum;
			}

			@Override
			public BigInteger bigSize() {
				return bigsum;
			}

			@Override
			public T get(long index) {
				long sum = 0;
				for (int i = 0; i != subdomains.length; ++i) {
					Domain<? extends T> ith = subdomains[i];
					long size = ith.size();
					if (index < (sum + size)) {
						return ith.get(index - sum);
					}
					sum = sum + size;
				}
				throw new IllegalArgumentException("invalid index");
			}

			@Override
			public T get(BigInteger index) {
				throw new UnsupportedOperationException();
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
	public static <T> Domain<T[]> Product(final Domain<? extends T>[] fields, T... dummy) {
		final long size = size(fields);
		return new Domain<T[]>() {

			@Override
			public long size() {
				return size;
			}

			@Override
			public T[] get(long index) {
				T[] result = Arrays.copyOf(dummy, fields.length);
				//
				for(int i=0;i!=result.length;++i) {
					Domain<? extends T> field = fields[i];
					long size = field.size();
					result[i] = field.get(index % size);
					index = index / size;
				}
				//
				return result;
			}

			@Override
			public BigInteger bigSize() {
				throw new UnsupportedOperationException();
			}

			@Override
			public T[] get(BigInteger index) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Domain<T[]> slice(long start, long end) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static long size(Domain<?>[] fields) {
		long size = 1;
		for(int i=0;i!=fields.length;++i) {
			size = size * fields[i].size();
		}
		return size;
	}

	/**
	 * A domain constructed from a finite set of values. The size of the domain is
	 * thus exactly the number of elements in the set.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static <T> Domain<T> Finite(T... items) {
		return new Domain<T>() {
			@Override
			public long size() {
				return items.length;
			}

			@Override
			public T get(long index) {
				return items[(int) index];
			}

			@Override
			public BigInteger bigSize() {
				return BigInteger.valueOf(items.length);
			}

			@Override
			public T get(BigInteger index) {
				return items[index.intValue()];
			}

			@Override
			public Domain<T> slice(long start, long end) {
				if (start < 0 || start > items.length) {
					throw new IllegalArgumentException("invalid start");
				} else if (end < start || end > items.length) {
					throw new IllegalArgumentException("invalid end");
				}
				return Finite(Arrays.copyOfRange(items, (int) start, (int) end));
			}
		};
	}

	/**
	 * A domain constructed from all values in a given domain meeting a given
	 * constraint. This necessarily involves enumerating all elements of the
	 * original domain in order to determine how many match predicate. Having done
	 * that, only a single array of indices is retained which ensures constant time
	 * lookups for all matching elements.
	 *
	 * @param domain
	 * @param constraint
	 * @return
	 */
	public static <T> Domain<T> Constrained(Domain<T> domain, Predicate<T> constraint) {
		// First identify matching items
		ArrayList<Long> tmp = new ArrayList<>();
		for(long i=0;i!=domain.size();++i) {
			T item = domain.get(i);
			if(constraint.test(item)) {
				tmp.add(i);
			}
		}
		// Stash indices
		final long[] indices = new long[tmp.size()];
		for(int i=0;i!=tmp.size();++i) {
			indices[i] = tmp.get(i);
		}
		// Done
		return new AbstractDomain<T>() {
			@Override
			public long size() {
				return indices.length;
			}

			@Override
			public T get(long index) {
				return domain.get(indices[(int) index]);
			}


			@Override
			public BigInteger bigSize() {
				throw new UnsupportedOperationException();
			}

			@Override
			public T get(BigInteger index) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * An experimental domain constructed from all values in a given domain meeting
	 * a given constraint. This necessarily involves enumerating all elements of the
	 * original domain in order to determine how many match predicate. Having done
	 * that, only a single array of indices is retained which ensures constant time
	 * lookups for all matching elements. The key distinction of this domain is that
	 * it is constructed using parallel streams.
	 *
	 * @param domain
	 * @param constraint
	 * @return
	 */
	public static <T> Domain<T> ParallelConstrained(Domain<T> domain, Predicate<T> constraint) {
		long[] indices = LongStream.range(0, domain.size()).map(l -> constraint.test(domain.get(l)) ? l : -1).filter(l -> l >= 0).parallel().toArray();
		// Done
		return new AbstractDomain<T>() {
			@Override
			public long size() {
				return indices.length;
			}

			@Override
			public T get(long index) {
				return domain.get(indices[(int) index]);
			}

			@Override
			public BigInteger bigSize() {
				throw new UnsupportedOperationException();
			}

			@Override
			public T get(BigInteger index) {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Provides a simple mechanism for adapting one domain for another return type.
	 * This requires a conversion function which can take an arbitrary element of
	 * the source domain and convert it into an element of the target domain.
	 *
	 * @param <S>
	 * @param <T>
	 * @param domain
	 * @param functor
	 * @return
	 */
	public static abstract class Adaptor<S,T> implements Domain<T> {
		protected final Domain<S> domain;

		public Adaptor(Domain<S> domain) {
			this.domain = domain;
		}
		@Override
		public long size() {
			return domain.size();
		}

		@Override
		public T get(long index) {
			return get(domain.get(index));
		}

		/**
		 * Convert an arbitrary item in the source domain into its corresponding item in
		 * the target domain.
		 *
		 * @param item
		 * @return
		 */
		protected abstract T get(S item);

		@Override
		public Domain<T> slice(long start, long end) {
			// FIXME: would be good to support this at some point.
			throw new UnsupportedOperationException();
		}
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Domain<Integer> is = Domains.Int(0, 500000000);
		Domain<Integer> s1 = Domains.Sample(is, 20);
		long split = System.currentTimeMillis();
		Domain<Integer> s2 = Domains.FastApproximateSample(is, 20);
		long end = System.currentTimeMillis();
		System.out.println("(" + (split-start) + "ms)" + s1);
		System.out.println("(" + (end-split) + "ms)" + s2);
	}
}
