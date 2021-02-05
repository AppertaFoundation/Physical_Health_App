package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat;

import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBase;
import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto.EhrTemplateListResponseDto;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.MarandEhrRestConstants.TEMPLATE_ENDPOINT;

@Component
public class EhrTemplate extends EhrBase {

    private static final Logger LOG = LoggerFactory.getLogger(EhrTemplate.class);

    private final EhrConnector ehrConnector;


    @Autowired
    public EhrTemplate(EhrConnector ehrConnector) {
        this.ehrConnector = ehrConnector;
    }

    public List<String> fetchTemplates() throws EhrOperationException, EhrAuthenticationException {

        List<String> templateNames = Collections.emptyList();

        try {
        RestTemplate template = ehrConnector.getSessionRestTemplate();
        UriComponents uri = ehrConnector.getSessionUriBuilder()
                .path(TEMPLATE_ENDPOINT)
                .build();

            EhrTemplateListResponseDto templatesDto = template.getForObject(uri.toUri(), EhrTemplateListResponseDto.class);

            if (templatesDto != null) {
                templateNames = templatesDto.getTemplates().stream().map(t -> t.getTemplateId()).collect(Collectors.toList());
            }

        } catch (RestClientResponseException rce) {
            LOG.debug("Issue fetching templates", rce);
            throw new EhrOperationException("Issue fetching templates", rce);
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Issue fetching templated", rce);
        }

        return templateNames;
    }

}
