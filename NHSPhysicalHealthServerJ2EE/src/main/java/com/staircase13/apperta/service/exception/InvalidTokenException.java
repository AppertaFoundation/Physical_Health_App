package com.staircase13.apperta.service.exception;

public class InvalidTokenException extends AppertaException {
    public InvalidTokenException(String tokenId) {
        super("apperta.invalid.token", tokenId);
    }
}
