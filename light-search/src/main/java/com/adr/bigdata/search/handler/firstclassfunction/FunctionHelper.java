package com.adr.bigdata.search.handler.firstclassfunction;

import java.util.ArrayList;
import java.util.List;

public class FunctionHelper {
	/**
	 * Apply a function to every element of a list.
	 * 
	 * @param f
	 *            function to apply
	 * @param list
	 *            list to iterate over
	 * @return [f(list[0]), f(list[1]), ..., f(list[n-1])]
	 */
	public static <T, U> List<U> map(Function<T, U> f, List<T> list) {
		List<U> result = new ArrayList<U>();
		for (T t : list) {
			result.add(f.apply(t));
		}
		return result;
	}

	/**
	 * Compose two functions.
	 * 
	 * @param f
	 *            function A->B
	 * @param g
	 *            function B->C
	 * @return new function A->C formed by composing f with g
	 */
	public static <A, B, C> Function<A, C> compose(final Function<A, B> f, final Function<B, C> g) {
		return new Function<A, C>() {
			public C apply(A t) {
				return g.apply(f.apply(t));
			}
		};
	}

	/**
	 * Apply a function to every element of a list.
	 * 
	 * @param f
	 *            function to apply
	 * @param list
	 *            list to iterate over
	 * @return [f(list[0]), f(list[1]), ..., f(list[n-1])]
	 */
	public static <T> List<T> filter(Predicate<T> p, List<T> list) {
		List<T> result = new ArrayList<T>();
		for (T t : list) {
			if (p.condition(t)) {
				result.add(t);
			}
		}
		return result;
	}

}
