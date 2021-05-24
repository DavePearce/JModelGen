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
package jmodelgen.core;

import java.util.Iterator;

public interface Walker<T> extends Iterable<T> {
	public void reset();

	public boolean finished();

	public T get();

	public void next();

	public long advance(long n);

	@Override
	public Iterator<T> iterator();

	/**
	 *
	 * @author David J. Pearce
	 *
	 * @param <T>
	 */
	public static interface State<T> {
		public Walker<T> construct();

		public State<T> transfer(T item);
	}
}
