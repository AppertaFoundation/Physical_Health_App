package com.staircase13.apperta.service.exception;

public class InvalidAppNameException extends AppertaException {
    public InvalidAppNameException(String appName) {
        super("apperta.app.invalid.appname", appName);
    }
}
