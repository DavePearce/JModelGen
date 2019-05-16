package jmodelgen.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import jmodelgen.util.Pair;

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
	 * Provides a mechanism for manipulating the domain of a mutator based on the
	 * sequence of children.
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 * @param <S>
	 */
	public interface Transfer<T extends Mutable<T>, S extends Domain<T>> {
		public S enter(T item, S context);

		public S leave(T item, S context);
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
		private final int maxWidth;
		private final Transfer<T, S> transfer;

		public LeftMutator(S domain, int maxWidth) {
			this.domain = domain;
			this.maxWidth = maxWidth;
			// If no transfer function provided, simply use identity
			this.transfer = new Transfer<T, S>() {
				@Override
				public S leave(T i, S t) {
					return t;
				}

				@Override
				public S enter(T i, S t) {
					return t;
				}
			};
		}

		public LeftMutator(S domain, Transfer<T, S> transfer, int maxWidth) {
			this.domain = domain;
			this.transfer = transfer;
			this.maxWidth = maxWidth;
		}

		@Override
		public List<T> transform(T item) {
			return transform(item, domain).first();
		}

		private Pair<List<T>,S> transform(T item, S context) {
			List<T> extensions = Collections.EMPTY_LIST;
			// Register the entering of this mutable
			context = transfer.enter(item, context);
			//
			if (item.size() > 0 || item instanceof Extensible) {
				//
				extensions = new ArrayList<>();
				// First, generation all possible mutations
				for (int i = 0; i != item.size(); ++i) {
					// Generate all extensions of the ith child. This will only generate extensions
					// if the child is a block.
					Pair<List<T>,S> p = transform(item.get(i), context);
					List<T> children = p.first();
					context = p.second();
					//
					for (int j = 0; j != children.size(); ++j) {
						extensions.add(item.replace(i, children.get(j)));
					}
				}
				// Second, generate all possible extensions (if applicable)
				if (item instanceof Extensible && item.size() < maxWidth) {
					Extensible<T> tree = (Extensible<T>) item;
					for (int i = 0; i != context.size(); ++i) {
						extensions.add(tree.append(context.get(i)));
					}
				}
				// Done
			}
			// Register the leaving of this mutable
			context = transfer.leave(item, context);
			//
			return new Pair<>(extensions, context);
		}
	}
}
