package modelgen.core;

/**
 * <p>
 * Represents a finite domain of abstract values where each value is given a
 * unique index starting from zero. The key characteristics of any domain are
 * its size, and the ability to get any given element efficiently.
 * </p>
 * <p>
 * As an example, consider the domain of <i>integer arrays</i> for integers
 * between <code>0</code> and <code>1</code>. Values in this domain might be
 * ordered like so:
 * </p>
 *
 * <pre>
 * 0 = []
 * 1 = [0]
 * 2 = [1]
 * 3 = [0,0]
 * 4 = [1,0]
 * 5 = [0,1]
 * 6 = [1,1]
 * 7 = [0,0,0]
 * ...
 * </pre>
 *
 * Observe there are an infinite number of values here and, to create a finite
 * domain, we would need to place a cap on the number of elements.
 *
 * @author David J. Pearce
 *
 * @param <T>
 */
public interface Domain<T> {
	/**
	 * Determine how many objects this generator can produce
	 *
	 * @return
	 */
	public long size();

	/**
	 * Get a given value from the domain determined by its index.
	 *
	 * @param kind
	 * @return
	 */
	public T get(long index);

	/**
	 * A simple domain for boolean values.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Bool implements Domain<Boolean> {

		@Override
		public long size() {
			return 2;
		}

		@Override
		public Boolean get(long index) {
			return index == 0;
		}
	}

	/**
	 * A domain for integers based on a range which includes all values between a
	 * given lower bound and a given upper bound.
	 *
	 * @author David J. Pearce
	 *
	 */
	public class Int implements Domain<Integer> {
		private final int lower;
		private final int upper;

		public Int(int lower, int upper) {
			this.lower = lower;
			this.upper = upper;
		}

		@Override
		public long size() {
			return upper - lower + 1;
		}

		@Override
		public Integer get(long index) {
			return lower + (int) index;
		}
	}


	/**
	 * A domain constructed from a finite set of values. The size of the domain is
	 * thus exactly the number of elements in the set.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static class Finite<T> implements Domain<T> {
		private final T[] items;

		public Finite(T[] items) {
			this.items = items;
		}

		@Override
		public long size() {
			return items.length;
		}

		@Override
		public T get(long index) {
			return items[(int) index];
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
	public static class Union<T> implements Domain<T> {
		private final Domain<? extends T>[] subdomains;

		public Union(Domain<? extends T>... subdomains) {
			this.subdomains = subdomains;
		}

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
	}
}
