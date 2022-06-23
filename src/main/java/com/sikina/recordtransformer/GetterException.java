package com.sikina.recordtransformer;

/**
 * This is thrown when an error occurs while getting the value of a record field
 * or while getting the name of a field via an Accessor function.
 */
public class GetterException extends RuntimeException {
    GetterException(Throwable cause) {
        super("error while trying to map to record attribute", cause);
    }
}
