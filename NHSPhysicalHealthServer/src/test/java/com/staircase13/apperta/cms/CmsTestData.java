package com.staircase13.apperta.cms;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class CmsTestData {
    public static String getInvalidConditionResponse() throws IOException {
        return getNhsApiJson("/nhs-api/condition-invalid-json.json");
    }

    public static String getValidConditionResponse() throws IOException {
        return getNhsApiJson("/nhs-api/condition-response.json");
    }

    public static String getValidConditionResponseNoTracker() throws IOException {
        return getNhsApiJson("/nhs-api/condition-response-no-tracker.json");
    }

    public static String getValidConditionResponseManyTrackers() throws IOException {
        return getNhsApiJson("/nhs-api/condition-response-many-trackers.json");
    }

    private static String getNhsApiJson(String contentResource) throws IOException {
        return IOUtils.toString(CmsTestData.class.getResourceAsStream(contentResource), Charset.defaultCharset());
    }
}
