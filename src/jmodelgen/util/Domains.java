package jmodelgen.util;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
	 * Return a domain which generates lists with between zero and <code>n</code>
	 * elements selected from another domain.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static <T> Domain<java.util.List<T>> List(int n, Domain<T> generator) {
		return new AbstractDomain.Nary<java.util.List<T>, T>(n,generator) {
			@Override
			public java.util.List<T> generate(java.util.List<T> items) {
				return items;
			}
		};
	}

	/**
	 * Generators
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
}
