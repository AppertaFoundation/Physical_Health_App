package com.staircase13.apperta.service.util;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.staircase13.apperta.config.DateTimeConfig.DEFAULT_TIME_ZONE;

public class DateTimeUtil {

    public static long toEpoch(LocalDateTime localDateTime) {
        return localDateTime.atZone(DEFAULT_TIME_ZONE.toZoneId()).toInstant().toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), DEFAULT_TIME_ZONE.toZoneId());
    }
}
