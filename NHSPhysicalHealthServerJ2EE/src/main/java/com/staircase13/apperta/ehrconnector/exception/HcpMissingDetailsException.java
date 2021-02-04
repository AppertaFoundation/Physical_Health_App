package com.staircase13.apperta.ehrconnector.exception;

import com.staircase13.apperta.service.exception.AppertaException;

public class HcpMissingDetailsException extends AppertaException {
    public HcpMissingDetailsException(String username) {
        super("apperta.unauthorized.hcp.details", username);
    }
}
