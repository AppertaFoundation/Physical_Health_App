package com.staircase13.apperta.ehrconnector.exception;

import com.staircase13.apperta.service.exception.AppertaException;

/**
 * An exception thrown for operations which try to fetch care professional details if the details are unavailable.
 */
public class HcpMissingDetailsException extends AppertaException {

    /**
     * Create a new exception
     * @param username The name of the HCP for whom details were missing.
     */
    public HcpMissingDetailsException(String username) {
        super("apperta.unauthorized.hcp.details", username);
    }
}
