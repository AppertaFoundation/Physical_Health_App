package com.staircase13.apperta.integration.util;

import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.EhrConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import javax.annotation.PostConstruct;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Component
public class MockEhrServer {

    @Autowired
    private EhrConnector ehrConnector;

    @Value("${apperta.ehr.baseurl}")
    private String ehrUrl;

    @Value("${apperta.ehr.user.username}")
    private String ehrUsername;

    @Value("${apperta.ehr.user.token}")
    private String ehrPassword;

    private MockRestServiceServer mockServer;

    @PostConstruct
    public void setUp() {
        mockServer = MockServerFactory.createMockServer(ehrConnector.getUnderlyingRestTemplate());

        // TODO: these session calls are only required for specific connectors
//        ensureHealthCheckSystemUserCheckAlwaysSuccessful();
        ensureHealthCheckRetrieveTemplatesAlwaysSuccessful();
    }

    private void ensureHealthCheckSystemUserCheckAlwaysSuccessful() {
        String userCheckUrl = ehrUrl + "/rest/v1/session?username=" + ehrUsername + "&password=" + ehrPassword;

        mockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(userCheckUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{ \"action\": \"CREATE\", " +
                        " \"sessionId\": \"8116d917-4a4b-4543-aa95-9257d7aae17d\" }", MediaType.APPLICATION_JSON));
    }

    private void ensureHealthCheckRetrieveTemplatesAlwaysSuccessful() {
        mockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(ehrUrl + "/rest/v1/template"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{ \"templates\": [] }", MediaType.APPLICATION_JSON));
    }

    public void expectUserSearch() {
        mockServer.expect(
                requestTo(ehrUrl + "/rest/v1/ehr?subjectId=myUserName&subjectNamespace=apperta.user"))
                .andRespond(withSuccess("{ \"action\": \"RETRIEVE\"," +
                        "    \"ehrStatus\": {" +
                        "        \"subjectId\": \"myUserName\", " +
                        "        \"subjectNamespace\": \"apperta.user\", " +
                        "        \"queryable\": true, " +
                        "        \"modifiable\": true " +
                        "    }, " +
                        "    \"ehrId\": \"e4ffdba0-db82-4ede-9bba-385bc1bdc149\"}", MediaType.APPLICATION_JSON));
    }
}
