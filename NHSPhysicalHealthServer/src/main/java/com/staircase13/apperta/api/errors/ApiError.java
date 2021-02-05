package com.staircase13.apperta.api.errors;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {

    @ApiModelProperty(notes = "Describes the error that occurred", required = true)
    private String message;

    @ApiModelProperty(notes = "The request URL", required = true)
    private String url;

    @ApiModelProperty(notes = "Date Time for when the error occurred", required = true)
    private LocalDateTime timestamp;

    @ApiModelProperty(notes = "The request Method", required = true)
    private String method;

    @ApiModelProperty(notes = "Unique ID for this request", required = true)
    private String uuid;

}
