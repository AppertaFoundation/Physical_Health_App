package com.staircase13.apperta.cms;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LoadLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadLog.class);

    private final List<LogEntry> logs;

    public LoadLog() {
        this.logs = new ArrayList<>();
    }

    public void addInfoLog(String message, Object... args) {
        String formattedMessage = String.format(message,args);
        LOGGER.info(formattedMessage);
        logs.add(LogEntry.builder().message(formattedMessage).severity(Severity.INFO).build());
    }

    public void addErrorLog(String message, Object... args) {
        String formattedMessage = String.format(message,args);
        LOGGER.error(formattedMessage);
        logs.add(LogEntry.builder().message(formattedMessage).severity(Severity.ERROR).build());
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    @Builder
    @Getter
    @Setter
    public static class LogEntry {
        private String message;
        private Severity severity;
    }

    public enum Severity {
        INFO,
        ERROR
    }

}
