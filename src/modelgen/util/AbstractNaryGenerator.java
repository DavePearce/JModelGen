package modelgen.util;

import java.util.ArrayList;
import java.util.List;

import modelgen.core.Domain;

/**
 * An abstract generator for values composed from an arbitrary number of
 * children. For example, if generating random logical expressions then the "or"
 * operator would extend this class.
 *
 * @author David J. Pearce
 *
 * @param <T>
 * @param <S>
 */
public abstract class AbstractNaryGenerator<T, S> implements Domain<T> {
	private final int max;
	private final Domain<S> generator;

	public AbstractNaryGenerator(int max, Domain<S> generator) {
		this.max = max;
		this.generator = generator;
	}

	@Override
	public long size() {
		long generator_size = generator.size();
		long size = 0;
		for (int i = 0; i <= max; ++i) {
			long delta = delta(generator_size, i);
			size = size + delta;
		}
		return size;
	}

	@Override
	public T get(long index) {
		ArrayList<S> items = new ArrayList<>();
		final long generator_size = generator.size();
		// Yes, this one is a tad complex
		for (int i = 0; i <= max; ++i) {
			long delta = delta(generator_size, i);
			if (index < delta) {
				for (int j = 0; j < i; ++j) {
					items.add(generator.get(index % generator_size));
					index = index / generator_size;
				}
				break;
			}
			index = index - delta;
		}
		return generate(items);
	}

	private static long delta(long base, int power) {
		if (power == 0) {
			// special case as only one empty list
			return 1;
		} else {
			long r = base;
			for (int i = 1; i < power; ++i) {
				r = r * base;
			}
			return r;
		}
	}

	public abstract T generate(List<S> items);
}
