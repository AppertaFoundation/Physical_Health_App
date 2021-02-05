package com.staircase13.apperta.ui;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class MatchingPasswordsValidator implements ConstraintValidator<MatchingPasswords, PasswordResetRequest> {

    @Override
    public void initialize(MatchingPasswords constraint) {
    }

    @Override
    public boolean isValid(PasswordResetRequest form, ConstraintValidatorContext context) {
        return Objects.equals(form.getPassword(),form.getPasswordRepeated());
    }

}
