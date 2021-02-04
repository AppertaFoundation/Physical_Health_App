package com.staircase13.apperta.cms.tar;

import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

public class CmsContentAggregatorTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2007-12-03T10:15:30");
    private static final LocalDateTime YESTERDAY = NOW.minusDays(1);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private CmsContentAggregator cmsContentAggregator;

    @Mock
    private CmsPageRepository cmsPageRepository;

    @Mock
    private CmsTarBuilderFactory cmsTarBuilderFactory;

    @Mock
    private CmsTarBuilder cmsTarBuilder;

    @Mock
    private CmsContentFileBuilder cmsContentFileBuilder;

    @Mock
    private LoadLog loadLog;

    @Before
    public void setupClock() {
        ReflectionTestUtils.setField(cmsContentAggregator, "clock", Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault()));
    }

    @Before
    public void setupTarBuilder() throws IOException {
        when(cmsTarBuilderFactory.newTarBuilder()).thenReturn(cmsTarBuilder);
        when(cmsTarBuilder.commit(any())).thenReturn(Optional.of(Files.createTempFile("cms",".tar.gz")));
    }

    @Test
    public void no_pages_configured_dont_create_tar() throws Exception {
        when(cmsPageRepository.findAll()).thenReturn(emptyList());

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(cmsTarBuilder).rollback();
        verifyNoMoreInteractions(cmsTarBuilder);
    }

    @Test
    public void io_exception_creating_tar_added_to_load_log() throws Exception {
        when(cmsTarBuilderFactory.newTarBuilder()).thenThrow(new IOException("oh dear"));

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(loadLog).addErrorLog("Unexpected Error 'oh dear'");

        verifyNoMoreInteractions(cmsTarBuilder);
    }

    @Test
    public void single_page() throws Exception {
        CmsPage page = CmsPage.builder().name("cms-page-1").build();

        when(cmsPageRepository.findAll()).thenReturn(asList(page));

        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page, loadLog)).thenReturn(NOW);

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(cmsContentFileBuilder).addPage(cmsTarBuilder, page, loadLog);

        verify(cmsTarBuilder).commit(NOW);
    }

    @Test
    public void multiple_pages_use_newest_modified_date_for_tar_modified() throws Exception {
        CmsPage page1 = CmsPage.builder().name("cms-page-1").build();
        CmsPage page2 = CmsPage.builder().name("cms-page-2").build();

        when(cmsPageRepository.findAll()).thenReturn(asList(page1,page2));

        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page1, loadLog)).thenReturn(YESTERDAY);
        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page2, loadLog)).thenReturn(NOW);

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(cmsContentFileBuilder).addPage(cmsTarBuilder, page1, loadLog);

        verify(cmsContentFileBuilder).addPage(cmsTarBuilder, page2, loadLog);

        verify(cmsTarBuilder).commit(NOW);
    }

    @Test
    public void if_aggregation_exception_occurs_log_exceptions_and_continue_to_next_page() throws Exception {
        CmsPage page1 = CmsPage.builder().name("cms-page-1").build();
        CmsPage page2 = CmsPage.builder().name("cms-page-2").build();
        CmsPage page3 = CmsPage.builder().name("cms-page-3").build();

        when(cmsPageRepository.findAll()).thenReturn(asList(page1,page2,page3));

        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page1, loadLog)).thenThrow(new AggregationException("oops"));
        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page2, loadLog)).thenThrow(new AggregationException("oh dear"));
        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page3, loadLog)).thenReturn(YESTERDAY);

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(loadLog).addErrorLog("Discarding page '%s' as it could not be created with reason '%s'","cms-page-1","oops");
        verify(loadLog).addErrorLog("Discarding page '%s' as it could not be created with reason '%s'","cms-page-2","oh dear");
        verify(cmsTarBuilder).commit(YESTERDAY);

        verifyNoMoreInteractions(cmsTarBuilder);
    }

    @Test
    public void if_aggregation_exception_occurs_for_all_pages_create_empty_tar() throws Exception {
        CmsPage page1 = CmsPage.builder().name("cms-page-1").build();
        CmsPage page2 = CmsPage.builder().name("cms-page-2").build();

        when(cmsPageRepository.findAll()).thenReturn(asList(page1,page2));

        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page1, loadLog)).thenThrow(new AggregationException("oops"));
        when(cmsContentFileBuilder.addPage(cmsTarBuilder, page2, loadLog)).thenThrow(new AggregationException("oh dear"));

        cmsContentAggregator.aggregateToTar(loadLog);

        verify(loadLog).addErrorLog("Discarding page '%s' as it could not be created with reason '%s'","cms-page-1","oops");
        verify(loadLog).addErrorLog("Discarding page '%s' as it could not be created with reason '%s'","cms-page-2","oh dear");

        verify(cmsTarBuilder).commit(NOW);

        verifyNoMoreInteractions(cmsTarBuilder);
    }
}
