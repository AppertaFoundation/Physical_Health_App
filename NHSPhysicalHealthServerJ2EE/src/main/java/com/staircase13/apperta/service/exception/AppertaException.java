package com.staircase13.apperta.service.exception;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class AppertaException extends Exception {

    private final String messageCode;
    private final Object[] messageArgs;

    /**
     * @param messageCode Corresponds to an entry in messages.properties
     * @param messageArgs Arguments used to resolve placeholders in messages.properties
     */
    public AppertaException(String messageCode, Object... messageArgs) {
        super(messageCode + Arrays.asList(messageArgs));
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }

}
