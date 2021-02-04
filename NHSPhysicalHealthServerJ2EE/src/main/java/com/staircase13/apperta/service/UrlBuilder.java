package com.staircase13.apperta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilder {

    private final String baseUrl;

    public UrlBuilder(@Value("${apperta.ui.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }


    public String resolveUiUrl(String relativePath, String queryParamName, String queryParamValue) {
        return String.format("%s%s?%s=%s",
            baseUrl,
                relativePath,
                queryParamName,
                queryParamValue);

    }

}
