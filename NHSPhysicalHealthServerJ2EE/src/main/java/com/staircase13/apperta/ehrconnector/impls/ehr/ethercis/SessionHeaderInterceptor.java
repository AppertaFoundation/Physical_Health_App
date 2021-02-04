package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.EthercisRestConstants.SESSION_ID;

public class SessionHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final String sessionId;

    public SessionHeaderInterceptor(String sessionId){
        this.sessionId = sessionId;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add(SESSION_ID, sessionId);
        return execution.execute(request, body);
    }
}
