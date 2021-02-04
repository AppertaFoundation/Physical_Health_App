package com.staircase13.apperta.integration.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TestUrlBuilder {

    @Autowired
    private Environment environment;

    public String createUrl(String path, Object... args) {
        return String.format("http://localhost:%s%s",environment.getProperty("local.server.port"),String.format(path,args));
    }

    public UriComponentsBuilder createBuilder() {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(environment.getProperty("local.server.port",Integer.class));
    }

}
