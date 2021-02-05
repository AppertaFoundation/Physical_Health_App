package com.staircase13.apperta.service.exception;

public class InvalidQueryTemplateException extends AppertaException {
    public InvalidQueryTemplateException(String templateName) {
        super("apperta.ehr.invalid.queryTemplate", templateName);
    }
}
