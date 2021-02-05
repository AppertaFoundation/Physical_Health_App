package com.staircase13.apperta.ehrconnector.impls.MarandBase;

import com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.EhrRecord;
import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomError {
    public String userMessage;
    public String developerMessage;

    public static CustomError convert(RestClientResponseException rre){
        CustomError customError = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            customError = mapper.readValue(rre.getResponseBodyAsString(), CustomError.class);
        } catch (Exception e){
            Logger LOG = LoggerFactory.getLogger(EhrRecord.class);
            LOG.debug("Bad request");
        }

        return customError;
    }
}
