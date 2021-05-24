// Copyright 2019 David J. Pearce
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package jmodelgen.tests;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jmodelgen.core.Domain;
import jmodelgen.core.Domains;
import jmodelgen.util.AbstractSmallDomain;
import static jmodelgen.tests.TestDomains.*;
import static jmodelgen.core.Domains.BOOL;

/**
 * Perform a range of tests on small domains. We use
 * <code>AbstractSmallDomain</code> rather than going through the
 * <code>Domains</code> API to ensure we are checking what we are intending.
 *
 * @author David J. Pearce
 *
 */
public class SmallDomainTests {
	@Test
	public void test_adaptor_01() {
		// Convert words into their sizes
		Domain.Small<Integer> word_sizes = new AbstractSmallDomain.Adaptor<Integer, String>(WORDS) {

			@Override
			public Integer get(String s) {
				return s.length();
			}

		};
		// Check sizes match
		assertEquals(word_sizes.size(),WORDS.size());
		assertEquals(word_sizes.bigSize(),BigInteger.valueOf(WORDS.size()));
		// Check elements match as expected using get()
		for (int i = 0; i != WORDS.size(); ++i) {
			assertEquals(WORDS.get(i).length(), (int) word_sizes.get(i));
		}
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
		Domain.Small<String> wordints = new AbstractSmallDomain.BinaryProduct<String, String, Integer>(
				WORDS, INTS) {

			@Override
			public String get(String s, Integer i) {
				return s + "," + i;
			}

		};
		// Check sizes match
		assertEquals(wordints.size(), WORDS.size() * INTS.size());
		assertEquals(wordints.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using get()
		for (int i = 0; i != WORDS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				String s1 = WORDS.get(i) + "," + INTS.get(j);
				String s2 = wordints.get((j * WORDS.size()) + i);
				assertEquals(s1, s2);
			}
		}
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
		Domain.Small<String> intwords = new AbstractSmallDomain.BinaryProduct<String, Integer, String>(INTS, WORDS) {

			@Override
			public String get(Integer i, String s) {
				return s + "," + i;
			}

		};
		// Check sizes match
		assertEquals(intwords.size(), WORDS.size() * INTS.size());
		assertEquals(intwords.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using get()
		for (int i = 0; i != WORDS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				String s1 = WORDS.get(i) + "," + INTS.get(j);
				String s2 = intwords.get((i * INTS.size()) + j);
				assertEquals(s1, s2);
			}
		}
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
		Domain.Small<String> wordintbools = new AbstractSmallDomain.TernaryProduct<String, String, Integer, Boolean>(WORDS,
				INTS, BOOL) {

			@Override
			public String get(String s, Integer i, Boolean b) {
				return s + "," + i + "," + b;
			}

		};
		// Check sizes match
		assertEquals(wordintbools.size(), WORDS.size() * INTS.size() * BOOL.size());
		assertEquals(wordintbools.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()).multiply(BOOL.bigSize()));
		// Check elements match as expected
		for (int i = 0; i != WORDS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != BOOL.size(); ++k) {
					String s1 = WORDS.get(i) + "," + INTS.get(j) + "," + BOOL.get(k);
					String s2 = wordintbools.get((k * WORDS.size() * INTS.size()) + (j * WORDS.size()) + i);
					assertEquals(s1, s2);
				}
			}
		}
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
		Domain.Small<String> boolwordints = new AbstractSmallDomain.TernaryProduct<String, Boolean, String, Integer>(BOOL,
				WORDS, INTS) {

			@Override
			public String get(Boolean b, String s, Integer i) {
				return s + "," + i + "," + b;
			}

		};
		// Check sizes match
		assertEquals(boolwordints.size(), WORDS.size() * INTS.size() * BOOL.size());
		assertEquals(boolwordints.bigSize(), WORDS.bigSize().multiply(INTS.bigSize()).multiply(BOOL.bigSize()));
		// Check elements match as expected
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != WORDS.size(); ++j) {
				for (int k = 0; k != BOOL.size(); ++k) {
					String s1 = WORDS.get(j) + "," + INTS.get(i) + "," + BOOL.get(k);
					String s2 = boolwordints.get((i * WORDS.size() * BOOL.size()) + (j * BOOL.size()) + k);
					assertEquals(s1, s2);
				}
			}
		}
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

		Domain.Small<Integer> word_sizes = new AbstractSmallDomain.NarySequence<Integer, String>(1, WORDS) {

			@Override
			public Integer generate(String[] items) {
				return items[0].length();
			}
		};
		// Check sizes match
		assertEquals(word_sizes.size(), WORDS.size());
		assertEquals(word_sizes.bigSize(), BigInteger.valueOf(WORDS.size()));
		// Check elements match as expected using get()
		for (int i = 0; i != WORDS.size(); ++i) {
			assertEquals(WORDS.get(i).length(), (int) word_sizes.get(i));
		}
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
		Domain.Small<String> intints = new AbstractSmallDomain.NarySequence<String, Integer>(2, INTS) {

			@Override
			public String generate(Integer[] items) {
				return items[0] + "," + items[1];
			}
		};

		// Check sizes match
		assertEquals(intints.size(), INTS.size() * INTS.size());
		assertEquals(intints.bigSize(), INTS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using get()
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				String s1 = INTS.get(i) + "," + INTS.get(j);
				String s2 = intints.get((j * INTS.size()) + i);
				assertEquals(s1, s2);
			}
		}
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
		Domain.Small<String> intintints = new AbstractSmallDomain.NarySequence<String, Integer>(3, INTS) {

			@Override
			public String generate(Integer[] items) {
				return items[0] + "," + items[1] + "," + items[2];
			}
		};
		// Check sizes match
		assertEquals(intintints.size(), INTS.size() * INTS.size() * INTS.size());
		assertEquals(intintints.bigSize(), INTS.bigSize().multiply(INTS.bigSize()).multiply(INTS.bigSize()));
		// Check elements match as expected using get()
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != INTS.size(); ++k) {
					String s1 = INTS.get(i) + "," + INTS.get(j) + "," + INTS.get(k);
					String s2 = intintints.get((k * INTS.size() * INTS.size()) + (j * INTS.size()) + i);
					assertEquals(s1, s2);
				}
			}
		}
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
		Domain.Small<Object> wordints = new AbstractSmallDomain.NarySum<Object>(WORDS, INTS);
		// Check sizes match
		assertEquals(wordints.size(), WORDS.size() + INTS.size());
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
