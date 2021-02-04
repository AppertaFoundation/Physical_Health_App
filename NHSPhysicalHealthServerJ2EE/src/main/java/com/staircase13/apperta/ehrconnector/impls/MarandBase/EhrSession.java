package com.staircase13.apperta.ehrconnector.impls.MarandBase;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;


/**
 * Manages creating, maintaining and refreshing an EHR session
 *
 * Only used by {@link BaseConnector}
 *
 * The EhrSession is not Thread Safe. Instead, {@link BaseConnector} should provide thread safety
 */
public class EhrSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(EhrSession.class);

    private final RestTemplate restTemplate;

    private final String baseUrl;

    private BasicAuthenticationInterceptor authenticationInterceptor = null;

    protected EhrSession(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    protected void startSession(String userName, String password) throws EhrOperationException, EhrAuthenticationException {

        LOGGER.debug("Starting session for '{}'", userName);

        if (authenticationInterceptor == null) {
            authenticationInterceptor = new BasicAuthenticationInterceptor(userName, password);

            restTemplate.getInterceptors().add(authenticationInterceptor);

        } else {
            // handle attempted re-init
            LOGGER.info("Attempting to re-init session");
            restTemplate.getInterceptors().remove(authenticationInterceptor);
            restTemplate.getInterceptors().add(authenticationInterceptor);
        }
    }

    protected boolean refreshSession(String userName, String password) {
        return true;
    }

    protected void endSession() {
        if (authenticationInterceptor == null) {
            LOGGER.debug("No remote session information, doing nothing");
            return;
        }

        LOGGER.debug("Deleting session");
        restTemplate.getInterceptors().remove(authenticationInterceptor);
        authenticationInterceptor = null;

    }

    protected void endSession(boolean deleteRemoteSession) {

        LOGGER.debug("End session");

        // No remote session to end in this instance
        endSession();
    }

    protected boolean hasSession() {
        return authenticationInterceptor != null;
    }


//    private HttpHeaders getBasicAuthHeaders(String userName, String password) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBasicAuth(userName, password);
//        return headers;
//    }

}
