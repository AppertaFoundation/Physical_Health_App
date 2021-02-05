package com.staircase13.apperta.integration;

import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.DemographicsConnector;
import com.staircase13.apperta.integration.util.ResetDatabaseTestListener;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(value = ResetDatabaseTestListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private DemographicsConnector demographicsConnector;

    protected TestRestTemplate testRestTemplate() {
        return testRestTemplate;
    }

    @After
    public void discardEhrSessions() {
        demographicsConnector.closeEhrSession(false);
    }
}
