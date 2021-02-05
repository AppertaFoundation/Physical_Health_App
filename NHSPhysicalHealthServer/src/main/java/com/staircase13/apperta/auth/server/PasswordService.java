package com.staircase13.apperta.auth.server;

import com.staircase13.apperta.repository.PasswordResetTokenRepository;
import com.staircase13.apperta.service.EmailService;
import com.staircase13.apperta.service.UrlBuilder;
import com.staircase13.apperta.service.UuidService;
import com.staircase13.apperta.service.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class PasswordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordService.class);

    private static final String CONFIG_RESET_TOKEN_VALIDITY_DURATION = "apperta.password.reset.validity.duration";

    private final OAuthUserRepository oAuthUserRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final UuidService uuidService;

    private final Clock clock;

    private final EmailService emailService;

    private final UrlBuilder urlBuilder;

    private final PasswordEncoder passwordEncoder;

    private final Environment environment;

    @Autowired
    public PasswordService(OAuthUserRepository oAuthUserRepository, PasswordResetTokenRepository passwordResetTokenRepository, UuidService uuidService, Clock clock, EmailService emailService, UrlBuilder urlBuilder, PasswordEncoder passwordEncoder, Environment environment) {
        this.oAuthUserRepository = oAuthUserRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.uuidService = uuidService;
        this.clock = clock;
        this.emailService = emailService;
        this.urlBuilder = urlBuilder;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    public void issuePasswordResetToken(String username) {

        LOGGER.debug("Password reset token requested for user '{}'",username);

        Optional<OAuthUser> user = oAuthUserRepository.findByUsername(username);

        if(!user.isPresent()) {
            LOGGER.warn("Password reset token requested for non existent user '{}'",username);
            return;
        }

        PasswordResetToken token = PasswordResetToken.builder()
                .value(generateUniqueUuid())
                .status(PasswordResetToken.Status.ISSUED)
                .user(user.get())
                .issued(now())
                .build();

        passwordResetTokenRepository.save(token);

        String passwordResetUrl = urlBuilder.resolveUiUrl("/iam/passwordReset", "token", token.getValue());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.get().getEmailAddress());
        message.setSubject("Password Reset");
        message.setText("To reset your password please visit " + passwordResetUrl);
        emailService.send(message);

        LOGGER.debug("Reset token '{}' sent to '{}'",token.getValue(),user.get().getEmailAddress());
    }

    public boolean isValidToken(String tokenString) {

        LOGGER.debug("Validating token '{}'",tokenString);

        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findById(tokenString);

        if(!tokenOptional.isPresent()) {
            LOGGER.debug("Token '{}' doesn't exist",tokenString);
            return false;
        }

        PasswordResetToken token = tokenOptional.get();

        if(token.getStatus() == PasswordResetToken.Status.USED) {
            LOGGER.debug("Token '{}' has been used",tokenString);
            return false;
        }

        LocalDateTime expiryDateTime = token.getIssued().plusSeconds(getResetTokenValidityDuration().getSeconds());
        LocalDateTime now = now();

        LOGGER.debug("Token '{}' expiry date time is '{}' vs now '{}'",tokenString,expiryDateTime,now);

        if(expiryDateTime.isBefore(now)) {
            LOGGER.debug("Token '{}' has expired",token);
            return false;
        }

        return true;

    }

    @Transactional
    public void resetPassword(String userName, String newPassword) {
        LOGGER.debug("Resetting password for user '{}'",userName);

        OAuthUser user = oAuthUserRepository.findByUsername(userName).get();
        updatePassword(user, newPassword);
    }

    @Transactional
    public void resetPasswordWithToken(String tokenValue, String newPassword) throws InvalidTokenException {

        LOGGER.debug("Resetting password for token '{}'",tokenValue);

        if(!isValidToken(tokenValue)) {
            throw new InvalidTokenException(tokenValue);
        }

        PasswordResetToken token = passwordResetTokenRepository.findById(tokenValue).get();

        token.setStatus(PasswordResetToken.Status.USED);
        passwordResetTokenRepository.save(token);

        updatePassword(token.getUser(), newPassword);
    }

    private void updatePassword(OAuthUser user, String password) {
        user.setPassword(encodePassword(password));
        oAuthUserRepository.save(user);
        LOGGER.debug("Password updated for user '{}'",user.getUsername());
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private Duration getResetTokenValidityDuration() {
        return environment.getProperty(CONFIG_RESET_TOKEN_VALIDITY_DURATION, Duration.class);
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.systemDefault());
    }

    private String generateUniqueUuid() {
        String uuid = uuidService.randomUuid();
        if(!passwordResetTokenRepository.existsById(uuid)) {
            return uuid;
        } else {
            return generateUniqueUuid();
        }
    }


}
