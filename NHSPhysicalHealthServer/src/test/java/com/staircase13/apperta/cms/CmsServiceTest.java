package com.staircase13.apperta.cms;

import com.staircase13.apperta.cms.dto.CmsPageDto;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.repository.CmsCacheRepository;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import com.staircase13.apperta.cms.tar.CmsContentAggregator;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CmsServiceTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private CmsService cmsService;

    @Mock
    private CmsDtoAdapter cmsDtoAdapter;

    @Mock
    private CmsPageRepository cmsPageRepository;

    @Mock
    private CmsCacheRepository cmsCacheRepository;

    @Mock
    private CmsContentAggregator cmsContentAggregator;

    @Test
    public void get_pages() {

        CmsPage page1 = new CmsPage();
        CmsPage page2 = new CmsPage();

        when(cmsPageRepository.findAll()).thenReturn(Arrays.asList(page1,page2));

        CmsPageDto dto1 = CmsPageDto.builder().build();
        CmsPageDto dto2 = CmsPageDto.builder().build();

        when(cmsDtoAdapter.toDto(page1)).thenReturn(dto1);
        when(cmsDtoAdapter.toDto(page2)).thenReturn(dto2);

        List<CmsPageDto> dtos = cmsService.getPages();
        assertThat(dtos, hasSize(2));
        assertThat(dtos, hasItem(dto1));
        assertThat(dtos, hasItem(dto2));

    }

    @Test
    public void save_changes() {

        CmsPageDto dto1 = CmsPageDto.builder().build();
        CmsPageDto dto2 = CmsPageDto.builder().build();

        CmsPage page1 = new CmsPage();
        CmsPage page2 = new CmsPage();
        when(cmsDtoAdapter.toPage(dto1)).thenReturn(page1);
        when(cmsDtoAdapter.toPage(dto2)).thenReturn(page2);

        LoadLog loadLog = new LoadLog();
        when(cmsContentAggregator.aggregateToTar()).thenReturn(loadLog);

        LoadLog result = cmsService.saveChanges(Arrays.asList(dto1,dto2));

        assertThat(result, is(loadLog));

        verify(cmsPageRepository).deleteAll();
        verify(cmsPageRepository).save(page1);
        verify(cmsPageRepository).save(page2);
    }

    @Test
    public void flush_cache() {
        cmsService.flushCache();

        verify(cmsCacheRepository).deleteAll();
        verify(cmsContentAggregator).aggregateToTar();
    }
}
