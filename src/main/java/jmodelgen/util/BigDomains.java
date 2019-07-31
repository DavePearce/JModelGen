package jmodelgen.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

import jmodelgen.core.BigDomain;
import jmodelgen.core.Domain;
import jmodelgen.core.Walker;

public class BigDomains {
	private static final ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * A simple constant representing the empty domain.
	 */
	public static final BigDomain EMPTY = new AbstractBigDomain() {

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
	 * A simple domain for boolean values.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static BigDomain<Boolean> Bool() {
		return new AbstractBigDomain<Boolean>() {
			@Override
			public BigInteger bigSize() {
				return BigInteger.valueOf(2);
			}

			@Override
			public Boolean get(BigInteger index) {
				return index.equals(BigInteger.ZERO);
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
	public static BigDomain<Integer> Int(final int lower, final int upper) {
		return new AbstractBigDomain<Integer>() {
			@Override
			public BigInteger bigSize() {
				return BigInteger.valueOf(((long) upper) - lower + 1);
			}

			@Override
			public Integer get(BigInteger index) {
				return lower + index.intValue();
			}

			@Override
			public BigDomain<Integer> slice(BigInteger start, BigInteger end) {
				return slice(start.intValue(),end.intValue());
			}

			private BigDomain<Integer> slice(int start, int end) {
				long size = bigSize().longValue();
				if (start < 0 || start > size) {
					throw new IllegalArgumentException("invalid start");
				} else if (end < start || end > size) {
					throw new IllegalArgumentException("invalid end");
				}
				return Int(start, end);
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
	public static <T> BigDomain<java.util.List<T>> List(int min, int max, BigDomain<T> generator) {
		return new AbstractBigDomain.Nary<java.util.List<T>, T>(min,max,generator) {
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
	public static <T> BigDomain<List<T>> append(BigDomain<List<T>> domain, T item) {
		return new AbstractBigDomain<List<T>>() {

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
	public static <T> BigDomain<T> Union(final BigDomain<? extends T>... subdomains) {
		// Long size calculation
		BigInteger _sum = BigInteger.ZERO;
		for (int i = 0; i != subdomains.length; ++i) {
			_sum = _sum.add(subdomains[i].bigSize());
		}
		final BigInteger sum = _sum;
		//
		return new AbstractBigDomain<T>() {
			@Override
			public BigInteger bigSize() {
				return sum;
			}

			@Override
			public T get(BigInteger index) {
				BigInteger sum = BigInteger.ZERO;
				for (int i = 0; i != subdomains.length; ++i) {
					BigDomain<? extends T> ith = subdomains[i];
					BigInteger _sum = sum.add(ith.bigSize());
					if (index.compareTo(_sum) < 0) {
						return ith.get(index.subtract(sum));
					}
					sum = _sum;
				}
				throw new IllegalArgumentException("invalid index");
			}

			@Override
			public String toString() {
				String r = "";
				for(int i=0;i!=subdomains.length;++i) {
					if(i != 0) {
						r += "+";
					}
					r += subdomains[i];
				}
				return r;
			}
		};
	}


	public static <T, L, R> BigDomain<T> Product(final BigDomain<L> left, BigDomain<R> right, BiFunction<L, R, T> mapping) {
		return new AbstractBigDomain.Binary<T, L, R>(left, right) {

			@Override
			public T get(L left, R right) {
				return mapping.apply(left, right);
			}

		};
	}

	public static <T, S> BigDomain<T> Product(int min, int max, final BigDomain<S> domain,
			Function<List<S>, T> mapping) {
		return new AbstractBigDomain.Nary<T, S>(min, max, domain) {
			@Override
			public T generate(List<S> items) {
				return mapping.apply(items);
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
	public static <T> BigDomain<T[]> Product(final BigDomain<? extends T>[] fields, T... dummy) {
		final BigInteger size = size(fields);
		//
		return new AbstractBigDomain<T[]>() {

			@Override
			public BigInteger bigSize() {
				return size;
			}

			@Override
			public T[] get(BigInteger index) {
				T[] result = Arrays.copyOf(dummy, fields.length);
				//
				for(int i=0;i!=result.length;++i) {
					BigDomain<? extends T> field = fields[i];
					BigInteger size = field.bigSize();
					result[i] = field.get(index.remainder(size));
					index = index.divide(size);
				}
				//
				return result;
			}
		};
	}

	private static BigInteger size(BigDomain<?>[] fields) {
		BigInteger size = BigInteger.ONE;
		for(int i=0;i!=fields.length;++i) {
			size = size.multiply(fields[i].bigSize());
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
	public static <T> BigDomain<T> Finite(T... items) {
		return new AbstractBigDomain<T>() {
			@Override
			public BigInteger bigSize() {
				return BigInteger.valueOf(items.length);
			}

			@Override
			public T get(BigInteger index) {
				return items[index.intValue()];
			}

			@Override
			public BigDomain<T> slice(BigInteger start, BigInteger end) {
				return slice(start.intValue(),end.intValue());
			}
			private BigDomain<T> slice(int start, int end) {
				if (start < 0 || start > items.length) {
					throw new IllegalArgumentException("invalid start");
				} else if (end < start || end > items.length) {
					throw new IllegalArgumentException("invalid end");
				}
				return Finite(Arrays.copyOfRange(items, start, end));
			}
		};
	}

	public static <T, S> BigDomain<T> Adaptor(BigDomain<S> domain, Function<S, T> mapping) {
		return new AbstractBigDomain.Unary<T, S>(domain) {

			@Override
			public T get(S s) {
				return mapping.apply(s);
			}

		};
	}

}
