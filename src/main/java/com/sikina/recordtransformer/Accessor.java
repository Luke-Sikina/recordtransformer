package com.sikina.recordtransformer;

import java.io.Serializable;

/**
 * This is a serializable getter. Extending Serializable allows us to retain information
 * like the method name, which lets this serve as both type checking and a reference
 * to a field in a record.
 */
@FunctionalInterface
public interface Accessor<V> extends Serializable {
    /**
     * @return the value in the record that corresponds to this field
     */
    V get();
}
