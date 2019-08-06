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
		Domain.Static<String> wordints = Domains.Product(WORDS, INTS, (s, i) -> s + "," + i);
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
		Domain.Static<String> intwords = Domains.Product(INTS, WORDS, (i, s) -> s + "," + i);
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
	public void test_naryproduct_01() {
		Domain.Static<String[]> words = Domains.Array(new String[1], WORDS);
		// Check sizes match
		assertEquals(words.bigSize(), BigInteger.valueOf(WORDS.size()));
		// Check elements match as expected using iterator()
		Iterator<String[]> iterator = words.iterator();
		for (int i = 0; i != WORDS.size(); ++i) {
			String[] ws = iterator.next();
			assertEquals(ws.length,1);
			assertEquals(WORDS.get(i), ws[0]);
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_02() {
		Domain.Static<Integer[]> words = Domains.Array(new Integer[2], INTS);
		// Check sizes match
		assertEquals(words.bigSize(), INTS.bigSize().multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<Integer[]> iterator = words.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				Integer[] is = iterator.next();
				assertEquals(is.length,2);
				assertEquals(INTS.get(j), is[0]);
				assertEquals(INTS.get(i), is[1]);
			}
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void test_naryproduct_03() {
		Domain.Static<Integer[]> words = Domains.Array(new Integer[3], INTS);
		// Check sizes match
		assertEquals(words.bigSize(), INTS.bigSize().multiply(INTS.bigSize()).multiply(INTS.bigSize()));
		// Check elements match as expected using iterator()
		Iterator<Integer[]> iterator = words.iterator();
		for (int i = 0; i != INTS.size(); ++i) {
			for (int j = 0; j != INTS.size(); ++j) {
				for (int k = 0; k != INTS.size(); ++k) {
					Integer[] is = iterator.next();
					assertEquals(is.length, 3);
					assertEquals(INTS.get(k), is[0]);
					assertEquals(INTS.get(j), is[1]);
					assertEquals(INTS.get(i), is[2]);
				}
			}
		}
		assertFalse(iterator.hasNext());
	}


	@Test
	public void test_narysum() {
		// Convert words into their sizes
		Domain.Static<Object> wordints = new AbstractBigDomain.NarySum<Object>(WORDS, INTS);
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
