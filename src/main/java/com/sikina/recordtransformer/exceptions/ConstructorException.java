package com.sikina.recordtransformer.exceptions;

public class ConstructorException extends RuntimeException {
    public ConstructorException(Throwable e) {
        super("Error finding or invoking constructor", e);
    }
}
