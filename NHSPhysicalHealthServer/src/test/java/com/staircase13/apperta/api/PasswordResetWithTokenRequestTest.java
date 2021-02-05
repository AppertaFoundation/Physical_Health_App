package com.staircase13.apperta.api;

import com.staircase13.apperta.util.ValidationUtil;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static com.staircase13.apperta.util.ValidationUtil.violationMatchers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;

public class PasswordResetWithTokenRequestTest {

    private PasswordResetWithTokenRequest request;

    @Before
    public void setupValidRequest() {
        request = new PasswordResetWithTokenRequest();
        request.setPassword("newPassword");
        request.setToken("theToken");
    }

    @Test
    public void tokenRequired() {
        request.setToken(null);
        Set<ConstraintViolation<PasswordResetWithTokenRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().propertyPath("token").message("must not be empty").build())
        );
    }

    @Test
    public void passwordMustBeValid() {
        request.setPassword("nouppercase");
        Set<ConstraintViolation<PasswordResetWithTokenRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().propertyPath("password").message("Must be at least 5 characters long and a mix of upper and lower case letters").build())
        );
    }

    @Test
    public void validRequest() {
        Set<ConstraintViolation<PasswordResetWithTokenRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, is(empty()));
    }

}
