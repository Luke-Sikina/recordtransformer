package com.sikina.recordtransformer;

import java.io.Serializable;

@FunctionalInterface
public interface Accessor<V> extends Serializable {
    public V get();
}
