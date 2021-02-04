package com.staircase13.apperta.api.util;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingEndpoint {

    @GetMapping("/api/ping")
    public String ping() {
        return "pong";
    }

}
