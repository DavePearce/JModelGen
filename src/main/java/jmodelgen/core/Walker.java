package jmodelgen.core;

import java.util.Iterator;

public interface Walker<T> extends Iterable<T> {
	public void reset();

	public boolean finished();

	public T get();

	public void next();

	@Override
	public Iterator<T> iterator();

	/**
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static interface State<T> {
		public Walker<T> construct();

		public State<T> transfer(T item);
	}
}
