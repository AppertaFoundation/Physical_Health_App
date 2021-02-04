package com.staircase13.apperta.api.errors;

import brave.Tracer;
import com.google.common.collect.Sets;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.service.exception.AppertaException;
import com.staircase13.apperta.service.exception.AppertaJsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

    private final Clock clock;

    private final Tracer tracer;

    private final MessageSource messageSource;

    @Autowired
    public CustomRestExceptionHandler(Clock clock, Tracer tracer, MessageSource messageSource) {
        this.clock = clock;
        this.tracer = tracer;
        this.messageSource = messageSource;
    }

    @ExceptionHandler(AppertaException.class)
    public ResponseEntity<Object> appertaExceptionHandler(AppertaException appertaException, WebRequest request) {
        LOGGER.debug("Handling Apperta Exception with Code '{}' and Args '{}'",
                appertaException.getMessageCode(),
                Arrays.asList(appertaException.getMessageArgs()));

        return defaultResponse(
                resolveMessage(appertaException),
                request,
                resolveStatusCode(appertaException));
    }

    private String resolveMessage(AppertaException appertaException) {
        try {
            String resolvedMessage = messageSource.getMessage(
                    appertaException.getMessageCode(),
                    appertaException.getMessageArgs(),
                    Locale.getDefault());

            return String.format("%s: %s", appertaException.getMessageCode(), resolvedMessage);
        } catch(org.springframework.context.NoSuchMessageException e) {
            return ErrorConstants.HTTP_500_MESSAGE;
        }
    }

    private HttpStatus resolveStatusCode(AppertaException appertaException) {
        try {
            String statusCode = messageSource.getMessage(
                    appertaException.getMessageCode() + ".http.status",
                    new Object[0],
                    Locale.getDefault()).trim();
            LOGGER.debug("Resolved status code for '{}' as '{}'", appertaException.getMessageCode(),statusCode);
            return HttpStatus.resolve(Integer.parseInt(statusCode));
        } catch(org.springframework.context.NoSuchMessageException e) {
            LOGGER.warn("No status code defined for Apperta Exception '{}'. Using 500",appertaException.getMessageCode());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } catch(NumberFormatException e) {
            LOGGER.warn("Could not process status code for " + appertaException.getMessageCode() + ". Using 500");
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @ExceptionHandler(AppertaJsonException.class)
    public ResponseEntity<Object> appertaJsonExceptionHandler(AppertaJsonException appertaJsonException, WebRequest request) {

        LOGGER.debug("Handling Apperta Json Exception with errorCode '{}' ", appertaJsonException.getErrorCode());

        return defaultResponse(resolveJsonMessage(appertaJsonException),
                request,
                resolveStatusCode(appertaJsonException));
    }

    private String resolveJsonMessage(AppertaJsonException appertaJsonException) {
        try {
            Map<String,String> errorMap = appertaJsonException.getErrorMap();
            String errors = errorMap.keySet().stream().map(k -> String.format("\"%s\":\"%s\"",k, errorMap.get(k))).collect(Collectors.joining(","));
            Object[] messageArgs = new Object[] {Integer.toString(appertaJsonException.getErrorCode()), errors};
         StringBuffer sb = new StringBuffer("{\"errorCode\": ").append(messageArgs[0]).append(" , \"errors\": [").append(errors).append("] }");
            return sb.toString();
            // TODO: fix
         //   return messageSource.getMessage(
         //           "apperta.json.formatter",
         //           messageArgs,
         //           Locale.getDefault());

        } catch(org.springframework.context.NoSuchMessageException e) {
            return ErrorConstants.HTTP_500_MESSAGE;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> defaultErrorHandler(Exception e, WebRequest request) {
        LOGGER.error("Default error handler invoked for exception",e);
        return defaultResponse(ErrorConstants.HTTP_500_MESSAGE, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError = new ApiValidationErrors(
                ErrorConstants.HTTP_400_MESSAGE,
                getUrl(request),
                getTime(),
                getMethod(request),
                getUuid(),
                Sets.newHashSet(
                        ApiValidationErrors.ValidationError.builder().field(ex.getParameterName()).message(ex.getMessage()).build()));

        return new ResponseEntity<>(
                apiError, new HttpHeaders(), status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Set<ApiValidationErrors.ValidationError> validationErrors = new HashSet<>();

        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(ApiValidationErrors.ValidationError.builder().field(error.getField()).message(error.getDefaultMessage()).build());
        }

        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            validationErrors.add(ApiValidationErrors.ValidationError.builder().message(error.getDefaultMessage()).build());
        }

        ApiError apiError = new ApiValidationErrors(
                "Request not valid",
                getUrl(request),
                getTime(),
                getMethod(request),
                getUuid(),
                validationErrors);

        return new ResponseEntity<>(
                apiError, new HttpHeaders(), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDenied(Exception e, WebRequest request) {
        /**
         * Unfortunately AccessDeniedException doesn't provide us with any context, so
         * we need to look at the request to determine if it's a 401 or 403
         *
         * @see ApiAuthError
         */
        boolean isAuthorizationError = request.getHeader(HttpHeaders.AUTHORIZATION) != null;

        if(isAuthorizationError) {
            return authorizationError();
        } else {
            // note that expired/invalid token errors are handled by spring-secuity-oauth. See notes on ApiAuthError
            return authenticationError();
        }
    }

    private ResponseEntity<Object> authenticationError() {
        /**
         * As it can't be easily overridden, we have to use the Spring Security Oauth Structure
         *
         * @see ApiAuthError
         */
        ApiAuthError apiError = ApiAuthError.builder()
                .error("authentication_failure")
                .error_description("Cannot authenticate the caller")
                .build();
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }


    private ResponseEntity<Object> authorizationError() {
        /**
         * As it can't be easily overridden, we have to use the Spring Security Oauth Structure
         *
         * @see ApiAuthError
         */
        ApiAuthError apiError = ApiAuthError.builder()
                .error("unauthorized")
                .error_description("Caller is not authorized to access this resource")
                .build();
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return defaultResponse(ex.getMessage(), request, status);
    }

    private ResponseEntity<Object> defaultResponse(String message, WebRequest request, HttpStatus httpStatus) {
        return defaultResponse(message, getUrl(request), getMethod(request), httpStatus);
    }

     private ResponseEntity<Object> defaultResponse(String message, String url, String method, HttpStatus httpStatus) {
        ApiError apiError = ApiError.builder()
                .message(message)
                .timestamp(getTime())
                .url(url)
                .method(method)
                .uuid(getUuid())
                .build();
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), httpStatus);
    }

    private String getMethod(WebRequest request) {
        return ((ServletWebRequest)request).getHttpMethod().toString();
    }

    private String getUrl(WebRequest request) {
       return ((ServletWebRequest)request).getRequest().getRequestURI();
    }

    private LocalDateTime getTime() {
        return LocalDateTime.now(clock);
    }

    private String getUuid() {
        return Long.toHexString(tracer.currentSpan().context().traceId());
    }

}
