package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;


import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.EhrDetailsDto;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.MarandEhrRestConstants.EHR_ENDPOINT;
import static com.staircase13.apperta.ehrconnector.OpenEhrRestConstants.EHR_SUBJECT_NAMESPACE;
import static com.staircase13.apperta.ehrconnector.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EhrRecordTest {



    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private EhrRecord ehrRecord;

    private IdentifiedParty identifiedParty;

    @Mock
    private EhrConnector ehrConnector;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setupBackendUrl() throws EhrOperationException, EhrAuthenticationException {
        ReflectionTestUtils.setField(ehrConnector, "baseEhrUrl", EHR_URL);
        ehrRecord = new EhrRecord(ehrConnector);
        identifiedParty = IdentifiedParty.builder().name(COMMITTER_NAME).number(COMMITTER_NUMBER).build();
        when(ehrConnector.getSessionRestTemplate()).thenReturn(restTemplate);
        when(ehrConnector.getSessionUriBuilder()).thenReturn(UriComponentsBuilder.fromHttpUrl(EHR_URL));
    }

    @Test
    public void findEhrStatusByUserName_success() {
        EhrDetailsDto detailsDto = EhrDetailsDto.builder().ehrId(EHR_ID).build();
        when(restTemplate.getForObject(any(URI.class),any(Class.class))).thenReturn(detailsDto);

        EhrDetailsDto result = null;
        try {
            result = ehrRecord.findEhrStatusByUserName(TEST_USERNAME);
        } catch (EhrOperationException eoe) {
        } catch (EhrAuthenticationException ae) {
        }


        assert detailsDto.equals(result);
        String requestString = EHR_URL + EHR_ENDPOINT + "?subjectId="+TEST_USERNAME+"&subjectNamespace="+EHR_SUBJECT_NAMESPACE;
        verify(restTemplate).getForObject(URI.create(requestString), EhrDetailsDto.class);
    }


    @Test
    public void createEhrForUserName_success() {
        EhrDetailsDto detailsDto = EhrDetailsDto.builder().ehrId(EHR_ID).build();
        when(restTemplate.postForObject(any(URI.class),isNull(), any(Class.class))).thenReturn(detailsDto);

        EhrDetailsDto dto = null;
        try {
             dto = ehrRecord.createEhrForUserName(TEST_USERNAME, identifiedParty);

        } catch (EhrOperationException eoe) {
        } catch (EhrAuthenticationException ae) {
        }
        assert detailsDto.equals(dto);
        String requestString = EHR_URL + EHR_ENDPOINT + "?subjectId="+TEST_USERNAME+"&subjectNamespace="+
                EHR_SUBJECT_NAMESPACE+"&committerName="+COMMITTER_NAME+"&committerId="+COMMITTER_NUMBER;
        verify(restTemplate).postForObject(URI.create(requestString), null, EhrDetailsDto.class);
    }
}
