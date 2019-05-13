package jmodelgen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import jmodelgen.core.Domain;

public class Domains {
	private static final Random random = new Random(System.currentTimeMillis());

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
				return upper - lower + 1;
			}

			@Override
			public Integer get(long index) {
				return lower + (int) index;
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
		return new AbstractDomain<T>() {
			@Override
			public long size() {
				long sum = 0;
				for (int i = 0; i != subdomains.length; ++i) {
					sum = sum + subdomains[i].size();
				}
				return sum;
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
		};
	}

	/**
	 * A domain construct from the cartesian product of a given set of fields. The
	 * size of the domain is the product of all fields.
	 *
	 * @param fields
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
			public Domain<T[]> slice(long start, long end) {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	private static <T> long size(Domain<T>[] fields) {
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
		};
	}

	public static void main(String[] args) {
		Domain<Object[]> d = Product(new Domain[] { Domains.Int(0, 2), Domains.Bool(), Domains.Int(0, 1) });
		for (int i = 0; i != d.size(); ++i) {
			System.out.println("GOT: " + Arrays.toString(d.get(i)));
		}
	}
}
