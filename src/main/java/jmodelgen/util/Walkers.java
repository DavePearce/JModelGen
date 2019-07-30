package jmodelgen.util;

import java.util.Iterator;

import jmodelgen.core.Domain;
import jmodelgen.core.Walker;

public class Walkers {

	/**
	 * A simple adaptor from an arbitrary domain to a walker.
	 *
	 * @param <T>
	 * @param domain
	 * @return
	 */
	public static <T> Walker<T> from(Domain<T> domain) {
		return new AbstractWalker<T>() {
			private int index = 0;
			@Override
			public void reset() {
				index = 0;
			}

			@Override
			public boolean finished() {
				return index >= domain.size();
			}

			@Override
			public void next() {
				index = index + 1;
			}

			@Override
			public T get() {
				return domain.get(index);
			}

		};
	}
}
