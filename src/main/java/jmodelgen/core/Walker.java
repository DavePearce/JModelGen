package jmodelgen.core;

import java.util.Iterator;

public interface Walker<T> extends Iterable<T> {
	public void reset();

	public boolean finished();

	public T get();

	public void next();

	@Override
	public Iterator<T> iterator();
}
