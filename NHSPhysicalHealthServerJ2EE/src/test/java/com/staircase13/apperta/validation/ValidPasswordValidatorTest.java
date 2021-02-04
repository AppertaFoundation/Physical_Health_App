package com.staircase13.apperta.validation;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ValidPasswordValidatorTest {


    private ValidPasswordValidator validator;

    @Before
    public void setup() {
        validator = new ValidPasswordValidator();
        validator.initialize();
    }

    @Test
    public void length_at_least_5() {
        assertInvalidPassword("a");
        assertInvalidPassword("aB");
        assertInvalidPassword("aBc");
        assertInvalidPassword("aBcd");
        assertValidPassword("abCde");
    }

    @Test
    public void mix_of_lower_and_upper() {
        assertInvalidPassword("abcdef");
        assertInvalidPassword("ABCDEF");
        assertValidPassword("abCdef");
    }

    private void assertInvalidPassword(String password) {
        assertThat(validator.isValid(password, null), is(false));
    }

    private void assertValidPassword(String password) {
        assertThat(validator.isValid(password, null), is(true));
    }

}
