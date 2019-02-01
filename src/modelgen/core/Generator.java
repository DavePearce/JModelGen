package modelgen.core;

import java.util.List;

public interface Generator<T> {
	/**
	 * Generate all descendents from a given seed value in a given domain.
	 *
	 * @param seed
	 * @param domain
	 * @return
	 */
	public List<T> generate(T seed);
}
