package com.staircase13.apperta.ui;


import com.staircase13.apperta.util.ValidationUtil;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static com.staircase13.apperta.util.ValidationUtil.violationMatchers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;

public class PasswordResetRequestTest {

    private PasswordResetRequest request;

    @Before
    public void setupValidRequest() {
        request = new PasswordResetRequest();
        request.setPassword("newPassword");
        request.setPasswordRepeated("newPassword");
        request.setResetToken("theToken");
    }

    @Test
    public void tokenRequired() {
        request.setResetToken(null);
        Set<ConstraintViolation<PasswordResetRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().propertyPath("resetToken").message("must not be empty").build())
        );
    }

    @Test
    public void passwordMustBeValid() {
        request.setPassword("nouppercase");
        request.setPasswordRepeated("nouppercase");
        Set<ConstraintViolation<PasswordResetRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().propertyPath("password").message("Must be at least 5 characters long and a mix of upper and lower case letters").build())
        );
    }

    @Test
    public void passwordRepeatedRequired() {
        request.setPasswordRepeated(null);
        Set<ConstraintViolation<PasswordResetRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().message("Passwords must match").build())
        );
    }

    @Test
    public void passwordRepeatedMustMatchPassword() {
        request.setPassword("firstVersion");
        request.setPasswordRepeated("secondVersion");
        Set<ConstraintViolation<PasswordResetRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().message("Passwords must match").build())
        );
    }

    @Test
    public void validRequest() {
        Set<ConstraintViolation<PasswordResetRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, is(empty()));
    }

}
