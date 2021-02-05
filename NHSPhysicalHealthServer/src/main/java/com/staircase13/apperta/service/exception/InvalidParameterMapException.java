package com.staircase13.apperta.service.exception;

public class InvalidParameterMapException extends AppertaException {
    public InvalidParameterMapException(String mapName) {
        super("apperta.ehr.invalid.parametermap", mapName);
    }
}
