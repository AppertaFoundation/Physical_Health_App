package com.staircase13.apperta.api.errors;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class ApiValidationErrors extends ApiError {

    private final Set<ValidationError> validationErrors;

    public ApiValidationErrors(String message, String url, LocalDateTime timestamp, String method, String uuid, Set<ValidationError> validationErrors) {
        super(message, url, timestamp, method, uuid);
        this.validationErrors = validationErrors;
    }

    @Getter
    @Builder
    public static class ValidationError {
        @ApiModelProperty(notes = "If provided, identifies the field on the request related to this error. If no field is provided, it indicates that the error relates to the request as a whole", required = false)
        private String field;

        @ApiModelProperty(notes = "If provided, identifies the field on the request related to this error", required = true)
        private String message;
    }

}
