package com.staircase13.apperta.integration.util;


import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.repository.AppertaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUsers {
    public static final String HCP_1_USERNAME = "hcp1";
    public static final String HCP_1_PASSWORD = "hcp1password";

    public static final String HCP_2_USERNAME = "hcp2";
    public static final String HCP_2_PASSWORD = "hcp1password";

    public static final String PATIENT_USERNAME = "patient1";
    public static final String PATIENT_PASSWORD = "patient1password";

    public static final String RESEARCH_USERNAME = "research1";
    public static final String RESEARCH_PASSWORD = "research1password";

    @Autowired
    private AppertaUserRepository appertaUserRepository;

    public User getPatientUser() {
        return appertaUserRepository.findByUsername(PATIENT_USERNAME).get();
    }
}
