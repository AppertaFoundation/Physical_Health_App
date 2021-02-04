package com.staircase13.apperta.ehrconnector.exception;

import com.staircase13.apperta.service.exception.AppertaException;

public class EhrOperationException extends AppertaException {

    public EhrOperationException(String messageCode, Throwable cause, Object... messageArgs) {
        super(messageCode, messageArgs);
        if (cause != null) {
            initCause(cause);
        }
    }

}
