package com.staircase13.apperta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender emailSender;

    private final String fromAddress;

    public EmailService(JavaMailSender emailSender, @Value("${apperta.email.from}") String fromAddress) {
        this.emailSender = emailSender;
        this.fromAddress = fromAddress;
    }

    public void send(SimpleMailMessage message) {
        message.setFrom(fromAddress);

        LOGGER.debug("Sending email with subject '{}' to '{}'",message.getSubject(), Arrays.asList(message.getTo()));

        emailSender.send(message);
    }
}
