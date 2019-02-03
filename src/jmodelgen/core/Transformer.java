package jmodelgen.core;

import java.util.List;

public interface Transformer<T> {

	/**
	 * Transform a given item into zero or more items which are modified in some
	 * way. For example, if the item was an array then we might return the list of
	 * its "one place extensions" --- that is, all instances of the array appended
	 * with a new element.
	 *
	 * @param item
	 * @param context
	 * @return
	 */
	List<T> transform(T item);
}
