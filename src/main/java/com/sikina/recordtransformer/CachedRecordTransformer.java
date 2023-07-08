package com.sikina.recordtransformer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CachedRecordTransformer<T extends Record> extends RecordTransformer<T>{

    private static final ConcurrentHashMap<String, Object> getterCache
        = new ConcurrentHashMap<>();
    /**
     * Call this constructor once, at the beginning of your record's lifecycle. This constructor is a bit
     * expensive, so avoid duplicate wrapping if you can.
     *
     * @param rec the record to transform
     * @throws GetterException thrown if the wrapper can't get record components from rec
     */
    public CachedRecordTransformer(T rec) throws GetterException {
        super(rec);
        // create a list of getters that accept a T and produce the relevant field value
        // these are used to get old values in transform
        getters = initGetters(rec);
    }

    private synchronized Map<String, Function<T, Object>> initGetters(T rec) {
        if (!getterCache.containsKey(rec.getClass().getName())) {
            getterCache.put(rec.getClass().getName(), createGetters(rec));
        }
        // This awful cast is needed because the cache is static, and is populated with
        // getters for multiple different record types at runtime
        //noinspection unchecked
        return (Map<String, Function<T, Object>>) getterCache.get(rec.getClass().getName());
    }
}
