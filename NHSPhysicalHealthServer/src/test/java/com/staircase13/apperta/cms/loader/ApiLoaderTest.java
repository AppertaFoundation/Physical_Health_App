package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.NhsApiContentFactory;
import com.staircase13.apperta.cms.entities.NhsApi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.staircase13.apperta.cms.CmsTestData.getValidConditionResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiLoaderTest {

    private static final String BASE_URL = "http://localhost:123/livewell";
    private static final String SUBSCRIPTION_KEY = "asubscriptionkey";
    private static final String TOPIC = "myTopic";
    private static final String TOPIC_URL = "http://localhost:123/livewell/myTopic";
    private static final String PAGE = "myPage";
    private static final String PAGE_URL = "http://localhost:123/livewell/myTopic/myPage";
    private static final NhsApi NHS_API = NhsApi.CONDITIONS;

    private static final NhsContentKey TOPIC_KEY = NhsContentKey.builder()
            .primaryEntityName(TOPIC)
            .secondaryEntityName(null)
            .build();

    private static final NhsContentKey PAGE_KEY = NhsContentKey.builder()
            .primaryEntityName(TOPIC)
            .secondaryEntityName(PAGE)
            .build();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ApiLoader apiLoader;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NhsApiContentFactory nhsApiContentFactory;

    @Mock
    private RestClientResponseException clientResponseException;

    @Mock
    private RestClientException restClientException;

    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor;

    @Before
    public void setupLoader() {

        apiLoader = ApiLoader
                .builder()
                .baseUrl(BASE_URL)
                .nhsApi(NhsApi.CONDITIONS)
                .subscriptionKey(SUBSCRIPTION_KEY)
                .nhsApiContentFactory(nhsApiContentFactory)
                .restTemplate(restTemplate)
                .build();

        ReflectionTestUtils.setField(apiLoader, "subscriptionKey", SUBSCRIPTION_KEY);
        ReflectionTestUtils.setField(apiLoader, "baseUrl", BASE_URL);
    }

    @Test
    public void successPathTopic() throws Exception {
        String json = getValidConditionResponse();

        when(restTemplate.exchange(eq(TOPIC_URL),eq(HttpMethod.GET),httpEntityArgumentCaptor.capture(),eq(String.class)))
                .thenReturn(new ResponseEntity<>(json, HttpStatus.OK));
        when(nhsApiContentFactory.forKeyAndContent(TOPIC_KEY,json)).thenReturn(mock(NhsApiContent.class));

        Optional<NhsApiContent> content = apiLoader.getApiContent(TOPIC_KEY);
        assertThat(content.isPresent(), is(true));

        assertThat(httpEntityArgumentCaptor.getValue().getHeaders().get("subscription-key").get(0), is(SUBSCRIPTION_KEY));
    }

    @Test
    public void successPathPage() throws Exception {
        String json = getValidConditionResponse();

        when(restTemplate.exchange(eq(PAGE_URL),eq(HttpMethod.GET),httpEntityArgumentCaptor.capture(),eq(String.class)))
                .thenReturn(new ResponseEntity<>(json, HttpStatus.OK));
        when(nhsApiContentFactory.forKeyAndContent(PAGE_KEY,json)).thenReturn(mock(NhsApiContent.class));

        Optional<NhsApiContent> content = apiLoader.getApiContent(PAGE_KEY);
        assertThat(content.isPresent(), is(true));

        assertThat(httpEntityArgumentCaptor.getValue().getHeaders().get("subscription-key").get(0), is(SUBSCRIPTION_KEY));
    }

    @Test
    public void notFoundReturnOptionalEmpty() throws Exception {
        when(clientResponseException.getRawStatusCode()).thenReturn(404);

        when(restTemplate.exchange(eq(TOPIC_URL),eq(HttpMethod.GET),httpEntityArgumentCaptor.capture(),eq(String.class)))
                .thenThrow(clientResponseException);

        Optional<NhsApiContent> content = apiLoader.getApiContent(TOPIC_KEY);
        assertThat(content.isPresent(), is(false));
    }

    @Test
    public void not404ErrorThrowException() throws Exception {
        when(clientResponseException.getRawStatusCode()).thenReturn(405);

        when(restTemplate.exchange(eq(TOPIC_URL),eq(HttpMethod.GET),httpEntityArgumentCaptor.capture(),eq(String.class)))
                .thenThrow(clientResponseException);

        expectedException.expect(NhsContentRetrievalException.class);

        apiLoader.getApiContent(TOPIC_KEY);
    }

    @Test
    public void restClientExceptionWrapInCustomException() throws Exception {
        when(restTemplate.exchange(eq(TOPIC_URL),eq(HttpMethod.GET),httpEntityArgumentCaptor.capture(),eq(String.class)))
                .thenThrow(restClientException);

        expectedException.expect(NhsContentRetrievalException.class);

        apiLoader.getApiContent(TOPIC_KEY);
    }
}
