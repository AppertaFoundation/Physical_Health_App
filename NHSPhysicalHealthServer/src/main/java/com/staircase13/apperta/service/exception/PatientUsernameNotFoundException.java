package com.staircase13.apperta.service.exception;

public class PatientUsernameNotFoundException extends AppertaException {
    public PatientUsernameNotFoundException(String username) {
        super("apperta.patient.username.notfound", username);
    }
}
