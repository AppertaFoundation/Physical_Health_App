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

public class PasswordResetTokenVerifyRequestTest {

    private PasswordResetTokenVerifyRequest request;

    @Before
    public void setupValidRequest() {
        request = new PasswordResetTokenVerifyRequest();
        request.setToken("tokenValue");
    }

    @Test
    public void userNameRequired() {
        request.setToken(null);
        Set<ConstraintViolation<PasswordResetTokenVerifyRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, hasSize(1));
        assertThat(violations, hasItem(
                violationMatchers().propertyPath("token").message("must not be empty").build())
        );
    }
    @Test
    public void validRequest() {
        Set<ConstraintViolation<PasswordResetTokenVerifyRequest>> violations = ValidationUtil.validate(request);
        assertThat(violations, is(empty()));
    }

}
