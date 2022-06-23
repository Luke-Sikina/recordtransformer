package com.sikina.recordtransformer;

/**
 * This is thrown when an error occurs while accessing or invoking the constructor
 * of a record being wrapped by RecordLens
 */
public class ConstructorException extends RuntimeException {
    ConstructorException(Throwable e) {
        super("Error finding or invoking constructor", e);
    }
}
