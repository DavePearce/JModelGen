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
package jmodelgen.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import jmodelgen.core.Domain.Big;
import jmodelgen.core.Domain;
import jmodelgen.core.Walker;

public class Walkers {

	/**
	 * A simple adaptor from an arbitrary domain to a walker.
	 *
	 * @param <T>
	 * @param domain
	 * @return
	 */
	public static <T> Walker<T> Adaptor(Domain.Small<T> domain) {
		return new AbstractWalker<T>() {
			private long index = 0;
			@Override
			public void reset() {
				index = 0;
			}

			@Override
			public boolean finished() {
				return index >= domain.size();
			}

			@Override
			public void next() {
				index = index + 1;
			}

			@Override
			public long advance(long n) {
				long m = domain.size() - index;
				if(n > m) {
					index = index + m;
					return m;
				} else {
					index = index + n;
					return n;
				}
			}

			@Override
			public T get() {
				return domain.get(index);
			}

		};
	}

	/**
	 * A simple adaptor from an arbitrary (big) domain to a walker.
	 *
	 * @param <T>
	 * @param domain
	 * @return
	 */
	public static <T> Walker<T> Adaptor(Domain.Big<T> domain) {
		return new AbstractWalker<T>() {
			private BigInteger index = BigInteger.ZERO;
			@Override
			public void reset() {
				index = BigInteger.ZERO;
			}

			@Override
			public boolean finished() {
				return index.compareTo(domain.bigSize()) >= 0;
			}

			@Override
			public void next() {
				index = index.add(BigInteger.ONE);
			}

			@Override
			public long advance(long i) {
				// Calculate maximum step
				BigInteger m = domain.bigSize().subtract(index);
				// Check whether could fail
				if(m.bitLength() <= 63) {
					// Could fail
					long max = m.longValueExact();
					if(i >= max) {
						index = index.add(m);
						return max;
					}
				}
				// Cannot fail
				index = index.add(BigInteger.valueOf(i));
				//
				return i;
			}


			@Override
			public T get() {
				return domain.get(index);
			}

		};
	}

	/**
	 * A simple adaptor from an arbitrary (big) domain of one type to a walker of
	 * another type, assuming mapping from one to the other exists.
	 *
	 * @param <T>
	 * @param <S>
	 * @param domain
	 * @param mapping
	 * @return
	 */
	public static <T, S> Walker<T> Adaptor(Domain.Big<S> domain, Function<S, T> mapping) {
		return Adaptor(new AbstractBigDomain.Adaptor<T, S>(domain) {

			@Override
			public T get(S s) {
				return mapping.apply(s);
			}

		});
	}

	public static <T, S> Walker<T> Adaptor(Walker<S> walker, Function<S, T> mapping) {
		return new AbstractWalker.Unary<T, S>(walker) {

			@Override
			public T get(S s) {
				return mapping.apply(s);
			}

		};
	}

	public static <T, L, R> Walker<T> Product(Domain.Big<L> left, Domain.Big<R> right, BiFunction<L, R, T> mapping) {
		return new AbstractWalker.Binary<T, L, R>(Adaptor(left), Adaptor(right)) {

			@Override
			public T get(L left, R right) {
				return mapping.apply(left, right);
			}
		};
	}

	public static <T, L, R> Walker<T> Product(Domain.Small<L> left, Walker<R> right, BiFunction<L, R, T> mapping) {
		return new AbstractWalker.Binary<T, L, R>(Adaptor(left), right) {

			@Override
			public T get(L left, R right) {
				return mapping.apply(left, right);
			}
		};
	}


	public static <T, S> Walker<T> Product(int min, Function<List<S>, T> mapping, Walker<S>... walkers) {
		return new AbstractWalker.Nary<T, S>(min, walkers) {
			@Override
			public T get(List<S> items) {
				return mapping.apply(items);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T, S> Walker<T> Product(int min, int max, Walker.State<S> seed, Function<List<S>, T> mapping) {
		return new AbstractWalker.LazyNary<T, S>(min, max, seed) {
			@Override
			public T get(List<S> items) {
				return mapping.apply(items);
			}
		};
	}

	public static <T> Walker<T> Union(Walker<? extends T> ...walkers) {
		return new AbstractWalker.Option(walkers);
	}
}
