package jmodelgen.util;

import jmodelgen.core.Domain;

public abstract class AbstractDomain<T> implements Domain.Static<T> {

	@Override
	public String toString() {
		String r = "{";
		boolean firstTime = true;
		for(T t : this) {
			if(!firstTime) {
				r += ",";
			}
			firstTime=false;
			r += t.toString();
		}
		return r + "}";
	}

}
