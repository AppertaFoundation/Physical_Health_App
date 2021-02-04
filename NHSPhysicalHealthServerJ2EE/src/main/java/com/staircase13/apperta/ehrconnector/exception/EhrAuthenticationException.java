package com.staircase13.apperta.ehrconnector.exception;


import com.staircase13.apperta.service.exception.AppertaException;

public class EhrAuthenticationException extends AppertaException {

    private String username;

    private int responseCode;

    public EhrAuthenticationException(int responseCode, String username) {
        super("apperta.ehr.unauthorized", "Authentication error " + responseCode + "for " + username);
        this.responseCode = responseCode;
        this.username = username;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
