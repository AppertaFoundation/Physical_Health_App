package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics;

import static com.staircase13.apperta.ehrconnector.impls.MarandBase.MarandRestConstants.REST_BASE_V1;

public class MarandDemographicsRestConstants {

    static final String DEMOGRAPHICS_PARTY_ENDPOINT      = REST_BASE_V1 + "/demographics/party";
    static final String DEMOGRAPHICS_PARTY_QUERY_ENDPOINT = DEMOGRAPHICS_PARTY_ENDPOINT + "/query";

    public static final String EHR_MOBILE_TELEPHONE = "mobile";
    public static final String EHR_TELEPHONE = "telephone";
    public static final String EHR_TITLE = "title";
    public static final String EHR_NHS_NUMBER = "nhs.number";
    public static final String EHR_GENDER_CODE = "gender.code";
    public static final String EHR_HEALTHCARE_PROFESSIONAL_KEY = "apperta.hcp.id";
    public static final String EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY = "apperta.primary.hcp.id";


}
