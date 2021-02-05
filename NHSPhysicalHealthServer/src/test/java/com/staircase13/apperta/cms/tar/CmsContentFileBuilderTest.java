package com.staircase13.apperta.cms.tar;

import com.google.common.collect.Sets;
import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.NhsApiContent.ContentCoordinates;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.loader.CmsContentLoader;
import com.staircase13.apperta.cms.loader.NhsContentKey;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;

import static com.staircase13.apperta.cms.entities.NhsApi.CONDITIONS;
import static com.staircase13.apperta.cms.entities.NhsApi.LIVE_WELL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.*;

public class CmsContentFileBuilderTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2007-12-03T10:15:30");
    private static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private CmsContentFileBuilder cmsContentFileBuilder;

    @Mock
    private CmsContentLoader cmsContentLoader;

    @Mock
    private CmsTarBuilder cmsTarBuilder;

    @Mock
    private LoadLog loadLog;
    
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setupTarBuilder() throws IOException {
        when(cmsTarBuilder.commit(NOW)).thenReturn(Optional.of(Files.createTempFile("cms",".tar.gz")));
    }

    @Test
    public void no_fragments_no_links_no_trackers() throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(NOW)
                .links(emptySet())
                .fragments(emptySet())
                .build();

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n</body></html>");
    }

    @Test
    public void links_only() throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(NOW)
                .links(newLinkedHashSet(
                    cmsPageLinkBuilder()
                            .url("http://link1")
                            .label("link1")
                            .description("description-for-link-1")
                            .build(),
                    cmsPageLinkBuilder()
                            .url("http://link2")
                            .label("link2")
                            .description("description-for-link-2")
                            .build()
                    )
                )
                .fragments(emptySet())
                .build();

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n<h2>the links header</h2>\n<dl class=\"cms-links\">\n<dt><a href=\"http://link1\">link1</a></dt>\n<dd>description-for-link-1</dd>\n<dt><a href=\"http://link2\">link2</a></dt>\n<dd>description-for-link-2</dd>\n</dl>\n</body></html>");
    }

    @Test
    public void single_content_fragment()throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(NOW)
                .links(emptySet())
                .fragments(singleton(
                        cmsPageFragmentBuilder()
                                .api(CONDITIONS)
                                .primaryEntityName("api-content-1")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(5).build()
                )).build();

        NhsApiContent nhsApiContent = mock(NhsApiContent.class);
        when(nhsApiContent.getLastModified()).thenReturn(NOW);
        when(nhsApiContent.getFragment(coordsForTitleAndPosition("test-section",5))).thenReturn(of("api-content-1 test-section pos-5"));
        when(nhsApiContent.getTrackerHtml()).thenReturn(Arrays.asList("tracker1","tracker2"));

        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(CONDITIONS).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent));

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n<div class=\"cms-trackers\">\ntracker1\ntracker2\n</div>\n<div class=\"cms-fragment\">api-content-1 test-section pos-5</div>\n</body></html>");
    }

    @Test
    public void single_content_fragment_and_links()throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(NOW)
                .links(newLinkedHashSet(
                        cmsPageLinkBuilder()
                                .url("http://link1")
                                .label("link1")
                                .description("description-for-link-1")
                                .build(),
                        cmsPageLinkBuilder()
                                .url("http://link2")
                                .label("link2")
                                .description("description-for-link-2")
                                .build()
                        )
                )
                .fragments(singleton(
                        cmsPageFragmentBuilder()
                                .api(CONDITIONS)
                                .primaryEntityName("api-content-1")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(5).build()
                )).build();

        NhsApiContent nhsApiContent = mock(NhsApiContent.class);
        when(nhsApiContent.getLastModified()).thenReturn(NOW);
        when(nhsApiContent.getFragment(coordsForTitleAndPosition("test-section",5))).thenReturn(of("api-content-1 test-section pos-5"));
        when(nhsApiContent.getTrackerHtml()).thenReturn(Arrays.asList("tracker1","tracker2"));

        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(CONDITIONS).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent));

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n<div class=\"cms-trackers\">\ntracker1\ntracker2\n</div>\n<div class=\"cms-fragment\">api-content-1 test-section pos-5</div>\n<h2>the links header</h2>\n<dl class=\"cms-links\">\n<dt><a href=\"http://link1\">link1</a></dt>\n<dd>description-for-link-1</dd>\n<dt><a href=\"http://link2\">link2</a></dt>\n<dd>description-for-link-2</dd>\n</dl>\n</body></html>");
    }

    @Test
    public void multiple_content_fragments_same_source()throws Exception {
        CmsPage page = CmsPage.builder()
                        .name("cms-page-1")
                        .linksHeader("the links header")
                        .modified(NOW)
                        .links(emptySet())
                        .fragments(newLinkedHashSet(
                            cmsPageFragmentBuilder()
                                    .api(LIVE_WELL)
                                    .primaryEntityName("api-content-1")
                                    .jsonSectionHeading("test-section")
                                    .jsonMainEntityPosition(10).build(),
                            cmsPageFragmentBuilder()
                                    .api(LIVE_WELL)
                                    .primaryEntityName("api-content-1")
                                    .jsonSectionHeading("test-section")
                                    .jsonMainEntityPosition(1).build()
                    )).build();

        NhsApiContent nhsApiContent = mock(NhsApiContent.class);
        when(nhsApiContent.getLastModified()).thenReturn(NOW);
        when(nhsApiContent.getFragment(coordsForTitleAndPosition("test-section",1))).thenReturn(of("api-content-1 test-section pos-1"));
        when(nhsApiContent.getFragment(coordsForTitleAndPosition("test-section",10))).thenReturn(of("api-content-1 test-section pos-10"));
        when(nhsApiContent.getTrackerHtml()).thenReturn(Arrays.asList("tracker1","tracker2"));

        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(LIVE_WELL).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent));

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n<div class=\"cms-trackers\">\ntracker1\ntracker2\n</div>\n<div class=\"cms-fragment\">api-content-1 test-section pos-10</div>\n<div class=\"cms-fragment\">api-content-1 test-section pos-1</div>\n</body></html>");
    }

    @Test
    public void multiple_content_fragments_different_source()throws Exception {
        CmsPage page = CmsPage.builder()
                                .name("cms-page-1")
                                .linksHeader("the links header")
                                .modified(NOW)
                                .links(emptySet())
                                .fragments(newLinkedHashSet(
                                        cmsPageFragmentBuilder()
                                                .api(CONDITIONS)
                                                .primaryEntityName("api-content-1")
                                                .jsonSectionHeading("test-section")
                                                .jsonMainEntityPosition(1).build(),
                                        cmsPageFragmentBuilder()
                                                .api(LIVE_WELL)
                                                .primaryEntityName("api-content-2")
                                                .secondaryEntityName("secondary")
                                                .jsonSectionHeading("test-section")
                                                .jsonMainEntityPosition(1).build()
                                )).build();

        NhsApiContent nhsApiContent1 = mock(NhsApiContent.class);
        when(nhsApiContent1.getLastModified()).thenReturn(NOW);
        when(nhsApiContent1.getFragment(coordsForTitleAndPosition("test-section",1))).thenReturn(of("api-content-1 test-section pos-1"));
        when(nhsApiContent1.getTrackerHtml()).thenReturn(Arrays.asList("content1Tracker1","content1Tracker2"));
        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(CONDITIONS).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent1));

        NhsApiContent nhsApiContent2 = mock(NhsApiContent.class);
        when(nhsApiContent2.getLastModified()).thenReturn(NOW);
        when(nhsApiContent2.getFragment(coordsForTitleAndPosition("test-section",1))).thenReturn(of("api-content-2 test-section pos-1"));
        when(nhsApiContent2.getTrackerHtml()).thenReturn(Arrays.asList("content2Tracker1","content2Tracker2"));
        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(LIVE_WELL).primaryEntityName("api-content-2").secondaryEntityName("secondary").build(),loadLog))
                .thenReturn(of(nhsApiContent2));

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).addPage("cms-page-1.html","<html><body>\n<div class=\"cms-trackers\">\ncontent1Tracker1\ncontent1Tracker2\ncontent2Tracker1\ncontent2Tracker2\n</div>\n<div class=\"cms-fragment\">api-content-1 test-section pos-1</div>\n<div class=\"cms-fragment\">api-content-2 test-section pos-1</div>\n</body></html>");
    }

    @Test
    public void if_fragment_cant_be_loaded_discard_whole_page() throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(YESTERDAY)
                .links(emptySet())
                .fragments(newLinkedHashSet(
                        cmsPageFragmentBuilder()
                                .api(CONDITIONS)
                                .primaryEntityName("api-content-1")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(0).build(),
                        cmsPageFragmentBuilder()
                                .api(LIVE_WELL)
                                .primaryEntityName("api-content-2")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(0).build()
                )).build();

        NhsApiContent nhsApiContent1 = mock(NhsApiContent.class);
        when(nhsApiContent1.getLastModified()).thenReturn(YESTERDAY);
        when(nhsApiContent1.getFragment(coordsForTitleAndPosition("test-section",0))).thenReturn(of("api-content-1 test-section pos-0"));
        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(CONDITIONS).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent1));

        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(LIVE_WELL).primaryEntityName("api-content-2").build(),loadLog))
                .thenReturn(empty());

        expectedException.expect(AggregationException.class);

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);
    }

    @Test
    public void if_content_cant_be_loaded_discard_whole_page() throws Exception {
        CmsPage page = CmsPage.builder()
                .name("cms-page-1")
                .linksHeader("the links header")
                .modified(YESTERDAY)
                .links(emptySet())
                .fragments(newLinkedHashSet(
                        cmsPageFragmentBuilder()
                                .api(LIVE_WELL)
                                .primaryEntityName("api-content-1")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(0).build(),
                        cmsPageFragmentBuilder()
                                .api(LIVE_WELL)
                                .primaryEntityName("api-content-2")
                                .jsonSectionHeading("test-section")
                                .jsonMainEntityPosition(0).build()
                )).build();

        NhsApiContent nhsApiContent1 = mock(NhsApiContent.class);
        when(nhsApiContent1.getLastModified()).thenReturn(YESTERDAY);
        when(nhsApiContent1.getFragment(coordsForTitleAndPosition("test-section",0))).thenReturn(of("api-content-1 test-section pos-0"));
        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(LIVE_WELL).primaryEntityName("api-content-1").build(),loadLog))
                .thenReturn(of(nhsApiContent1));

        NhsApiContent nhsApiContent2 = mock(NhsApiContent.class);
        when(nhsApiContent2.getLastModified()).thenReturn(NOW);
        when(nhsApiContent2.getFragment(coordsForTitleAndPosition("test-section",0))).thenReturn(empty());
        when(cmsContentLoader.getApiContent(
                NhsContentKey.builder().nhsApi(LIVE_WELL).primaryEntityName("api-content-2").build(),loadLog))
                .thenReturn(of(nhsApiContent2));

        expectedException.expect(AggregationException.class);

        cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog);
    }

    private <T> LinkedHashSet<T> newLinkedHashSet(T... elements) {
        return Sets.newLinkedHashSet(asList(elements));
    }

    private ContentCoordinates coordsForTitleAndPosition(String title, int position) {
        return ContentCoordinates.builder().sectionHeading(title).mainEntityPosition(position).build();
    }

    private static CmsPageFragment.CmsPageFragmentBuilder cmsPageFragmentBuilder() {
        return CmsPageFragment.builder();
    }

    private static CmsPageLink.CmsPageLinkBuilder cmsPageLinkBuilder() {
        return CmsPageLink.builder();
    }
}
