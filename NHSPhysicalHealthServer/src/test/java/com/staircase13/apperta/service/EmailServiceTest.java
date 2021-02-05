package com.staircase13.apperta.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.verify;

public class EmailServiceTest {

    private static final String FROM_ADDRESS = "from@localhost";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageCaptor;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(emailService,"fromAddress", FROM_ADDRESS);
    }

    @Test
    public void send_fromAddressSet() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("to@localhost");
        emailService.send(message);

        verify(javaMailSender).send(simpleMailMessageCaptor.capture());

        SimpleMailMessage sentMessage = simpleMailMessageCaptor.getValue();
        assertThat(sentMessage, notNullValue());
        assertThat(sentMessage.getFrom(), is(FROM_ADDRESS));
    }

}
