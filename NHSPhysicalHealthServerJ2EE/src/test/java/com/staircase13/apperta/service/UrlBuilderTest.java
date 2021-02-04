package com.staircase13.apperta.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlBuilderTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private UrlBuilder urlBuilder;

    @Before
    public void setupUrl() {
        ReflectionTestUtils.setField(urlBuilder, "baseUrl", "http://baseUrl:8080");
    }

    @Test
    public void resolveUiUrl() {
        String url = urlBuilder.resolveUiUrl("/my/path","name","jeff");
        assertThat(url, is("http://baseUrl:8080/my/path?name=jeff"));
    }

}
