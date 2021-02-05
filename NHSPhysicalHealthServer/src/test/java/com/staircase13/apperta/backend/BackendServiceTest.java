package com.staircase13.apperta.backend;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.Mockito.verify;

public class BackendServiceTest {

    private static final String BACKEND_URL = "http://backend-service";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private BackendService backendService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setupBackendUrl() {
        ReflectionTestUtils.setField(backendService, "backendUrl", BACKEND_URL);

    }

    @Test
    public void getUserInfo_successPath() {
        backendService.getUserInfo("myUsername");

        verify(restTemplate).getForObject(BACKEND_URL + "/user?username={username}", Map.class, "myUsername");
    }

}
