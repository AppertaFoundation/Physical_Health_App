package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.entities.Hcp;
import com.staircase13.apperta.repository.HcpRepository;
import com.staircase13.apperta.repository.AppertaUserRepository;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.HcpSummaryDto;
import com.staircase13.apperta.service.exception.InvalidHcpUsernameException;
import com.staircase13.apperta.service.exception.NhsIdAlreadyRegisteredException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

import static com.staircase13.apperta.entities.Role.HCP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HcpServiceTest {
    private static final String EMAIL_ADDRESS = "aEmailAddress";
    private static final String FIRST_NAME = "aFirstName";
    private static final String JOB_TITLE = "aJobTitle";
    private static final String LAST_NAME = "aLastName";
    private static final String LOCATION = "aLocation";
    private static final String NHS_ID = "aNhsId";
    private static final String TITLE = "aTitle";
    private static final String USERNAME = "aUserName";
    private static final String OTHER_USERNAME = "anOtherUserName";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private HcpService hcpService;

    @Mock
    private HcpRepository hcpRepository;

    @Mock
    private AppertaUserRepository appertaUserRepository;

    @Captor
    private ArgumentCaptor<Hcp> hcpArgumentCaptor;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;
//
//    @Test
//    public void findUsernameForNhsId_doesntExist() {
//        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.empty());
//        Optional<String> result = hcpService.findUsernameForNhsId(NHS_ID);
//        assertThat(result.isPresent(), is(false));
//    }
//
//    @Test
//    public void findUsernameForNhsId_exists() {
//        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.of(Hcp.builder().user(User.builder().username(USERNAME).build()).build()));
//        Optional<String> result = hcpService.findUsernameForNhsId(NHS_ID);
//        assertThat(result.get(), is(USERNAME));
//    }

    @Test
    public void findByNhsId_noResult() {
        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.empty());

        Optional<HcpSummaryDto> result = hcpService.findByNhsId(NHS_ID);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void findByNhsId_exists() {
        Hcp aHcp = new Hcp();
        aHcp.setFirstNames(FIRST_NAME);
        aHcp.setJobTitle(JOB_TITLE);
        aHcp.setLastName(LAST_NAME);
        aHcp.setLocation(LOCATION);
        aHcp.setNhsId(NHS_ID);
        aHcp.setTitle(TITLE);

        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.of(aHcp));

        Optional<HcpSummaryDto> result = hcpService.findByNhsId(NHS_ID);

        assertThat(result.isPresent(), is(true));

        HcpSummaryDto hcpDto = result.get();
        assertThat(hcpDto.getFirstNames(), is(FIRST_NAME));
        assertThat(hcpDto.getJobTitle(), is(JOB_TITLE));
        assertThat(hcpDto.getLastName(), is(LAST_NAME));
        assertThat(hcpDto.getLocation(), is(LOCATION));
        assertThat(hcpDto.getNhsId(), is(NHS_ID));
        assertThat(hcpDto.getTitle(), is(TITLE));
    }

    @Test
    public void getProfile_userDoesntExist_throwException() throws Exception {
        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.empty());

        exception.expect(InvalidHcpUsernameException.class);
        exception.expect(hasProperty("messageArgs",arrayContaining(USERNAME)));

        hcpService.getProfile(USERNAME);
    }

    @Test
    public void getProfile_profileDoesntExist_returnUserInfoOnly() throws Exception {
        User user = User.builder()
                .username(USERNAME)
                .emailAddress(EMAIL_ADDRESS)
                .build();

        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(user));
        when(hcpRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        HcpDto hcp = hcpService.getProfile(USERNAME);

        assertThat(hcp.getUsername(), is(USERNAME));
        assertThat(hcp.getEmail(), is(EMAIL_ADDRESS));
        assertThat(hcp.getNhsId(), nullValue());
        assertThat(hcp.getTitle(), nullValue());
        assertThat(hcp.getLocation(), nullValue());
        assertThat(hcp.getLastName(), nullValue());
        assertThat(hcp.getJobTitle(), nullValue());
        assertThat(hcp.getFirstNames(), nullValue());
    }

    @Test
    public void getProfile_profileExists_returnUserInfoOnly() throws Exception {
        User user = User.builder()
                .username(USERNAME)
                .emailAddress(EMAIL_ADDRESS)
                .build();

        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.of(Hcp.builder().user(user).build()));
        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(user));

        Hcp profile = Hcp.builder()
                .nhsId(NHS_ID)
                .location(LOCATION)
                .jobTitle(JOB_TITLE)
                .title(TITLE)
                .firstNames(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();

        when(hcpRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(profile));

        HcpDto hcp = hcpService.getProfile(USERNAME);

        assertThat(hcp.getUsername(), is(USERNAME));
        assertThat(hcp.getEmail(), is(EMAIL_ADDRESS));
        assertThat(hcp.getNhsId(), is(NHS_ID));
        assertThat(hcp.getTitle(), is(TITLE));
        assertThat(hcp.getLocation(), is(LOCATION));
        assertThat(hcp.getLastName(), is(LAST_NAME));
        assertThat(hcp.getJobTitle(), is(JOB_TITLE));
        assertThat(hcp.getFirstNames(), is(FIRST_NAME));
    }

    @Test
    public void updateProfile_userNotValid_throwException() throws Exception {
        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.empty());

        exception.expect(InvalidHcpUsernameException.class);
        exception.expect(hasProperty("messageArgs",hasItemInArray(USERNAME)));

        hcpService.updateProfile(HcpDto.builder().username(USERNAME).build());
    }

    @Test
    public void updateProfile_nhsIdAlreadyRegistered_throwException() throws Exception {
        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(User.builder().build()));
        when(hcpRepository.findByNhsId(NHS_ID)).thenReturn(Optional.of(Hcp.builder().user(User.builder().username(OTHER_USERNAME).build()).build()));

        exception.expect(NhsIdAlreadyRegisteredException.class);
        exception.expect(hasProperty("messageArgs",hasItemInArray(NHS_ID)));

        hcpService.updateProfile(HcpDto.builder().nhsId(NHS_ID).username(USERNAME).build());
    }

    @Test
    public void updateProfile_noHcpRecord_createNewEntry() throws Exception {
        HcpDto hcpDto = HcpDto.builder()
                .email(EMAIL_ADDRESS)
                .firstNames(FIRST_NAME)
                .jobTitle(JOB_TITLE)
                .lastName(LAST_NAME)
                .location(LOCATION)
                .nhsId(NHS_ID)
                .title(TITLE)
                .username(USERNAME)
                .build();

        User user = User.builder()
                .username(USERNAME)
                .build();

        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(user));
        when(hcpRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        hcpService.updateProfile(hcpDto);

        verify(hcpRepository).save(hcpArgumentCaptor.capture());

        Hcp createdHcp = hcpArgumentCaptor.getValue();
        assertThat(createdHcp.getUser(), is(user));
        assertThat(createdHcp.getFirstNames(), is(FIRST_NAME));
        assertThat(createdHcp.getJobTitle(), is(JOB_TITLE));
        assertThat(createdHcp.getLastName(), is(LAST_NAME));
        assertThat(createdHcp.getLocation(), is(LOCATION));
        assertThat(createdHcp.getNhsId(), is(NHS_ID));
        assertThat(createdHcp.getTitle(), is(TITLE));
    }


    @Test
    public void updateProfile_hcpRecordExists_updateRecord() throws Exception {
        HcpDto hcpDto = HcpDto.builder()
                .firstNames(FIRST_NAME)
                .jobTitle(JOB_TITLE)
                .lastName(LAST_NAME)
                .location(LOCATION)
                .nhsId(NHS_ID)
                .title(TITLE)
                .username(USERNAME)
                .build();

        User user = User.builder()
                .username(USERNAME)
                .build();

        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(user));
        when(hcpRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(new Hcp()));

        hcpService.updateProfile(hcpDto);

        verify(hcpRepository).save(hcpArgumentCaptor.capture());

        Hcp updatedHcp = hcpArgumentCaptor.getValue();
        assertThat(updatedHcp.getFirstNames(), is(FIRST_NAME));
        assertThat(updatedHcp.getJobTitle(), is(JOB_TITLE));
        assertThat(updatedHcp.getLastName(), is(LAST_NAME));
        assertThat(updatedHcp.getLocation(), is(LOCATION));
        assertThat(updatedHcp.getNhsId(), is(NHS_ID));
        assertThat(updatedHcp.getTitle(), is(TITLE));
    }

    @Test
    public void updateProfile_updateUserEmail() throws Exception {
        HcpDto hcpDto = HcpDto.builder()
                .username(USERNAME)
                .email(EMAIL_ADDRESS)
                .build();

        User user = User.builder()
                .emailAddress("oldaddress@localhost")
                .build();

        when(appertaUserRepository.findByUsernameAndRole(USERNAME, HCP)).thenReturn(Optional.of(user));
        when(hcpRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        hcpService.updateProfile(hcpDto);

        verify(appertaUserRepository).save(userArgumentCaptor.capture());

        assertThat(userArgumentCaptor.getValue().getEmailAddress(), is(EMAIL_ADDRESS));
    }
}
