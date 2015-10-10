package com.vinecom.common.tool.topK;

import java.io.Serializable;

import com.vinecom.common.data.Tuple2;

/**
 * Created by ndn on 7/23/2015.
 */
public interface TopK<A> extends Serializable {
	TopK<A> put(Tuple2<A, Double> e);

	TopK<A> put(A a, double d);

	TopK<A> put(TopK<A> other);

	int size();

	Tuple2<A, Double>[] real();

	Tuple2<A, Double>[] all();
}
