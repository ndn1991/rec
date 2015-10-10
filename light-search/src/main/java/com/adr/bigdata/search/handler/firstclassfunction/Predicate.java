package com.adr.bigdata.search.handler.firstclassfunction;

public interface Predicate<T> {
	/*
	 * @param t the object to be filtered
	 * 
	 * @return true if the object satisfy the condition
	 */
	public boolean condition(T t);
}
