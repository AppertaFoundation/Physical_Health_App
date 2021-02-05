package com.staircase13.apperta.service.exception;

public class UsernameAlreadyRegisteredException extends AppertaException {
    public UsernameAlreadyRegisteredException(String username) {
        super("apperta.user.username.already.registered", username);
    }
}
