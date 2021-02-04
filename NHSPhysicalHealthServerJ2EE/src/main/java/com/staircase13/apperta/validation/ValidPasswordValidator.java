package com.staircase13.apperta.validation;

import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private PasswordValidator validator;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        initialize();
    }

    public void initialize() {
        CharacterCharacteristicsRule characterCharacteristicsRule = new CharacterCharacteristicsRule(2,
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.UpperCase, 1)
        );

        LengthRule lengthRule = new LengthRule(5,255);

        validator = new PasswordValidator(characterCharacteristicsRule, lengthRule);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(StringUtils.isEmpty(value)) {
            return false;
        }
        boolean valid = validator.validate(new PasswordData(value)).isValid();
        return valid;
    }
}
