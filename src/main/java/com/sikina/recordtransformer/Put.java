package com.sikina.recordtransformer;

@FunctionalInterface
public interface Put {
    public Object put(String key, Object value);
}
