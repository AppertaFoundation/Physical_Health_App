package com.staircase13.apperta.service.exception;

import java.util.Map;

public class AppertaJsonException extends AppertaException {

    private int errorCode;

    private Map<String, String> errorMap;

    public AppertaJsonException(int errorCode, Map<String, String> errorMap) {
        super("", "");
        this.errorCode = errorCode;
        this.errorMap = errorMap;
    }

    public int getErrorCode(){
        return errorCode;
    }

    public Map<String, String> getErrorMap() {
        return errorMap;
    }
}
