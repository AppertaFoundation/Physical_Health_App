package com.staircase13.apperta.ui;

import com.staircase13.apperta.auth.server.PasswordService;
import com.staircase13.apperta.service.exception.InvalidTokenException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class PasswordResetControllerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private PasswordResetController passwordResetController;

    @Mock
    private PasswordService passwordService;

    @Mock
    private BindingResult bindingResult;

    @Test
    public void passwordResetGet_invalidToken() {
        when(passwordService.isValidToken("invalidToken")).thenReturn(false);

        ModelAndView modelAndView = passwordResetController.passwordResetGet("invalidToken");
        assertThat(modelAndView.getViewName(), is("passwordTokenExpired"));
    }

    @Test
    public void passwordResetGet_success() {
        when(passwordService.isValidToken("validToken")).thenReturn(true);

        ModelAndView modelAndView = passwordResetController.passwordResetGet("validToken");
        assertThat(modelAndView.getViewName(), is("passwordReset"));
    }

    @Test
    public void passwordResetPost_validationError() {
        when(bindingResult.hasErrors()).thenReturn(true);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest
                .builder()
                .resetToken("validToken")
                .password("newPassword")
                .build();

        ModelAndView modelAndView = passwordResetController.passwordResetPost(passwordResetRequest, bindingResult);
        assertThat(modelAndView.getViewName(), is("passwordReset"));
    }

    @Test
    public void passwordResetPost_invalidToken() throws Exception {
        doThrow(InvalidTokenException.class).when(passwordService).resetPasswordWithToken("validToken", "newPassword");

        PasswordResetRequest passwordResetRequest = PasswordResetRequest
                .builder()
                .resetToken("validToken")
                .password("newPassword")
                .build();

        ModelAndView modelAndView = passwordResetController.passwordResetPost(passwordResetRequest, bindingResult);
        assertThat(modelAndView.getViewName(), is("passwordTokenExpired"));
    }

    @Test
    public void passwordResetPost_success() {
        when(bindingResult.hasErrors()).thenReturn(false);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest
                .builder()
                .resetToken("validToken")
                .password("newPassword")
                .build();

        ModelAndView modelAndView = passwordResetController.passwordResetPost(passwordResetRequest, bindingResult);
        assertThat(modelAndView.getViewName(), is("passwordResetComplete"));
    }

}
