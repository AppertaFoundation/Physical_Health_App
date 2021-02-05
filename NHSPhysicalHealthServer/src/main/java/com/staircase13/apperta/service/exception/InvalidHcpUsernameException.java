package com.staircase13.apperta.service.exception;

public class InvalidHcpUsernameException extends AppertaException {
    public InvalidHcpUsernameException(String username) {
        super("apperta.hcp.invalid.username", username);
    }
}
