package com.vinecom.common.tool.topK;

import com.vinecom.common.data.Tuple2;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created by ndn on 7/23/2015.
 */
public class TreeMapTopK<A> implements TopK<A> {

	private static final long serialVersionUID = 5923552345898635562L;
	private int k;
	private TreeMap<Double, A> arr = new TreeMap<>(new ValueComparator());

	public TreeMapTopK(int k) {
		this.k = k;
	}

	@Override
	public TopK<A> put(Tuple2<A, Double> e) {
		return put(e._1, e._2);
	}

	@Override
	public TopK<A> put(A a, double d) {
		if (this.size() > this.k && d <= this.arr.firstKey()) {
			return this;
		}
		arr.put(d, a);
		return this;
	}

	@Override
	public TopK<A> put(TopK<A> other) {
		for (Tuple2<A, Double> e : other.all()) {
			put(e._1, e._2);
		}

		return this;
	}

	@Override
	public int size() {
		return this.arr.size();
	}

	@Override
	public Tuple2<A, Double>[] real() {
		return this.all();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Tuple2<A, Double>[] all() {
		Tuple2<A, Double>[] tmp = new Tuple2[this.size()];
		this.arr.entrySet().toArray(tmp);
		return tmp;
	}

	class ValueComparator implements Comparator<Double> {

		@Override
		public int compare(Double o1, Double o2) {
			return o2.compareTo(o1);
		}
	}
}
