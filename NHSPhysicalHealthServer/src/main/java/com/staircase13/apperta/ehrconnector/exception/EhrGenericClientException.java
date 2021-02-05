package com.staircase13.apperta.ehrconnector.exception;

/**
 * An exception that is thrown to represent a issue that occurs in the client connector when trying to make a request
 * to a remote records provider. This exception is not generated if the server responds with an error, but can result
 * from incomplete arguments or lack of server availability on the endpoints used by the connector.
 */
public class EhrGenericClientException extends EhrOperationException {

    /**
     * Create a new exception containing the internal message representing the error.
     * @param message The message with the reason for the exception.
     */
    public EhrGenericClientException(String message) {
        this(message, null);
    }

    /**
     * Create a new exception containing the internal message representing the error and an exception which caused this to be created.
     * @param message The message with the reason for the exception.
     * @param cause The exception representing the underlying cause.
     */
    public EhrGenericClientException(String message, Throwable cause) {
        super("apperta.ehr.client.exception", cause, message);
    }


}
