package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis;

import com.staircase13.apperta.ehrconnector.CompositionResult;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.dto.EhrCompositionCreateResponse;
import com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.dto.EthercisCompositionResponse;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrComposition;
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

import static com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.EthercisEhrRestConstants.COMPOSITION_ENDPOINT;

@Component
public class EthercisEhrComposition extends EhrBase implements IEhrComposition {

    private static final Logger LOG = LoggerFactory.getLogger(EthercisEhrComposition.class);

    private EthercisEhrConnector ehrConnector;


    @Autowired
    public EthercisEhrComposition(EthercisEhrConnector ehrConnector) {
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
            EthercisCompositionResponse queryResponseDto = template.getForObject(uri.toUri(), EthercisCompositionResponse.class);

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
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    // TODO : send back as validation
                    throw new EhrGenericClientException("404: Get composition  Not Found");
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
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
                // TODO: pass back validation errors
                LOG.debug("Failed to update ehr composition " + uid + " for ehrId: " + ehrId );
                compositionResult.setSuccess(false);
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
            // TODO:  handle getting response body and returning for failed validation
 //           {"status":400,"code":"COMP-1094","userMessage":"Specified composition has already been updated by another request.","developerMessage":"Specified composition has already been updated.","exceptionMessage":"Updating composition which has already been modified by another contribution!","moreInfo":"https://confluence.ehrvendor.com/display/ehrrest/COMP-1094","requestHref":"https://cdr.code4health.org/rest/v1/composition/1a8463e3-d6b7-4f31-8b53-902d4319a4b3::0269bd31-0d37-4d74-b3d4-5a302605d980::2?templateId=IDCR%20-%20Vital%20Signs%20Encounter.v1&format=FLAT&committerName=Patient&committerId=1"}
//            rre.getResponseBodyAsString()
            switch (hsce.getRawStatusCode()) {
                case 403:
                    ehrConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(hsce.getRawStatusCode(), ehrConnector.getSessionId());
                case 404:
                    // TODO: throw this back as a validation error
                    throw new EhrGenericClientException("404: Update composition Not Found");
                default:
                    throw new EhrOperationException("Create composition  response " + hsce.getRawStatusCode() , hsce);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Create composition failed", rce);
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

    // TODO
//    private Map<String, List<Map<String, String>>> createStructuredContents(Map<String, String> contents) {
//
//        Map<String, List<Map<String,String>>> structuredContents = new HashMap<>();
//        contents.keySet().forEach(key -> {
//            if (key.contains(":")) {
//                // array val
//            } else {
//                // flat val
//            }
//        });
//
//        return structuredContents;
//    }
}

