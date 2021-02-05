package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;

import com.staircase13.apperta.ehrconnector.*;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.CustomError;
import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.EhrCompositionCreateResponse;
import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.MarandCompositionResponse;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrComposition;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBase;
import com.staircase13.apperta.service.dto.CompositionResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.MarandEhrRestConstants.COMPOSITION_ENDPOINT;

@Component
public class EhrComposition extends EhrBase implements IEhrComposition {

    private static final Logger LOG = LoggerFactory.getLogger(EhrComposition.class);

    private EhrConnector ehrConnector;


    @Autowired
    public EhrComposition(EhrConnector ehrConnector) {
        this.ehrConnector = ehrConnector;
    }

    public CompositionResult createComposition(String ehrId, String templateId, Map<String, List<Map<String, String>>> content,
                                               IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException {

        CompositionResult compositionResult = new CompositionResult();

        Map<String, String> flatContents = createFlatContents(content);

        // Send
        RestTemplate template = ehrConnector.getSessionRestTemplate();

        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(COMPOSITION_ENDPOINT)
                .query("templateId={templateId}&ehrId={ehrId}&format=FLAT&committerName={committerName}&committerId={committerId}")
                .buildAndExpand(templateId, ehrId, committer.getName(), committer.getNumber());

        try {
            EhrCompositionCreateResponse compositionDetails = template.postForObject(uri.toUri(), flatContents, EhrCompositionCreateResponse.class);
            LOG.debug("Created ehr composition for ehrId: " + ehrId + " with composition Uid: " + compositionDetails.getCompositionUid());
            compositionResult.setSuccess(true);
            compositionResult.setCompositionUid(compositionDetails.getCompositionUid());
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Create composition server issue: ", rre);
            }

            switch (rre.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    throw new EhrGenericClientException("404: Create composition Operation Not Found");
                default:
                    CustomError error = CustomError.convert(rre);
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre, error.userMessage);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Create composition failed", rce);
        }


        return compositionResult;
    }

    public CompositionResultDto fetchCompositionByUid(String uid) throws EhrOperationException, EhrAuthenticationException {

        CompositionResultDto resultDto = new CompositionResultDto();

        RestTemplate template = ehrConnector.getSessionRestTemplate();

        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(COMPOSITION_ENDPOINT + "/" + uid)
                .query("format=FLAT")
                .build();

        try {
            MarandCompositionResponse queryResponseDto = template.getForObject(uri.toUri(), MarandCompositionResponse.class);

            resultDto.setComposition(queryResponseDto.getComposition());
            resultDto.setUid(getStringIdFromHref(queryResponseDto));
            resultDto.setLatest(queryResponseDto.isLastVersion());
            resultDto.setNextVersion(getStringIdFromNextHref(queryResponseDto));
        } catch (RestClientResponseException rre) {
            // Query failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Create composition server issue: ", rre);
            }

            switch (rre.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre, "Get composition  Not Found");
                default:
                    CustomError error = CustomError.convert(rre);
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre, error.userMessage);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Create composition failed", rce);
        }

        return resultDto;
    }

    public CompositionResult updateComposition(String ehrId, String uid, String templateId, Map<String, List<Map<String, String>>> contents,
                                        IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException {
        CompositionResult compositionResult = new CompositionResult();

        Map<String, String> flatContents = createFlatContents(contents);

        // Send
        RestTemplate template = ehrConnector.getSessionRestTemplate();

        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(COMPOSITION_ENDPOINT + "/" + uid)
                .query("templateId={templateId}&format=FLAT&committerName={committerName}&committerId={committerId}")
                .buildAndExpand(templateId, committer.getName(), committer.getNumber());

        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            List<MediaType> mediaTypeList = new ArrayList<MediaType>();
            mediaTypeList.add(MediaType.APPLICATION_JSON);
            requestHeaders.setAccept(mediaTypeList);
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,String>> requestEntity = new HttpEntity<>(flatContents,requestHeaders);

            ResponseEntity<EhrCompositionCreateResponse> responseEntity = template.exchange(uri.toUri(), HttpMethod.PUT, requestEntity, EhrCompositionCreateResponse.class);
            if (responseEntity == null || !responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new EhrOperationException("Failed to update ehr composition" , null);
            } else {
                EhrCompositionCreateResponse createResponse =  responseEntity.getBody();
                LOG.debug("Updated ehr composition " + uid + " for ehrId: " + ehrId + " with resulting composition Uid: " + createResponse.getCompositionUid());
                compositionResult.setSuccess(true);
                compositionResult.setCompositionUid(createResponse.getCompositionUid());
            }

        } catch (HttpStatusCodeException hsce) {
            // Creation failed
            int statusCode = hsce.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Create composition server issue: ", hsce);
            }

            switch (hsce.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(hsce.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    throw new EhrOperationException("Update composition response 404" , hsce, "composition Not Found");
                default:
                    CustomError error = CustomError.convert(hsce);
                    throw new EhrOperationException("Update composition  response " + hsce.getRawStatusCode() , hsce, error.userMessage);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Update composition failed", rce);
        }

        return compositionResult;
    }

    private Map<String, String> createFlatContents(Map<String, List<Map<String, String>>> contents) {

        Map<String, String> flatContents = new HashMap<>();

        contents.keySet().forEach(prefix -> {
            List<Map<String, String>> listVals = contents.get(prefix);
            if (listVals.size() > 1) {
                // array type
                int arrayCount = 0;
                for(Map<String, String> content : listVals) {
                    addToMap(flatContents, prefix, content, arrayCount);
                    arrayCount++;
                };
            } else {
                addToMap(flatContents, prefix, listVals.get(0), -1);
            }
        });

        return flatContents;
    }

    private void addToMap(Map<String, String> toMap, String prefix, Map<String, String> fromMap, int arrayCount) {
        StringBuffer prefixSb = new StringBuffer(prefix);
        if (arrayCount >= 0) {
            prefixSb.append(":").append(arrayCount);
        }
        prefixSb.append("/");
        String prefixPath = prefixSb.toString();
        fromMap.entrySet().forEach(entry -> toMap.put(prefixPath + entry.getKey(), entry.getValue()));
    }
}

