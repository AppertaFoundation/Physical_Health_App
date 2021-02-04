package com.staircase13.apperta.cms.integration;

import com.google.common.collect.Sets;
import com.staircase13.apperta.cms.CmsTestData;
import com.staircase13.apperta.cms.TarReader;
import com.staircase13.apperta.cms.entities.CmsCache;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.repository.CmsCacheRepository;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import com.staircase13.apperta.integration.AbstractIntegrationTest;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.staircase13.apperta.cms.entities.NhsApi.CONDITIONS;
import static com.staircase13.apperta.cms.entities.NhsApi.LIVE_WELL;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;

@TestPropertySource(properties = "apperta.cms.tar.rebuild.schedule.cron=*/2 * * * * *")
public class CmsTarConstructionIT extends AbstractIntegrationTest {

    private static final String CONDITION1 = "condition1";
    private static final String CONDITION2 = "condition2";
    private static final String CONDITION2_PAGE1 = "condition2page1";
    private static final String TOPIC1 = "topic1";
    private static final String TOPIC1_PAGE1 = "topic1Page1";

    private static final String DOWNLOAD_SUBPATH = "api/cms/tar";
    private static final String LAST_MODIFIED_SUBPATH = "api/cms/tar/lastModified";

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsTarConstructionIT.class);

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsCacheRepository cmsCacheRepository;

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Autowired
    private MockNhsApiServer mockNhsApiServer;

    @Value("${apperta.cms.tar.local.file}")
    private Path tarFile;

    @Autowired
    private Clock clock;

    @Before
    public void resetMocks() {
        mockNhsApiServer.reset();
    }

    @Before
    @After
    public void removeTar() throws IOException {
        try {
            Files.deleteIfExists(tarFile);
        } catch(AccessDeniedException e) {
            // this sometimes occurs on Windows, safe to ignore
            e.printStackTrace();
        }
    }

    @Test
    public void build_tar_using_caches_only() throws Exception {
        prePopulateCaches();
        setupCmsConfig();
        assertTarContent();
    }

    @Test
    public void build_tar_using_api_then_use_cached_content() throws Exception {
        setupCmsConfig();

        String sampleContent = CmsTestData.getValidConditionResponse();

        mockNhsApiServer.addMockConditionContent(CONDITION1,sampleContent);
        mockNhsApiServer.addMockConditionContent(CONDITION2,CONDITION2_PAGE1,sampleContent);
        mockNhsApiServer.addMockLiveWellContent(TOPIC1,sampleContent);
        mockNhsApiServer.addMockLiveWellContent(TOPIC1,TOPIC1_PAGE1,sampleContent);

        assertTarContent();

        mockNhsApiServer.refuseAllFutureRequests();

        assertTarContent();
    }

    @Test
    public void cache_refreshed_if_expired() throws Exception {
        prePopulateCaches();
        setupCmsConfig();
        assertTarContent();

        CmsCache cacheEntry = cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(CONDITIONS, CONDITION1, null).get();
        LOGGER.info("Initial Cache Entry loaded {}",cacheEntry.getLoaded());
        // reload threshold is set to 2 days, so we set the loaded date to just outside this threshold
        cacheEntry.setLoaded(cacheEntry.getLoaded().minus(Duration.parse("P2DT1M")));
        LOGGER.info("Updated Initial Cache Entry loaded to {}",cacheEntry.getLoaded());
        cmsCacheRepository.save(cacheEntry);

        mockNhsApiServer.addMockConditionContent(CONDITION1,CmsTestData.getValidConditionResponse());
        LOGGER.info("Re-retrieving the TAR");
        assertTarContent();

        CmsCache updatedCacheEntry = cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(CONDITIONS, CONDITION1, null).get();
        updatedCacheEntry.getLoaded();

    }

    @Test
    public void last_modified() throws Exception {
        prePopulateCaches();
        setupCmsConfig();

        assertTarContent();

        ResponseEntity<Long> lastModifiedResponse = callLastModifiedEndpoint();

        assertThat(lastModifiedResponse.getBody(), lessThanOrEqualTo(System.currentTimeMillis() + 1000));
    }

    @Test
    public void no_tar_503() throws Exception {
        ResponseEntity<byte[]> downloadResponse = callDownloadEndpoint();
        assertThat(downloadResponse.getStatusCode(), is(HttpStatus.SERVICE_UNAVAILABLE));

        ResponseEntity<Long> lastModifiedResponse = callLastModifiedEndpoint();
        assertThat(lastModifiedResponse.getStatusCode(), is(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private void assertTarContent() throws Exception {
        // Cron runs every 2 seconds, so this should be sufficient
        Thread.sleep(Duration.ofSeconds(2).toMillis());

        Path tarFile = downloadTarViaApi();

        Map<String,String> files = TarReader.extractFilesFromTarGz(tarFile);
        assertThat(files.size(), is(2));
        assertThat(files, hasKey("page-1.html"));
        assertThat(files, hasKey("page-2.html"));
    }

    private void setupCmsConfig() {
        addPageConfiguration("page-1",
                "page-1-links",
                newLinkedHashSet(
                    CmsPageFragment.builder()
                            .api(CONDITIONS)
                            .primaryEntityName(CONDITION1)
                            .jsonSectionHeading("One Main Entity, No Children")
                            .jsonMainEntityPosition(0).build(),
                    CmsPageFragment.builder()
                            .api(CONDITIONS)
                            .primaryEntityName(CONDITION2)
                            .secondaryEntityName(CONDITION2_PAGE1)
                            .jsonSectionHeading("One Main Entity, No Children")
                            .jsonMainEntityPosition(1).build(),
                    CmsPageFragment.builder()
                            .api(LIVE_WELL)
                            .primaryEntityName(TOPIC1)
                            .jsonSectionHeading("One Main Entity, No Children")
                            .jsonMainEntityPosition(1).build()
                ),
                newLinkedHashSet(
                   CmsPageLink.builder()
                            .url("http://localhost")
                            .label("localhost")
                            .description("This is localhost")
                            .build()
                )
        );

        addPageConfiguration("page-2",
                "page-2-links",
                newLinkedHashSet(
                    CmsPageFragment.builder()
                            .api(LIVE_WELL)
                            .primaryEntityName(TOPIC1)
                            .secondaryEntityName(TOPIC1_PAGE1)
                            .jsonSectionHeading("One Main Entity, No Children")
                            .jsonMainEntityPosition(0)
                            .build()
                ),
                newLinkedHashSet()
        );
    }

    private void prePopulateCaches() throws IOException {
        LocalDateTime now = LocalDateTime.now(clock);

        String sampleContent = CmsTestData.getValidConditionResponse();

        cmsCacheRepository.save(CmsCache.builder()
                .api(CONDITIONS)
                .primaryEntityName(CONDITION1)
                .loaded(now)
                .content(sampleContent).build());

        cmsCacheRepository.save(CmsCache.builder()
                .api(CONDITIONS)
                .primaryEntityName(CONDITION2)
                .secondaryEntityName(CONDITION2_PAGE1)
                .loaded(now)
                .content(sampleContent).build());

        cmsCacheRepository.save(CmsCache.builder()
                .api(LIVE_WELL)
                .primaryEntityName(TOPIC1)
                .loaded(now)
                .content(sampleContent).build());

        cmsCacheRepository.save(CmsCache.builder()
                .api(LIVE_WELL)
                .primaryEntityName(TOPIC1)
                .secondaryEntityName(TOPIC1_PAGE1)
                .loaded(now)
                .content(sampleContent).build());
    }

    private Path downloadTarViaApi() throws Exception {
        ResponseEntity<byte[]> response = callDownloadEndpoint();
        Path tempFile = Files.createTempFile("cms",".tar.gz");
        Files.write(tempFile, response.getBody());

        LOGGER.debug("Written TAR to temp file '{}'",tempFile);

        return tempFile;
    }

    private void addPageConfiguration(String pageName, String linksHeader, Set<CmsPageFragment> fragments, Set<CmsPageLink> links) {
        CmsPage page = cmsPageRepository.save(CmsPage.builder().name(pageName).linksHeader(linksHeader).build());
        fragments.stream().forEach(f -> f.setCmsPage(page));
        links.stream().forEach(f -> f.setCmsPage(page));
        page.setFragments(fragments);
        page.setLinks(links);
        cmsPageRepository.save(page);
    }

    private ResponseEntity<byte[]> callDownloadEndpoint() throws IOException {
        HttpHeaders headers = oAuthClient.headersForClientCredentials();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return testRestTemplate()
                .exchange(testUrlBuilder.createUrl(DOWNLOAD_SUBPATH), HttpMethod.GET, entity, byte[].class);
    }

    private ResponseEntity<Long> callLastModifiedEndpoint() throws IOException {
        HttpHeaders headers = oAuthClient.headersForClientCredentials();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return testRestTemplate()
                .exchange(testUrlBuilder.createUrl(LAST_MODIFIED_SUBPATH), HttpMethod.GET, entity, Long.class);
    }

    private <T> LinkedHashSet<T> newLinkedHashSet(T... elements) {
        return Sets.newLinkedHashSet(asList(elements));
    }

}
