package com.staircase13.apperta.ui;

import com.staircase13.apperta.auth.server.PasswordService;
import com.staircase13.apperta.service.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
@RequestMapping("/iam")
public class PasswordResetController {

    private static final String VIEW_PASSWORD_RESET = "passwordReset";
    private static final String VIEW_RESET_COMPLETE = "passwordResetComplete";
    private static final String VIEW_TOKEN_EXPIRED = "passwordTokenExpired";

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordService passwordService;

    @Autowired
    public PasswordResetController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/passwordReset")
    public ModelAndView passwordResetGet(@RequestParam(name="token") String token) {

        LOGGER.debug("Show password reset form for token '{}'",token);

        if(!passwordService.isValidToken(token)) {
            return new ModelAndView(VIEW_TOKEN_EXPIRED);
        }

        PasswordResetRequest request = new PasswordResetRequest();
        request.setResetToken(token);

        return new ModelAndView(VIEW_PASSWORD_RESET, "passwordResetRequest", request);
    }

    @PostMapping("/passwordReset")
    public ModelAndView passwordResetPost(@Valid @ModelAttribute PasswordResetRequest request,
                                    BindingResult result) {
        LOGGER.debug("Password reset requested for token '{}'",request.getResetToken());

        if (result.hasErrors()) {
            LOGGER.debug("Form has errors");
            return new ModelAndView(VIEW_PASSWORD_RESET, "passwordResetRequest", request);
        }

        try {
            passwordService.resetPasswordWithToken(request.getResetToken(), request.getPassword());
        } catch (InvalidTokenException e) {
            return new ModelAndView(VIEW_TOKEN_EXPIRED);
        }

        return new ModelAndView(VIEW_RESET_COMPLETE);
    }

}
