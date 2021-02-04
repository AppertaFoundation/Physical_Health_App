package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;

import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import static com.staircase13.apperta.ehrconnector.TestConstants.*;
import static org.mockito.Mockito.when;

public class EhrConnectorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private EhrConnector ehrConnector;

    @Mock
    private Environment environment;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup(){
        when(environment.getProperty(ConfigConstants.EHR_BASE_URL)).thenReturn(EHR_URL);
        when(environment.getProperty(ConfigConstants.EHR_USERNAME)).thenReturn(TEST_USERNAME);
        when(environment.getProperty(ConfigConstants.EHR_PASSWORD)).thenReturn(TEST_PASSWORD);
        // This test is being run outside of spring so we manually call the postconstruct
        ehrConnector.setupConnectionProperties();
    }

    @Test
    public void createEhrSession_success() {

        try {
            ehrConnector.createEhrSession();
        } catch (EhrOperationException eoe){
            assert(false);
        } catch (EhrAuthenticationException ae) {
            assert(false);
        }
        // TODO: test outcomes
    }

}
