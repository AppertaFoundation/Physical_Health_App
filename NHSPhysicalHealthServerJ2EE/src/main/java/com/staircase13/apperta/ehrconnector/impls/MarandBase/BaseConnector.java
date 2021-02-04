package com.staircase13.apperta.ehrconnector.impls.MarandBase;

import com.google.common.annotations.VisibleForTesting;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseConnector {
    private static final Logger LOG = LoggerFactory.getLogger(BaseConnector.class);

    protected final Environment environment;

    protected final RestTemplate restTemplate;

    /** The base url for all EHR endpoints */
    protected String baseEhrUrl;

    /** The openEHR user which will be used to connect to the EHR for all basic enquiries */
    protected String ehrUser;

    /** The openEHR user password which will be used to connect to the EHR for all basic enquiries */
    protected String ehrPassword;

    /** The current openEHR session */
    private EhrSession currentSession;

    private boolean inSession = false;

    public BaseConnector(Environment environment, RestTemplate restTemplate) {
        this.environment = environment;
        this.restTemplate = restTemplate;
    }

    public abstract void setupConnectionProperties();

    public boolean checkSession() {
        return currentSession != null && currentSession.hasSession();
    }

    public synchronized void createEhrSession() throws EhrOperationException, EhrAuthenticationException {
        if (currentSession == null) {
            currentSession = new EhrSession(restTemplate, baseEhrUrl);
            currentSession.startSession(ehrUser, ehrPassword);
        } else {
            if (!currentSession.refreshSession(ehrUser, ehrPassword)) {
                currentSession.endSession();
                currentSession.startSession(ehrUser, ehrPassword);
            }
        }
        inSession = true;
    }

    public synchronized void closeEhrSession(boolean deleteRemoteSession) {
        LOG.debug("Closing EHR Session");
        if (currentSession != null) {
            currentSession.endSession(deleteRemoteSession);
            currentSession = null;
        }
        inSession = false;
    }

    public String getSessionId() {
        return ehrUser;
    }

    public RestTemplate getSessionRestTemplate() throws EhrOperationException, EhrAuthenticationException  {
        if (!inSession) {
            createEhrSession();
        }

        if (inSession) {
            return restTemplate;
        }
        throw new EhrGenericClientException("Could not get a valid session");
    }

    public void resetSessionRestTemplate() throws EhrOperationException, EhrAuthenticationException {
        // Force recreate/refresh session
        createEhrSession();

        if (!inSession) {
            throw new EhrAuthenticationException(401, ehrUser);
        }
    }

    public UriComponentsBuilder getSessionUriBuilder(){
        return UriComponentsBuilder
                .fromHttpUrl(baseEhrUrl);
    }

    @VisibleForTesting
    public RestTemplate getUnderlyingRestTemplate() {
        return restTemplate;
    }
}
