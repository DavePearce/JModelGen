package jmodelgen.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import jmodelgen.util.AbstractSmallDomain;

public interface Domain {

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
	public static interface Big<T> extends Iterable<T> {
		/**
		 * Determine how many objects this generator can produce
		 *
		 * @return
		 */
		public BigInteger bigSize();

		/**
		 * Get a given value from the domain determined by its index.
		 *
		 * @param kind
		 * @return
		 */
		public T get(BigInteger index);

		@Override
		public Iterator<T> iterator();
	}

	/**
	 * <p>
	 * Represents a finite domain of less than <code>2^32</code> abstract values
	 * where each value is given a unique index starting from zero. The key
	 * characteristics of any domain are its size, and the ability to get any given
	 * element efficiently.
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
	 */
	public static interface Small<T> extends Big<T> {
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
	}
}
