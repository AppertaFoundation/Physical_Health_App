package com.staircase13.apperta.api;

import com.staircase13.apperta.api.errors.CommonApiResponses;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "App", description = "Client Application server configuration, designed for use by client microservices.")
@RestController
@RequestMapping("/api/user/hcp")
@CommonApiResponses

public class AppEndpoint {

    // TODO:  App endpoints will be secured by client auth
// MODIFY_APP


    // Register app

    // unregister app (remove all assets)


    // Add template(s) to app

    // Add querytemplate to app
    //  - includes adding parameters

    // Add parameters (for composition or query) to app
}
