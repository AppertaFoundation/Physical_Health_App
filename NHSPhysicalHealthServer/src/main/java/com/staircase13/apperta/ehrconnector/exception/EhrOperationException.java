package com.staircase13.apperta.ehrconnector.exception;

import com.staircase13.apperta.service.exception.AppertaException;

/**
 * A base exception class representing any error condition that occurs as a result of connecting to an external
 * record provider for create, update, query or delete operations.
 */
public class EhrOperationException extends AppertaException {

    /**
     * Create a new exception containing an internal error code, error message, and an underlying cause for the exception.
     * @param messageCode Internal code.
     * @param cause Underlying cause of exception.
     * @param message Error message
     */
    public EhrOperationException(String messageCode, Throwable cause, String message) {
        super("apperta.ehr.operation.error", messageCode, message);
        if (cause != null) {
            initCause(cause);
        }
    }

    /**
     * Create a new exception containing the underlying exception cause of this error but no other message.
     * @param messageCode Internal code.
     * @param cause Underlying cause of exception.
     */
    public EhrOperationException(String messageCode, Throwable cause) {
        super("apperta.ehr.operation.error", messageCode, "");
        if (cause != null) {
            initCause(cause);
        }
    }
}
