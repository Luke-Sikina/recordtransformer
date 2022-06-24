package com.sikina.recordtransformer;

/**
 * This is a transitory class used to make the Java compiler enforce
 * type T when making the transformation {@code with(Accessor<T> getter, T val)}.
 *
 * This class should never be instantiated directly, nor should it be stored in a variable.
 * Rather, you should create, use, and discard this class as part of a with + as chain:
 * {@code with(Accessor<T> getter).as(T val)}
 *
 * @param <T> the type of the record wrapped by the corresponding RecordLens
 * @param <V> the type of the field in T being updated
 */
public class PartialTransformation<T extends Record, V> {
    private final RecordTransformer<T> wrapper;
    private final Put putFunc;
    private final String key;

    PartialTransformation(RecordTransformer<T> wrapper, Put putFunc, String key) {
        this.wrapper = wrapper;
        this.putFunc = putFunc;
        this.key = key;
    }

    /**
     * Complete the queuing of the transformation for the record.
     * @param value the value to set the field to when transform is called.
     * @return The original lens, for chaining.
     */
    public RecordTransformer<T> as(V value) {
        putFunc.put(key, value);
        return wrapper;
    }
}
