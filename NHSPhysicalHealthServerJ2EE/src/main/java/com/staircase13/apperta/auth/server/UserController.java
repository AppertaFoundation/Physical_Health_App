package com.staircase13.apperta.auth.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/iam/user/me")
    public Principal user(Principal principal) {
        LOGGER.debug("Retrieve principle '{}'",principal.getName());
        return principal;
    }
}
