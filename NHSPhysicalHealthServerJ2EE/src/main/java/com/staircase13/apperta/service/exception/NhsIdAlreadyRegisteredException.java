package com.staircase13.apperta.service.exception;

public class NhsIdAlreadyRegisteredException extends AppertaException {
    public NhsIdAlreadyRegisteredException(String nhsid) {
        super("apperta.hcp.nhsid.already.registered", nhsid);
    }
}
