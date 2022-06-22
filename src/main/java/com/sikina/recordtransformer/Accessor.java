package com.sikina.recordtransformer;

import java.io.Serializable;

/**
 * We make our own functional interface rather than using something like Invokable
 * because we need the interface to be serializable in order to parse the method name.
 */
@FunctionalInterface
public interface Accessor<V> extends Serializable {
    public V get();
}
