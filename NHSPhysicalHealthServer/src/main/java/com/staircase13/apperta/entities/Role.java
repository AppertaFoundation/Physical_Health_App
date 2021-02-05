package com.staircase13.apperta.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.staircase13.apperta.entities.Authority.*;

public enum Role {
    ADMIN(GET_SET_DEVICE,ALL_DEVICES,MANAGE_CMS, RESET_PASSWORD),

    PATIENT(VIEW_USER_DETAILS, GET_SET_DEVICE, HCP_SEARCH, PATIENT_UPDATE_PROFILE, CREATE_EHR, QUERY_EHR, RESET_PASSWORD),

    HCP(HCP_GET_PROFILE, HCP_UPDATE_PROFILE,VIEW_USER_DETAILS,LIST_PATIENTS, CREATE_EHR, QUERY_EHR, RESET_PASSWORD),

    RESEARCH(VIEW_USER_DETAILS, RESET_PASSWORD),

    CLIENT_SERVICE(MODIFY_APP);

    private final Set<Authority> authorities;

    Role(Authority... authorities) {
        this.authorities = new HashSet<>(Arrays.asList(authorities));
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }
}
