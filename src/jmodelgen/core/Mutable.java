package jmodelgen.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents an abstract item which has zero or more positions that can be
 * mutated.
 *
 * @author David J. Pearce
 *
 * @param <T>
 */
public interface Mutable<T> {

	/**
	 * Return the number of children to this mutable item.
	 *
	 * @return
	 */
	public int size();

	/**
	 * Get the ith child of this mutable item.
	 *
	 * @param i
	 * @return
	 */
	public T get(int i);

	/**
	 * Return copy of this mutable item with the given child replaced.
	 *
	 * @param i
	 * @param child
	 * @return
	 */
	public T replace(int i, T child);

	/**
	 * A mutable item which can additionally be extended
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public interface Extensible<T> extends Mutable<T> {

		/**
		 * Return copy of this tree with given child appended onto the end.
		 *
		 * @param child
		 * @return
		 */
		public T append(T child);
	}

	/**
	 * Implements a generic mutator which generates all possible single mutations
	 * for a given mutable item. For example, consider an array item
	 * <code>[1,2]</code>. There are three possible mutations for this. We can
	 * replace either of the elements with a new element from the domain; or we can
	 * append a new element.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static class LeftMutator<T extends Mutable<T>, S extends Domain<T>> implements Transformer<T> {
		private final S domain;
		private final BiFunction<T,S,S> transfer;

		public LeftMutator(S domain) {
			this.domain = domain;
			// If no transfer function provided, simply use identity
			this.transfer = new BiFunction<T,S,S>() {
				@Override
				public S apply(T i, S t) { return t; }
			};
		}

		public LeftMutator(S domain, BiFunction<T, S, S> transfer) {
			this.domain = domain;
			this.transfer = transfer;
		}

		@Override
		public List<T> transform(T item) {
			return transform(item, domain);
		}

		private List<T> transform(T item, S context) {
			if(item.size() > 0 || item instanceof Extensible) {
				ArrayList<T> extensions = new ArrayList<>();
				// First, generation all possible mutations
				for (int i = 0; i != item.size(); ++i) {
					// Generate all extensions of the ith child. This will only generate extensions
					// if the child is a block.
					List<T> children = transform(item.get(i), context);
					//
					for (int j = 0; j != children.size(); ++j) {
						extensions.add(item.replace(i, children.get(j)));
					}
					// Update context
					context = transfer.apply(item.get(i), context);
				}
				// Second, generate all possible extensions (if applicable)
				if (item instanceof Extensible) {
					Extensible<T> tree = (Extensible<T>) item;
					for (int i = 0; i != context.size(); ++i) {
						extensions.add(tree.append(context.get(i)));
					}
				}
				// Done
				return extensions;
			} else {
				return Collections.EMPTY_LIST;
			}
		}
	}
}
