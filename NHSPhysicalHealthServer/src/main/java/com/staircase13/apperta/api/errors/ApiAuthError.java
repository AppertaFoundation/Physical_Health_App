package com.staircase13.apperta.api.errors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the structure used by the spring-security-oauth framework for 401 and 403 messages by default, originally
 * defined in a package private class JaxbOAuth2Exception
 *
 * We can override certain messages by handling the AccessDeniedException in our CustomResetExceptionHandler:
 *
 * 401 - No Token Provided
 * 403 - Access Denied
 *
 * But other 401 messages such as Token Expired and Invalid Token cannot be cleanly overridden
 * (see https://github.com/spring-projects/spring-security-oauth/issues/690)
 *
 * For this reason, we report on all Authorisation Errors using the JaxbOAuth2Exception structure (as defined in
 * this class), and document this in the Swagger.
 */
@Getter
@Setter
@Builder
public class ApiAuthError {
    private String error;
    private String error_description;
}
