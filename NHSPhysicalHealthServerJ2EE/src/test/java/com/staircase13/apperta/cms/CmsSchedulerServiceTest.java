package com.staircase13.apperta.cms;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.verify;

public class CmsSchedulerServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private CmsSchedulerService cmsSchedulerService;

    @Mock
    private CmsService cmsService;

    @Test
    public void scheduler_rebuilds_tar() {
        cmsSchedulerService.updateTar();

        verify(cmsService).rebuildTar();
    }
}
