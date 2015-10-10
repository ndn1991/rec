package com.adr.bigdata.search.handler.firstclassfunction;

public interface Function<T, U> {
	/**
	 * Apply this function.
	 * 
	 * @param t
	 *            object to apply this function to
	 * @return the result of applying this function to t.
	 */
	public U apply(T t);

}
