package com.sikina.recordtransformer;

public class PartialTransformation<T extends Record, V> {
    private final RecordLens<T> wrapper;
    private final Put putFunc;
    private final String key;

    public PartialTransformation(RecordLens<T> wrapper, Put putFunc, String key) {
        this.wrapper = wrapper;
        this.putFunc = putFunc;
        this.key = key;
    }

    public RecordLens<T> as(V value) {
        putFunc.put(key, value);
        return wrapper;
    }
}
