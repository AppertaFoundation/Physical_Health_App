package com.staircase13.apperta.ehrconnector.exception;

public class EhrGenericClientException extends EhrOperationException{

    public EhrGenericClientException(String message) {
        this(message, null);
    }

    public EhrGenericClientException(String message, Throwable cause) {
        super("apperta.ehr.client.exception", cause, message);
    }


}
