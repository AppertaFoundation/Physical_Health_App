package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.CannotParseApiContentException;
import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.LoadLogAsserter;
import com.staircase13.apperta.cms.NhsApiContent;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Optional;

import static com.staircase13.apperta.cms.LoadLog.Severity.ERROR;
import static com.staircase13.apperta.cms.LoadLog.Severity.INFO;
import static com.staircase13.apperta.cms.entities.NhsApi.CONDITIONS;
import static com.staircase13.apperta.cms.entities.NhsApi.LIVE_WELL;
import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRefreshThreshold;
import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRetentionThreshold;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class CmsContentLoaderTest {
    private static final String CONTENT_NAME = "my-content";

    private static final NhsContentKey CONDITIONS_KEY = NhsContentKey.builder()
            .nhsApi(CONDITIONS)
            .primaryEntityName(CONTENT_NAME)
            .build();

    private static final NhsContentKey LIVEWELL_KEY = NhsContentKey.builder()
            .nhsApi(LIVE_WELL)
            .primaryEntityName(CONTENT_NAME)
            .secondaryEntityName("page1")
            .build();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private CmsContentLoader cmsContentLoader;

    @Mock
    private CachedContentLoader cachedContentLoader;

    @Mock
    private ApiLoader conditionsApiLoader;

    @Mock
    private ApiLoader liveWellApiLoader;

    @Mock
    private NhsApiContent cachedNhsApiContent;

    @Mock
    private NhsApiContent nhsApiContent;

    private LoadLog loadLog;

    @Before
    public void setupContentLoader() {
        cmsContentLoader = new CmsContentLoader(
                cachedContentLoader,
                conditionsApiLoader,
                liveWellApiLoader);
    }

    @Before
    public void setupLoadLog() {
        loadLog = new LoadLog();
    }

    @Test
    public void use_cached_content_if_available_before_refresh_threshold() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(of(cachedNhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog).get();

        assertThat(nhsApiContent, Is.is(cachedNhsApiContent));
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from cache");

        verifyZeroInteractions(conditionsApiLoader);
    }

    @Test
    public void use_conditions_loader_if_fresh_cached_content_not_available() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(of(this.nhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog).get();
        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API and added to the cache");

        assertThat(nhsApiContent, Is.is(this.nhsApiContent));
    }

    @Test
    public void use_livewell_loader_if_fresh_cached_content_not_available() throws Exception {
        when(cachedContentLoader.getApiContent(LIVEWELL_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(liveWellApiLoader.getApiContent(LIVEWELL_KEY)).thenReturn(of(this.nhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(LIVEWELL_KEY,loadLog).get();
        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=LIVE_WELL, primaryEntityName=my-content, secondaryEntityName=page1)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=LIVE_WELL, primaryEntityName=my-content, secondaryEntityName=page1)' from the API and added to the cache");

        assertThat(nhsApiContent, Is.is(this.nhsApiContent));
    }

    @Test
    public void use_loader_if_fresh_cached_content_cannot_be_parsed() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenThrow(new CannotParseApiContentException("oops", new IOException()));
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(of(this.nhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog).get();
        assertLogAdded(ERROR,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' is invalid/couldn't be parsed. Will refresh");
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API and added to the cache");

        assertThat(nhsApiContent, Is.is(this.nhsApiContent));
    }

    @Test
    public void update_cache_if_loaded_from_nhs() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(of(nhsApiContent));

        cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog);

        verify(cachedContentLoader).updateCache(nhsApiContent);
    }

    @Test
    public void use_cached_content_before_retention_threshold_if_api_fails() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(Optional.empty());
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRetentionThreshold)).thenReturn(of(cachedNhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog).get();

        assertThat(nhsApiContent, Is.is(cachedNhsApiContent));
        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(ERROR,"Loading 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API failed. Will check cache to see if content is available and hasn't hit retention threshold");
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from cache, despite it needing a refresh");
    }

    @Test
    public void use_cached_content_before_retention_threshold_if_api_content_cannot_be_parsed() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenThrow(new CannotParseApiContentException("oops", new IOException()));
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRetentionThreshold)).thenReturn(of(cachedNhsApiContent));

        NhsApiContent nhsApiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog).get();

        assertThat(nhsApiContent, Is.is(cachedNhsApiContent));
        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(ERROR, "Content retrieved from API for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' is invalid/couldn't be parsed");
        assertLogAdded(ERROR,"Loading 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API failed. Will check cache to see if content is available and hasn't hit retention threshold");
        assertLogAdded(INFO,"Loaded 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from cache, despite it needing a refresh");
    }

    @Test
    public void not_available_in_cache_or_from_api() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(Optional.empty());
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRetentionThreshold)).thenReturn(Optional.empty());

        Optional<NhsApiContent> apiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog);
        assertThat(apiContent.isPresent(), is(false));

        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(ERROR,"Loading 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API failed. Will check cache to see if content is available and hasn't hit retention threshold");
        assertLogAdded(ERROR,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' not available or has breached retention threshold");
    }

    @Test
    public void not_available_from_api_and_cache_version_cant_be_parsed() throws Exception {
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRefreshThreshold)).thenReturn(Optional.empty());
        when(conditionsApiLoader.getApiContent(CONDITIONS_KEY)).thenReturn(Optional.empty());
        when(cachedContentLoader.getApiContent(CONDITIONS_KEY, BeforeRetentionThreshold)).thenThrow(new CannotParseApiContentException("oops", new IOException()));

        Optional<NhsApiContent> apiContent = cmsContentLoader.getApiContent(CONDITIONS_KEY,loadLog);
        assertThat(apiContent.isPresent(), is(false));
        assertLogAdded(INFO,"Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' doesn't exist or has breached refresh threshold. Will refresh");
        assertLogAdded(ERROR,"Loading 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' from the API failed. Will check cache to see if content is available and hasn't hit retention threshold");
        assertLogAdded(ERROR, "Cached content for 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=my-content, secondaryEntityName=null)' is invalid/couldn't be parsed");
    }

    private void assertLogAdded(LoadLog.Severity severity, String message) {
        LoadLogAsserter.assertLogAdded(loadLog, severity, message);
    }

}
