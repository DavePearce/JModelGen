package jmodelgen.tests;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jmodelgen.core.Domain;
import jmodelgen.core.Domains;
import jmodelgen.util.AbstractBigDomain;
import static jmodelgen.tests.TestDomains.*;
import static jmodelgen.core.Domains.BOOL;

/**
 * Perform a range of tests on small domains. We use
 * <code>AbstractBigDomain</code> rather than going through the
 * <code>Domains</code> API to ensure we are checking what we are intending.
 *
 * @author David J. Pearce
 *
 */
public class BigDomainTests {
	@Test
	public void test_adaptor_01() {
		// Convert words into their sizes
		Domain.Big<Integer> word_sizes = new AbstractBigDomain.Adaptor<Integer, String>(WORDS) {

			@Override
			public Integer get(String s) {
				return s.length();
			}

		};
		// Check sizes match
		assertEquals(word_sizes.bigSize(),BigInteger.valueOf(WORDS.size()));
		// Check elements match as expected using iterator()
		Iterator<Integer> iterator = word_sizes.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			assertEquals(WORDS.get(i).length(), (int) iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_binaryproduct_01() {
		// Convert words into their sizes
		Domain.Big<String> wordints = new AbstractBigDomain.BinaryProduct<String, String, Integer>(
				WORDS, INTS) {

			@Override
			public String get(String s, Integer i) {
				return s + "," + i;
			}

		};
		// Check sizes match
		assertEquals(wordints.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = wordints.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != WORDS.size(); ++j) {
				String s1 = WORDS.get(j) + "," + INTS.get(i);
				assertEquals(s1, iterator.next());
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_binaryproduct_02() {
		// Convert words into their sizes
		Domain.Big<String> intwords = new AbstractBigDomain.BinaryProduct<String, Integer, String>(INTS, WORDS) {

			@Override
			public String get(Integer i, String s) {
				return s + "," + i;
			}

		};
		// Check sizes match
		assertEquals(intwords.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = intwords.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				String s1 = WORDS.get(i) + "," + INTS.get(j);
				assertEquals(s1, iterator.next());
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_ternaryproduct_01() {
		// Convert words into their sizes
		Domain.Big<String> wordintbools = new AbstractBigDomain.TernaryProduct<String, String, Integer, Boolean>(WORDS,
				INTS, BOOL) {

			@Override
			public String get(String s, Integer i, Boolean b) {
				return s + "," + i + "," + b;
			}

		};
		// Check sizes match
		assertEquals(wordintbools.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()).multiply(BOOL.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = wordintbools.iterator();
		for (int i = 0; i != BOOL.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != WORDS.size(); ++k) {
					String s1 = WORDS.get(k) + "," + INTS.get(j) + "," + BOOL.get(i);
					assertEquals(s1, iterator.next());
				}
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_ternaryproduct_02() {
		// Convert words into their sizes
		Domain.Big<String> boolwordints = new AbstractBigDomain.TernaryProduct<String, Boolean, String, Integer>(BOOL,
				WORDS, INTS) {

			@Override
			public String get(Boolean b, String s, Integer i) {
				return s + "," + i + "," + b;
			}

		};
		// Check sizes match
		assertEquals(boolwordints.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()).multiply(BOOL.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = boolwordints.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != WORDS.size(); ++j) {
				for (int k = 0; k != BOOL.size(); ++k) {
					String s1 = WORDS.get(j) + "," + INTS.get(i) + "," + BOOL.get(k);
					assertEquals(s1, iterator.next());
				}
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_01() {

		Domain.Big<Integer> word_sizes = new AbstractBigDomain.NarySequence<Integer, String>(1, WORDS) {

			@Override
			public Integer generate(String[] items) {
				return items[0].length();
			}
		};
		// Check sizes match
		assertEquals(word_sizes.bigSize(), BigInteger.valueOf(WORDS.size()));
		// Check elements match as expected using iterator()
		Iterator<Integer> iterator = word_sizes.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			assertEquals(WORDS.get(i).length(), (int) iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_02() {
		// Convert words into their sizes
		Domain.Big<String> intints = new AbstractBigDomain.NarySequence<String, Integer>(2, INTS) {

			@Override
			public String generate(Integer[] items) {
				return items[0] + "," + items[1];
			}
		};

		// Check sizes match
		assertEquals(intints.bigSize(), INTS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = intints.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				String s1 = INTS.get(j) + "," + INTS.get(i);
				assertEquals(s1, iterator.next());
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_03() {
		// Convert words into their sizes
		Domain.Big<String> intintints = new AbstractBigDomain.NarySequence<String, Integer>(3, INTS) {

			@Override
			public String generate(Integer[] items) {
				return items[0] + "," + items[1] + "," + items[2];
			}
		};
		// Check sizes match
		assertEquals(intintints.bigSize(), INTS.bigSize().multiply(INTS.bigSize()).multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<String> iterator = intintints.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != INTS.size(); ++k) {
					String s1 = INTS.get(k) + "," + INTS.get(j) + "," + INTS.get(i);
					assertEquals(s1, iterator.next());
				}
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_narysum() {
		// Convert words into their sizes
		Domain.Big<Object> wordints = new AbstractBigDomain.NarySum<Object>(WORDS, INTS);
		// Check sizes match
		assertEquals(wordints.bigSize(), WORDS.bigSize().add(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<Object> iterator = wordints.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			assertEquals(WORDS.get(i), iterator.next());
		}
		for (int i = 0; i != INTS.size(); ++i) {
			assertEquals(INTS.get(i), iterator.next());
		}
	}
}
