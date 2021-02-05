package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that integrates with 3rd party push notification libraries.
 *
 * Note: This is a placeholder implementation to be replaced with an actual push service.
 * Push notifications were descoped from the requirements so the integration is TBC.
 */
@Service
public class PushNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushNotificationService.class);

    public static List<String> sentMessages = new ArrayList<>();

    public void send(User user, String message) {

        LOGGER.info("Sending message '{}' to '{}'", message, user.getUsername());

        // Temporary solution to allow us to integration test that messages are actually sent
        sentMessages.add(message);
    }

}
