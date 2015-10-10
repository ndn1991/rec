package com.vinecom.common.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ndn on 7/21/2015.
 */
public class CommonArray extends CommonData {

    private static final long serialVersionUID = -2924138158989680210L;
    private final ArrayList<Object> list;

    public CommonArray() {
        list = new ArrayList<>();
    }

    /**
     * @param objects must be one of types (Long, Integer, Double, Float, String, Boolean, Character, CommonInterface)
     */
    public CommonArray(List<Object> objects) {
        this();
        objects.forEach(this::add);
    }

    /**
     * @param objects must be one of types (Long, Integer, Double, Float, String, Boolean, Character, CommonInterface)
     */
    public CommonArray(Object... objects) {
        this();
        for (Object o : objects) {
            this.add(o);
        }
    }

    /**
     * @param o must be one of types (Long, Integer, Double, Float, String, Boolean, Character, CommonInterface)
     */
    public boolean add(Object o) {
        if (CommonObject.typeAccepted(o)) {
            return this.list.add(o);
        } else {
            throw new IllegalArgumentException("This object has type not be accepted: " + o);
        }
    }

    public List asList() {
        return Arrays.asList(this.list.toArray());
    }

    @Override
    public String toString() {
        return this.list.toString();
    }

    @Override
    public void copyFrom(CommonData other) {
        if (other instanceof CommonArray) {
            this.list.clear();
            this.list.addAll(((CommonArray) other).list);
        } else {
            throw new IllegalArgumentException("input is not a CommonArray");
        }
    }
}

