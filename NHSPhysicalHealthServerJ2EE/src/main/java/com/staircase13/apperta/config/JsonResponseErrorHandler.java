package com.staircase13.apperta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

import static org.springframework.http.HttpStatus.NOT_FOUND;


public class JsonResponseErrorHandler implements ResponseErrorHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return  httpResponse.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {
// TODO: decide if this is a good approach
//        if (httpResponse.getStatusCode().is5xxServerError()) {
//            throw new RestClientResponseException("", httpResponse.getRawStatusCode(), httpResponse.getStatusText(), httpResponse.getHeaders(), httpResponse.getBody())
//        } else if (httpResponse.getStatusCode().is4xxClientError()) {
//
//
//
//            if (httpResponse.getStatusCode() == NOT_FOUND) {
//            //    throw new NotFoundException();
//            }
//        }
    }

}
