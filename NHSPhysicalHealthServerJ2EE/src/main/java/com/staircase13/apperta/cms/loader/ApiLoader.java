package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.CannotParseApiContentException;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.NhsApiContentFactory;
import com.staircase13.apperta.cms.entities.NhsApi;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Builder
public class ApiLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLoader.class);

    private final RestTemplate restTemplate;

    private final String subscriptionKey;

    private final String baseUrl;

    private final NhsApiContentFactory nhsApiContentFactory;

    private final NhsApi nhsApi;

    public ApiLoader(RestTemplate restTemplate,
                     String subscriptionKey,
                     String baseUrl,
                     NhsApiContentFactory nhsApiContentFactory,
                     NhsApi nhsApi) {
        this.restTemplate = restTemplate;
        this.subscriptionKey = subscriptionKey;
        this.baseUrl = baseUrl;
        this.nhsApiContentFactory = nhsApiContentFactory;
        this.nhsApi = nhsApi;
    }

    public Optional<NhsApiContent> getApiContent(NhsContentKey key) throws CannotParseApiContentException, NhsContentRetrievalException {

        String resolvedUrl = determineUrl(key);

        HttpHeaders headers = new HttpHeaders();
        headers.add("subscription-key",subscriptionKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(resolvedUrl, HttpMethod.GET, entity, String.class);
            return Optional.of(nhsApiContentFactory.forKeyAndContent(key, response.getBody()));
        } catch(RestClientResponseException e) {
            if (HttpStatus.NOT_FOUND.value() == e.getRawStatusCode()) {
                return Optional.empty();
            } else {
                throw new NhsContentRetrievalException("Could not retrieve content for '" + key + "', with response '" + e.getResponseBodyAsString() + "'");
            }
        } catch(RestClientException e) {
            LOGGER.error("Could not access " + nhsApi + "  API for key " + key,e);
            throw new NhsContentRetrievalException("Could not retrieve content from the " + nhsApi + " API due to a connection error",e);
        }
    }

    private String determineUrl(NhsContentKey key) {
        String topic = key.getPrimaryEntityName();
        Optional<String> page = Optional.ofNullable(key.getSecondaryEntityName());

        if(!page.isPresent()) {
            LOGGER.debug("Making request to {} API for topic '{}'",nhsApi,topic);
            return String.format("%s/%s", baseUrl, topic);
        } else {
            LOGGER.debug("Making request to {} API for topic '{}' and page '{}'",nhsApi, topic, page);
            return String.format("%s/%s/%s", baseUrl, topic, page.get());
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
