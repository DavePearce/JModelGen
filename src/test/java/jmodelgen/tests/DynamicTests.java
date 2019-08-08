package jmodelgen.tests;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import jmodelgen.core.Domain;
import jmodelgen.core.Domains;
import jmodelgen.core.Walker;
import jmodelgen.core.Walker.State;
import jmodelgen.util.Walkers;


/**
 * Perform a range of tests on small domains. We use
 * <code>AbstractBigDomain</code> rather than going through the
 * <code>Domains</code> API to ensure we are checking what we are intending.
 *
 * @author David J. Pearce
 *
 */
public class DynamicTests {
	private static class IncreasingIntegers implements Walker.State<Integer> {
		private final int min;
		private final int max;

		public IncreasingIntegers(int min, int max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public Walker<Integer> construct() {
			return Walkers.Adaptor(Domains.Int(min, max));
		}

		@Override
		public State<Integer> transfer(Integer item) {
			return item <= min ? this : new IncreasingIntegers(item, max);
		}
	}

	@Test
	public void test_dynamic_01() {
		// NOTE: clearly the API could be improved
		Walker.State<Integer> seed = new IncreasingIntegers(0, 2);
		Walker<List<Integer>> walker = Walkers.Product(0, 4, seed, (items) -> items);
		Domain.Big<Integer[]> arrays = Domains.Array(0, 4, Domains.Int(0, 2));
		HashSet<ArrayList<Integer>> cache = new HashSet<>();
		for(Integer[] arr : arrays) {
			if(isSorted(arr)) {
				cache.add(new ArrayList<>(Arrays.asList(arr)));
			}
		}
		int count = 0;
		for(List<Integer> l : walker) {
			assertTrue(cache.contains(l));
			count ++;
		}
		assertEquals(cache.size(), count);
	}

	private static boolean isSorted(Integer[] arr) {
		for(int i=1;i<arr.length;++i) {
			if(arr[i-1] > arr[i]) {
				return false;
			}
		}
		return true;
	}
}
