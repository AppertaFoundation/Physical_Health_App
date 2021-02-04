package com.staircase13.apperta.ehrconnector.exception;

import com.staircase13.apperta.service.exception.AppertaJsonException;

import java.util.Map;

public class EhrServerException extends AppertaJsonException {

    public EhrServerException(int errorCode, Map<String,String> errors) {
        super(errorCode, errors);
    }

    public EhrServerException(int errorCode, Map<String,String> errors, Exception cause){
        this(errorCode, errors);
        initCause(cause);
    }
}
