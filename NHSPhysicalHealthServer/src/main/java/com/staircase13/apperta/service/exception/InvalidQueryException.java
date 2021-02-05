package com.staircase13.apperta.service.exception;

import java.util.List;

public class InvalidQueryException extends AppertaException {
    public InvalidQueryException(List<String> errorList) {
        super("apperta.ehr.invalid.query", String.join(",", errorList));
    }
}
