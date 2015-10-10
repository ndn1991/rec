package com.vinecom.common.tool.topK;

import com.vinecom.common.data.Tuple2;

import java.util.Arrays;

/**
 * Created by ndn on 7/23/2015.
 */
public class ArrayTopK<A> implements TopK<A> {
    private Tuple2<A, Double>[] arr;
    private int iMin = 0;
    private double vMin = Double.POSITIVE_INFINITY;
    private int size = 0;
    private int k;

    public ArrayTopK(int k) {
        this.arr = new Tuple2[k];
        this.k = k;
    }

    @Override
    public TopK<A> put(Tuple2<A, Double> v) {
        if (size < k) {
            arr[size] = v;
            if (v._2 < vMin) {
                vMin = v._2;
                iMin = size;
            }
            size += 1;
        }
        else if (v._2 > vMin) {
            arr[iMin] = v;
            iMin = 0;
            vMin = arr[0]._2;
            for (int i = 1; i < k; i++) {
                if (arr[i]._2 < vMin) {
                    iMin = i;
                    vMin = arr[i]._2;
                }
            }
        }

        return this;
    }

    @Override
    public TopK<A> put(A a, double d) {
        return put(new Tuple2<A, Double>(a, d));
    }

    @Override
    public TopK<A> put(TopK<A> other) {
        for (Tuple2<A, Double> e : other.all()) {
            put(e);
        }

        return this;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Tuple2<A, Double>[] real() {
        Tuple2<A, Double>[] copy = all();
        Arrays.sort(copy);
        return copy;
    }

    @Override
    public Tuple2<A, Double>[] all() {
        return Arrays.copyOf(this.arr, this.size);
    }
}
