package com.sikina.recordtransformer;

/**
 * Function interface used to pass Map::put to PartialTransformation
 * Not for external use.
 */
@FunctionalInterface
interface Put {
    Object put(String key, Object value);
}
