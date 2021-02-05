package com.staircase13.apperta.api.errors;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ApiResponses({
        @ApiResponse(code = 405, message = ErrorConstants.HTTP_405_API_DESCRIPTION, response = ApiError.class),
        @ApiResponse(code = 500, message = ErrorConstants.HTTP_500_API_DESCRIPTION, response = ApiError.class)
})
public @interface CommonApiResponses {
}
