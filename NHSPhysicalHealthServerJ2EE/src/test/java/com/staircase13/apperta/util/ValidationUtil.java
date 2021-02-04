package com.staircase13.apperta.util;

import org.hamcrest.Matcher;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class ValidationUtil {

    public static <T> Set<ConstraintViolation<T>> validate(T o) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(o);
    }

    public static ViolationMatchersBuilder violationMatchers() {
        return new ViolationMatchersBuilder();
    }

    public static class ViolationMatchersBuilder<T>  {

        private Optional<String> propertyPath = Optional.empty();
        private Optional<String> message = Optional.empty();

        public ViolationMatchersBuilder propertyPath(String propertyPath) {
            this.propertyPath = Optional.of(propertyPath);
            return this;
        }

        public ViolationMatchersBuilder message(String message) {
            this.message = Optional.of(message);
            return this;
        }

        public Matcher<ConstraintViolation<T>> build() {

            Set<Matcher<? super ConstraintViolation<T>>> matchers = new HashSet<>();

            if(propertyPath.isPresent()) {
                matchers.add(hasProperty("propertyPath",hasToString(propertyPath.get())));
            }

            if(message.isPresent()) {
                matchers.add(hasProperty("message",hasToString(message.get())));
            }

            return allOf(matchers);
        }

    }

}
