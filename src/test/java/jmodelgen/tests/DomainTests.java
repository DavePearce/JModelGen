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
public class DomainTests {

	@Test
	public void test_binaryproduct_01() {
		// Convert words into their sizes
		Domain.Big<String> wordints = Domains.Product(WORDS, INTS, (s, i) -> s + "," + i);
		// Check sizes match
		assertEquals(WORDS.bigSize().multiply(INTS.bigSize()),wordints.bigSize());
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
		Domain.Big<String> intwords = Domains.Product(INTS, WORDS, (i, s) -> s + "," + i);
		// Check sizes match
		assertEquals(WORDS.bigSize().multiply(INTS.bigSize()),intwords.bigSize());
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
	public void test_naryproduct_01() {
		Domain.Big<String[]> words = Domains.Array(1, WORDS);
		// Check sizes match
		assertEquals(BigInteger.valueOf(WORDS.size()),words.bigSize());
		// Check elements match as expected using iterator()
		Iterator<String[]> iterator = words.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			String[] ws = iterator.next();
			assertEquals(1,ws.length);
			assertEquals(WORDS.get(i), ws[0]);
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_02() {
		Domain.Big<Integer[]> words = Domains.Array(2, INTS);
		// Check sizes match
		assertEquals(INTS.bigSize().multiply(INTS.bigSize()),words.bigSize());
		// Check elements match as expected using iterator()
		Iterator<Integer[]> iterator = words.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				Integer[] is = iterator.next();
				assertEquals(2,is.length);
				assertEquals(INTS.get(j), is[0]);
				assertEquals(INTS.get(i), is[1]);
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_03() {
		Domain.Big<Integer[]> words = Domains.Array(3, INTS);
		// Check sizes match
		assertEquals(INTS.bigSize().multiply(INTS.bigSize()).multiply(INTS.bigSize()), words.bigSize());
		// Check elements match as expected using iterator()
		Iterator<Integer[]> iterator = words.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != INTS.size(); ++k) {
					Integer[] is = iterator.next();
					assertEquals(3, is.length);
					assertEquals(INTS.get(k), is[0]);
					assertEquals(INTS.get(j), is[1]);
					assertEquals(INTS.get(i), is[2]);
				}
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_04() {
		Domain.Big<Integer[]> words = Domains.Array(0, 2, INTS);
		// Check sizes match
		BigInteger size0 = BigInteger.ONE;
		BigInteger size1 = INTS.bigSize();
		BigInteger size2 = size1.multiply(INTS.bigSize());
		assertEquals(size0.add(size1).add(size2), words.bigSize());
		// Check elements match as expected using iterator()
		Iterator<Integer[]> iterator = words.iterator();
		// Length == 0
		assertArrayEquals(iterator.next(), new Integer[0]);
		// Length == 1
		for (int i = 0; i != INTS.size(); ++i) {
			Integer[] is = iterator.next();
			assertEquals(1, is.length);
			assertEquals(INTS.get(i), is[0]);
		}
		// Length == 2
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				Integer[] is = iterator.next();
				assertEquals(2, is.length);
				assertEquals(INTS.get(j), is[0]);
				assertEquals(INTS.get(i), is[1]);
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
