package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.EthercisRestConstants.SESSION_ENDPOINT;

/**
 * Manages creating, maintaining and refreshing an EHR session
 *
 * Only used by {@link EthercisEhrConnector}
 *
 * The EhrSession is not Thread Safe. Instead, {@link EthercisEhrConnector} should provide thread safety
 */
public class EhrSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(EhrSession.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;

    private String sessionId;

    private SessionHeaderInterceptor sessionHeaderInterceptor = null;

    protected EhrSession(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    protected void startSession(String userName, String password) throws EhrOperationException, EhrAuthenticationException {

        LOGGER.debug("Starting session for '{}'", userName);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path(SESSION_ENDPOINT)
                .query("username={username}&password={password}")
                .buildAndExpand(userName, password);

        try {
            Session session = restTemplate.exchange(uri.toUri(), HttpMethod.POST, new HttpEntity<>(getBasicAuthHeaders(userName, password)), Session.class).getBody();

            if (!session.getAction().equalsIgnoreCase("create")){
                throw new EhrGenericClientException("Did not receive create action in start session");
            }
            sessionId = session.getSessionId();
            if (StringUtils.isNotBlank(sessionId)) {
                sessionHeaderInterceptor = new SessionHeaderInterceptor(sessionId);
                restTemplate.getInterceptors().add(sessionHeaderInterceptor);
            }
        } catch (RestClientResponseException rre) {
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrGenericClientException("Start session server issue: ", rre);
            }
            switch (rre.getRawStatusCode()) {
                case 401:
                case 403:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), userName);
                case 404:
                    throw new EhrGenericClientException("404: Operation Not Found");
                default:
                    throw new EhrOperationException("Start session response " + rre.getRawStatusCode() , rre);
            }
        }
        catch (RestClientException rce) {
            throw new EhrGenericClientException("startSession failed", rce);
        }
    }

    protected boolean refreshSession(String userName, String password) {
        LOGGER.debug("Refresh session for [{}]", userName);

        UriComponents uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path(SESSION_ENDPOINT).build();

        try {
            restTemplate.exchange(uri.toUri(), HttpMethod.PUT, new HttpEntity<>(getBasicAuthHeaders(userName, password)), Void.class).getBody();

        } catch (RestClientException rce) {
            sessionId = null;
            return false;
        }

        return true;
    }

    protected void endSession() {
        endSession(true);
    }

    protected void endSession(boolean deleteRemoteSession) {

        LOGGER.debug("End session");

        if (sessionHeaderInterceptor == null) {
            LOGGER.debug("No remote session information, doing nothing");
            return;
        }

        try {
            LOGGER.debug("Deleting remote session");

            if(deleteRemoteSession) {
                restTemplate.delete(baseUrl + SESSION_ENDPOINT);
            }

        } catch (RestClientException rce){
            LOGGER.warn("An Error has occurred deleting the remote session. The session will be removed locally regardless",rce);
        } finally {
            restTemplate.getInterceptors().remove(sessionHeaderInterceptor);
            sessionHeaderInterceptor = null;
            sessionId = null;
        }
    }

    protected boolean hasSession() {
        return sessionHeaderInterceptor != null;
    }

    protected Optional<String> getSessionId() {
        return Optional.ofNullable(sessionId);
    }

    private HttpHeaders getBasicAuthHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(userName, password);
        return headers;
    }

}
