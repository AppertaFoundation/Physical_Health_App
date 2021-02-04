package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;

import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.EhrQueryRequestDto;
import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.MarandEhrQueryResponseDto;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import static com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.MarandEhrRestConstants.*;

@Component
public class EhrQuery implements IEhrQuery {

    private static final Logger LOG = LoggerFactory.getLogger(EhrQuery.class);

    private EhrConnector ehrConnector;


    @Autowired
    public EhrQuery(EhrConnector ehrConnector) {
        this.ehrConnector = ehrConnector;
    }

    public String executeBasicQuery(String aqlQuery) throws EhrOperationException, EhrAuthenticationException {

        RestTemplate template = ehrConnector.getSessionRestTemplate();

        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(QUERY_ENDPOINT).build();

        EhrQueryRequestDto requestDto = new EhrQueryRequestDto(aqlQuery);

        try {
            MarandEhrQueryResponseDto queryResponseDto = template.postForObject(uri.toUri(), requestDto,
                    MarandEhrQueryResponseDto.class);

            // Allow for 204 (no content) response which is successful but contains no body.
            if (queryResponseDto == null || queryResponseDto.getResultSet() == null) {
                return null;
            } else {
                return queryResponseDto.getResultSet();
            }

        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Create composition server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    throw new EhrGenericClientException("404: Create composition Operation Not Found");
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Create composition failed", rce);
        }
    }
}
