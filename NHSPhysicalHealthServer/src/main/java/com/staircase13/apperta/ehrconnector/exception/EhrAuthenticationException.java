package com.staircase13.apperta.ehrconnector.exception;


import com.staircase13.apperta.service.exception.AppertaException;

/**
 * A specialised exception indicating that a remote request to a record provider (EHR or demographics) failed
 * due to a remote authentication issue.
 */
public class EhrAuthenticationException extends AppertaException {

    private String username;

    private int responseCode;

    /**
     * Create new authentication exception with username and response code.
     * @param responseCode Response code received from the remote server.
     * @param username Username that failed authentication.
     */
    public EhrAuthenticationException(int responseCode, String username) {
        super("apperta.ehr.unauthorized", "Authentication error " + responseCode + "for " + username);
        this.responseCode = responseCode;
        this.username = username;
    }

    /**
     * The response code received that triggered the authentication exception.
     * @return The numeric response code.
     */
    public int getResponseCode() {
        return responseCode;
    }
}
