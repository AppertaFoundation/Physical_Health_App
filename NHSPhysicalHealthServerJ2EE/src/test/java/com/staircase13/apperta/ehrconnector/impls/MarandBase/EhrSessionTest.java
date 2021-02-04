package com.staircase13.apperta.ehrconnector.impls.MarandBase;

import com.staircase13.apperta.ehrconnector.TestConstants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class EhrSessionTest {

    private EhrSession ehrSession;

    private RestTemplate restTemplate;

    private List<ClientHttpRequestInterceptor> restTemplateInterceptors;

    @Before
    public void setup() {
        restTemplate = mock(RestTemplate.class);
        ehrSession = new EhrSession(restTemplate, TestConstants.EHR_URL);

        restTemplateInterceptors = new ArrayList<>();
    }

    @Test
    public void startSession_success() throws Exception {

//        Session session = Session.builder().action("CREATE").meta(new EhrBaseResponse.Meta("href",null)).sessionId(TestConstants.SESSION_ID).build();
//
//        String url = TestConstants.EHR_URL + SESSION_ENDPOINT + "?username="+ TestConstants.TEST_USERNAME +"&password=" + TestConstants.TEST_PASSWORD;
//        when(restTemplate.exchange(eq(URI.create(url)), eq(HttpMethod.POST), any(), eq(Session.class))).thenReturn(new ResponseEntity(session, HttpStatus.OK));
        when(restTemplate.getInterceptors()).thenReturn(restTemplateInterceptors);

        ehrSession.startSession(TestConstants.TEST_USERNAME, TestConstants.TEST_PASSWORD);

        // verify correct call made to get session
        assert(restTemplateInterceptors.size() == 1);

        assert(ehrSession.hasSession());

        // Ensure session adds correct header
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request  = new HttpRequest() {
            @Override
            public String getMethodValue() {
                return null;
            }

            @Override
            public URI getURI() {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };

        ClientHttpRequestExecution execution = (httpRequest, bytes) -> {
            assertThat(bytes, nullValue());
            List<String> authenticationHeaders = httpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION);
            assertThat(authenticationHeaders.size(), is(1));
            return null;
        };

        try {
            restTemplateInterceptors.get(0).intercept(request, null, execution);
        } catch (IOException ioe) {}
    }

    @Test
    public void endSession_noSessionInitialised_doNothing() {
        ehrSession.endSession();

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void endSession_deleteRemote_success() throws Exception {

        startSession_success();

        ehrSession.endSession();

        assertThat(restTemplateInterceptors, empty());
        assertThat(ehrSession.hasSession(), is(false));
    }

}
