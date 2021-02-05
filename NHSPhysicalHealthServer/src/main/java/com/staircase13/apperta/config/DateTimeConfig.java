package com.staircase13.apperta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.util.TimeZone;

@Configuration
public class DateTimeConfig {

    public static TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("UTC");

    @PostConstruct
    void setUTCTimezone() {
        /**
         * This ensures that all Local Date Times written/read to/from the database are in UTC
         */
        TimeZone.setDefault(DEFAULT_TIME_ZONE);
    }

    @Bean
    public Clock clock() {
        return Clock.system(DEFAULT_TIME_ZONE.toZoneId());
    }

}
