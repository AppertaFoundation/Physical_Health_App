package com.staircase13.apperta.api;

import com.google.api.client.http.HttpStatusCodes;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.HcpSummaryDto;
import com.staircase13.apperta.service.HcpService;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HcpEndpointTest {

    private static final String USERNAME = "myHcpUser";
    private static final String NHS_ID = "myNhsId";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private HcpEndpoint hcpEndpoint;

    @Mock
    private HcpService hcpService;

    @Test
    public void findByNhsId_result() {
        HcpSummaryDto result = new HcpSummaryDto();
        when(hcpService.findByNhsId(NHS_ID)).thenReturn(Optional.of(result));
        ResponseEntity<HcpSummaryDto> response = hcpEndpoint.findByNhsId(NHS_ID);

        assertThat(response.getStatusCode().value(), is(HttpStatusCodes.STATUS_CODE_OK));
        assertThat(response.getBody(), is(result));
    }

    @Test
    public void findByNhsId_noResult_404() {
        when(hcpService.findByNhsId(NHS_ID)).thenReturn(Optional.empty());
        ResponseEntity<HcpSummaryDto> response = hcpEndpoint.findByNhsId(NHS_ID);

        assertThat(response.getStatusCode().value(), is(HttpStatusCodes.STATUS_CODE_NOT_FOUND));
    }

    @Test
    public void getProfile() throws Exception {
        HcpDto profile = new HcpDto();
        when(hcpService.getProfile(USERNAME)).thenReturn(profile);

//        HcpDto response = hcpEndpoint.getProfile(
//                AppertaPrinciple.builder().username(USERNAME).build());
//
//        assertThat(response, is(profile));
    }

    @Test
    public void updateProfile() throws Exception {
        HcpDto profile = new HcpDto();
        profile.setUsername(USERNAME);

        hcpEndpoint.createUpdateProfile(
                profile,
                AppertaPrinciple.builder().username(USERNAME).build());

        verify(hcpService).updateProfile(profile);
    }

    @Test
    public void updateProfile_usernameDoesntMatchAuthUserName_throwException() throws Exception {
        HcpDto profile = HcpDto.builder().username(USERNAME).build();

        profile.setUsername(USERNAME);

        exception.expect(IncorrectHcpUsernameException.class);
        exception.expect(hasProperty("messageArgs",arrayContaining(USERNAME, "some other username")));

        hcpEndpoint.createUpdateProfile(
                profile,
                AppertaPrinciple.builder().username("some other username").build());
    }

}
