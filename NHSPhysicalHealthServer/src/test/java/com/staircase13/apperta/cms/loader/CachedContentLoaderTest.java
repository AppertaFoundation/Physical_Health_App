package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.CmsTestData;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.NhsApiContentFactory;
import com.staircase13.apperta.cms.entities.CmsCache;
import com.staircase13.apperta.cms.entities.NhsApi;
import com.staircase13.apperta.cms.repository.CmsCacheRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.*;
import java.util.Optional;

import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRefreshThreshold;
import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRetentionThreshold;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class CachedContentLoaderTest {

    private static final NhsApi API = NhsApi.CONDITIONS;
    private static final String PRIMARY_CONTENT_NAME = "my-content";
    private static final String SECONDARY_CONTENT_NAME = "sub-content";
    private static final NhsContentKey KEY = NhsContentKey.builder()
            .nhsApi(API)
            .primaryEntityName(PRIMARY_CONTENT_NAME)
            .secondaryEntityName(SECONDARY_CONTENT_NAME)
            .build();
    private static final String URL_PREFIX = "http://url.prefix";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private CachedContentLoader cachedContentLoader;

    @Mock
    private CmsCacheRepository cmsCacheRepository;

    @Mock
    private NhsApiContentFactory nhsApiContentFactory;

    @Captor
    private ArgumentCaptor<CmsCache> cmsContentCacheArgumentCaptor;

    @Before
    public void setupClockAndDuration() {
        ReflectionTestUtils.setField(cachedContentLoader, "clock", Clock.fixed(Instant.parse("2018-03-12T20:00:00.00Z"), ZoneId.systemDefault()));
        ReflectionTestUtils.setField(cachedContentLoader, "refreshDuration", Duration.parse("PT6H"));
        ReflectionTestUtils.setField(cachedContentLoader, "retentionDuration", Duration.parse("PT12H"));
    }

    @Test
    public void content_not_available() throws Exception {
        when(cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(API,PRIMARY_CONTENT_NAME,SECONDARY_CONTENT_NAME)).thenReturn(Optional.empty());

        Optional<NhsApiContent> apiContent = cachedContentLoader.getApiContent(KEY, BeforeRefreshThreshold);

        assertThat(apiContent.isPresent(), is(false));
    }

    @Test
    public void content_available_before_refresh_threshold() throws Exception {
        CmsCache cachedContent = CmsCache
                .builder()
                .loaded(LocalDateTime.of(2018, 3, 12, 14,01))
                .content(getValidJsonContent())
                .build();

        when(cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(API,PRIMARY_CONTENT_NAME,SECONDARY_CONTENT_NAME)).thenReturn(Optional.of(cachedContent));
        when(nhsApiContentFactory.forKeyAndContent(KEY,getValidJsonContent())).thenReturn(mock(NhsApiContent.class));

        Optional<NhsApiContent> apiContent = cachedContentLoader.getApiContent(KEY, BeforeRefreshThreshold);

        assertThat(apiContent.isPresent(), is(true));
    }

    @Test
    public void content_available_before_retention_threshold() throws Exception {
        CmsCache cachedContent = CmsCache
                .builder()
                .loaded(LocalDateTime.of(2018, 3, 12, 8,01))
                .content(getValidJsonContent())
                .build();

        when(cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(API,PRIMARY_CONTENT_NAME,SECONDARY_CONTENT_NAME)).thenReturn(Optional.of(cachedContent));
        when(nhsApiContentFactory.forKeyAndContent(KEY,getValidJsonContent())).thenReturn(mock(NhsApiContent.class));

        Optional<NhsApiContent> apiContent = cachedContentLoader.getApiContent(KEY, BeforeRetentionThreshold);

        assertThat(apiContent.isPresent(), is(true));
    }

    @Test
    public void content_available_but_breached_refresh_threshold() throws Exception {
        CmsCache cachedContent = CmsCache
                .builder()
                .loaded(LocalDateTime.of(2018, 3, 12, 13,59))
                .content(IOUtils.toString(getClass().getResourceAsStream("/nhs-api/condition-response.json"), Charset.defaultCharset()))
                .build();

        when(cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(API,PRIMARY_CONTENT_NAME,SECONDARY_CONTENT_NAME)).thenReturn(Optional.of(cachedContent));

        Optional<NhsApiContent> apiContent = cachedContentLoader.getApiContent(KEY, BeforeRefreshThreshold);

        assertThat(apiContent.isPresent(), is(false));
    }

    @Test
    public void content_available_but_breached_retention_threshold() throws Exception {
        CmsCache cachedContent = CmsCache
                .builder()
                .loaded(LocalDateTime.of(2018, 3, 12, 7,59))
                .content(IOUtils.toString(getClass().getResourceAsStream("/nhs-api/condition-response.json"), Charset.defaultCharset()))
                .build();

        when(cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(API,PRIMARY_CONTENT_NAME,SECONDARY_CONTENT_NAME)).thenReturn(Optional.of(cachedContent));

        Optional<NhsApiContent> apiContent = cachedContentLoader.getApiContent(KEY, BeforeRetentionThreshold);

        assertThat(apiContent.isPresent(), is(false));
    }

    @Test
    public void update_cache_no_existing_entry() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(KEY,getValidJsonContent(), URL_PREFIX);

        when(cmsCacheRepository
                .getByApiAndPrimaryEntityNameAndSecondaryEntityName(
                        KEY.getNhsApi(),
                        KEY.getPrimaryEntityName(),
                        KEY.getSecondaryEntityName())).thenReturn(Optional.empty());

        cachedContentLoader.updateCache(nhsApiContent);

        verify(cmsCacheRepository).save(cmsContentCacheArgumentCaptor.capture());

        CmsCache cmsCache = cmsContentCacheArgumentCaptor.getValue();
        assertThat(cmsCache.getPrimaryEntityName(), is(PRIMARY_CONTENT_NAME));
        assertThat(cmsCache.getLoaded(), is(LocalDateTime.parse("2018-03-12T20:00:00")));
        assertThat(cmsCache.getContent(), is(getValidJsonContent()));
    }

    @Test
    public void update_cache_updated_existing_entry() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(KEY,getValidJsonContent(), URL_PREFIX);

        CmsCache existingCacheEntry = new CmsCache();
        when(cmsCacheRepository
                .getByApiAndPrimaryEntityNameAndSecondaryEntityName(
                        KEY.getNhsApi(),
                        KEY.getPrimaryEntityName(),
                        KEY.getSecondaryEntityName())).thenReturn(Optional.of(existingCacheEntry));

        cachedContentLoader.updateCache(nhsApiContent);

        verify(cmsCacheRepository).save(existingCacheEntry);

        assertThat(existingCacheEntry.getLoaded(), is(LocalDateTime.parse("2018-03-12T20:00:00")));
        assertThat(existingCacheEntry.getContent(), is(getValidJsonContent()));
    }

    private String getValidJsonContent() throws IOException {
        return CmsTestData.getValidConditionResponse();
    }
}
