package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;

import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.EhrDetailsDto;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrRecord;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import static com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.MarandEhrRestConstants.*;
import static com.staircase13.apperta.ehrconnector.OpenEhrRestConstants.EHR_SUBJECT_NAMESPACE;

@Component
public class EhrRecord extends EhrBase implements IEhrRecord {

    private static final Logger LOG = LoggerFactory.getLogger(EhrRecord.class);

    private EhrConnector ehrConnector;


    @Autowired
    public EhrRecord(EhrConnector ehrConnector) {
        this.ehrConnector = ehrConnector;
    }


    public EhrDetailsDto findEhrStatusByUserName(String userName) throws EhrOperationException, EhrAuthenticationException {
        // TODO: need to do caching here
        EhrDetailsDto ehrDetails = null;

        RestTemplate template = ehrConnector.getSessionRestTemplate();
        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(EHR_ENDPOINT)
                .query("subjectId={subjectId}&subjectNamespace={namespace}")
                .buildAndExpand(userName, EHR_SUBJECT_NAMESPACE);

        try {
            ehrDetails = template.getForObject(uri.toUri(), EhrDetailsDto.class);
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("EhrRecord findEhrStatusByUserName server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 204:
                    LOG.debug("Username does not exist: " + userName);
                    break;
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
            throw new EhrGenericClientException("");
        }

        return ehrDetails;
    }

    /**
     *
     * @param userName Username to create as subject id for EHR - expected pre-validated
     * @param committer Committer details - expected pre-validated
     * @return
     * @throws EhrOperationException
     */
    public EhrDetailsDto createEhrForUserName(String userName, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException {

        EhrDetailsDto ehrDetails = null;

        RestTemplate template = ehrConnector.getSessionRestTemplate();

        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(EHR_ENDPOINT)
                .query("subjectId={subjectId}&subjectNamespace={namespace}&committerName={committerName}&committerId={committerId}")
                .buildAndExpand(userName, EHR_SUBJECT_NAMESPACE, committer.getName(), committer.getNumber());

        try {
            ehrDetails = template.postForObject(uri.toUri(), null, EhrDetailsDto.class);
            LOG.debug("Created ehr for user: " + userName);
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("EhrRecord createEhrForUserName server issue: ", rre);
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
            throw new EhrGenericClientException("");
        }

        return ehrDetails;
    }

}
