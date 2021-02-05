package com.staircase13.apperta.api;

import com.staircase13.apperta.service.exception.AppertaException;

public class IncorrectHcpUsernameException extends AppertaException {
    public IncorrectHcpUsernameException(String requestedUserName, String authenticatedUserName) {
        super("apperta.hcp.incorrect.username", requestedUserName, authenticatedUserName);
    }
}
