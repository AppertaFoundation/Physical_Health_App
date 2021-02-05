package com.staircase13.apperta.auth.server;

import com.staircase13.apperta.repository.PasswordResetTokenRepository;
import com.staircase13.apperta.service.EmailService;
import com.staircase13.apperta.service.UrlBuilder;
import com.staircase13.apperta.service.UuidService;
import com.staircase13.apperta.service.exception.InvalidTokenException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.*;
import java.util.Optional;

import static com.staircase13.apperta.auth.server.PasswordResetToken.Status.ISSUED;
import static com.staircase13.apperta.auth.server.PasswordResetToken.Status.USED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordServiceTest {

    private static final Long PERSISTED_USER_ID = 987L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private PasswordService passwordService;

    @Mock
    private OAuthUserRepository oAuthUserRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UuidService uuidService;

    @Mock
    private Clock clock;

    @Mock
    private EmailService emailService;

    @Mock
    private UrlBuilder urlBuilder;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @Captor
    private ArgumentCaptor<PasswordResetToken> passwordResetTokenArgumentCaptor;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setupUserRepository() {
        OAuthUser persistedUser = new OAuthUser();
        persistedUser.setId(PERSISTED_USER_ID);
        when(oAuthUserRepository.save(any(OAuthUser.class))).thenReturn(persistedUser);
    }

    @Test
    public void issuePasswordResetToken_tokenPersisted() {

        setClock("2007-12-03T10:15:00.00Z");

        OAuthUser user = OAuthUser.builder().emailAddress("someone@localhost").build();

        when(oAuthUserRepository.findByUsername("myUsername")).thenReturn(Optional.of(user));
        when(uuidService.randomUuid()).thenReturn("generatedUuid");

        passwordService.issuePasswordResetToken("myUsername");

        verify(passwordResetTokenRepository).save(passwordResetTokenArgumentCaptor.capture());

        PasswordResetToken passwordResetToken = passwordResetTokenArgumentCaptor.getValue();
        assertThat(passwordResetToken, notNullValue());
        assertThat(passwordResetToken.getValue(), is("generatedUuid"));
        assertThat(passwordResetToken.getStatus(), is(PasswordResetToken.Status.ISSUED));
        assertThat(passwordResetToken.getUser(), is(user));
        assertThat(passwordResetToken.getIssued(), is(LocalDateTime.of(2007, Month.DECEMBER, 3, 10, 15)));
    }

    @Test
    public void issuePasswordResetToken_emailSent() {

        setClock("2007-12-03T10:15:00.00Z");

        OAuthUser user = OAuthUser.builder().emailAddress("someone@localhost").build();

        when(oAuthUserRepository.findByUsername("myUsername")).thenReturn(Optional.of(user));
        when(uuidService.randomUuid()).thenReturn("generatedUuid");
        when(urlBuilder.resolveUiUrl("/iam/passwordReset","token","generatedUuid")).thenReturn("http://theexternalurl?token=generatedUuid");

        passwordService.issuePasswordResetToken("myUsername");

        verify(emailService).send(simpleMailMessageCaptor.capture());

        SimpleMailMessage email = simpleMailMessageCaptor.getValue();
        assertThat(email, notNullValue());
        assertThat(email.getTo(), hasItemInArray("someone@localhost"));
        assertThat(email.getSubject(), is("Password Reset"));
        assertThat(email.getText(), is("To reset your password please visit http://theexternalurl?token=generatedUuid"));
    }

    @Test
    public void issuePasswordResetToken_invalidUsername_doNothing() {
        when(oAuthUserRepository.findByUsername("myUsername")).thenReturn(Optional.empty());

        passwordService.issuePasswordResetToken("myUsername");

        verifyZeroInteractions(passwordResetTokenRepository);
        verifyZeroInteractions(emailService);
    }

    @Test
    public void issuePasswordResetToken_uuidClash_retryUntilUnique() {

        setClock("2007-12-03T10:15:00.00Z");

        when(oAuthUserRepository.findByUsername("myUsername")).thenReturn(Optional.of(new OAuthUser()));

        when(uuidService.randomUuid()).thenReturn("generatedUuid1","generatedUuid2","generatedUuid3");

        when(passwordResetTokenRepository.existsById("generatedUuid1")).thenReturn(true);
        when(passwordResetTokenRepository.existsById("generatedUuid2")).thenReturn(true);
        when(passwordResetTokenRepository.existsById("generatedUuid3")).thenReturn(false);

        passwordService.issuePasswordResetToken("myUsername");

        verify(passwordResetTokenRepository).save(passwordResetTokenArgumentCaptor.capture());

        PasswordResetToken passwordResetToken = passwordResetTokenArgumentCaptor.getValue();
        assertThat(passwordResetToken, notNullValue());
        assertThat(passwordResetToken.getValue(), is("generatedUuid3"));
    }

    @Test
    public void isValidToken_doesntExist_false() {

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.empty());

        assertThat(passwordService.isValidToken("abcdef"), is(false));

    }

    @Test
    public void isValidToken_existsButUsed_false() {

        PasswordResetToken token = PasswordResetToken.builder()
                .value("abcef")
                .status(USED)
                .build();

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.of(token));

        assertThat(passwordService.isValidToken("abcdef"), is(false));

    }

    @Test
    public void isValidToken_existsButExpired_false() {

        setClock("2018-03-12T10:30:01.00Z");
        setResetTokenValidityDuration("PT30M");

        PasswordResetToken token = PasswordResetToken.builder()
                .value("abcef")
                .status(ISSUED)
                .issued(LocalDateTime.of(2018,3,12,10,0))
                .build();

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.of(token));

        assertThat(passwordService.isValidToken("abcdef"), is(false));
    }

    @Test
    public void isValidToken_valid_true() {

        setClock("2018-03-12T10:29:59.00Z");
        setResetTokenValidityDuration("PT30M");

        PasswordResetToken token = PasswordResetToken.builder()
                .value("abcef")
                .status(ISSUED)
                .issued(LocalDateTime.of(2018,3,12,10,0))
                .build();

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.of(token));

        assertThat(passwordService.isValidToken("abcdef"), is(true));

    }

    @Test
    public void resetPassword_updatePassword() {

        when(passwordEncoder.encode("newPassword")).thenReturn("thePasswordEncoded");

        OAuthUser user = new OAuthUser();
        when(oAuthUserRepository.findByUsername("dave")).thenReturn(Optional.of(user));

        passwordService.resetPassword( "dave","newPassword");

        verify(oAuthUserRepository).save(user);
        assertThat(user.getPassword(), is("thePasswordEncoded"));

    }

    @Test
    public void resetPasswordWithToken_invalidToken_throwExeception() throws Exception {

        when(passwordResetTokenRepository.findById("abdcef")).thenReturn(Optional.empty());

        exception.expect(InvalidTokenException.class);

        passwordService.resetPasswordWithToken("abdcef","newPassword");

    }

    @Test
    public void resetPasswordWithToken_validToken_setTokenUsed() throws Exception {

        PasswordResetToken token = createValidToken();

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.of(token));

        passwordService.resetPasswordWithToken( "abcdef","newPassword");

        assertThat(token.getStatus(), is(USED));
        verify(passwordResetTokenRepository).save(token);

    }

    @Test
    public void resetPasswordWithToken_validToken_updatePassword() throws Exception {

        PasswordResetToken token = createValidToken();

        when(passwordResetTokenRepository.findById("abcdef")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword")).thenReturn("thePasswordEncoded");

        passwordService.resetPasswordWithToken( "abcdef","newPassword");

        assertThat(token.getUser().getPassword(), is("thePasswordEncoded"));

        verify(oAuthUserRepository).save(token.getUser());

    }

    private PasswordResetToken createValidToken() {
        setClock("2018-03-12T10:29:59.00Z");
        setResetTokenValidityDuration("PT30M");

        OAuthUser user = new OAuthUser();

        return PasswordResetToken.builder()
                .value("abcef")
                .status(ISSUED)
                .issued(LocalDateTime.of(2018,3,12,10,0))
                .user(user)
                .build();
    }

    @Test
    public void encodePassword() {
        when(passwordEncoder.encode("thePassword")).thenReturn("thePasswordEncoded");

        String encodedPassword = passwordEncoder.encode("thePassword");

        assertThat(encodedPassword, is("thePasswordEncoded"));
    }

    private void setClock(String time) {
        when(clock.instant()).thenReturn(Instant.parse(time));
    }

    private void setResetTokenValidityDuration(String duration) {
        when(environment.getProperty("apperta.password.reset.validity.duration",Duration.class)).thenReturn(Duration.parse(duration));
    }

}
