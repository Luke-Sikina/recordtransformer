package com.sikina.recordtransformer;

public class GetterMappingException extends RuntimeException {
    public GetterMappingException(Throwable cause) {
        super("error while trying to map to record attribute", cause);
    }
}
