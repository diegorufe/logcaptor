package com.example.demo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LogCaptor implements AutoCloseable {

    private final Map<Class<?>, LogAppender> logsAppender = new HashMap<>();

    private LogCaptor(Class<?>... classesLog) {
        Objects.requireNonNull(classesLog);
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        for (Class<?> classLog : classesLog) {
            final LogAppender logAppender = new LogAppender();
            logAppender.setContext(loggerContext);
            logAppender.start();

            ch.qos.logback.classic.Logger classLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(classLog);
            classLogger.addAppender(logAppender);
            classLogger.setLevel(Level.TRACE);

            this.logsAppender.put(classLog, logAppender);
        }
    }

    public static LogCaptor capture(Class<?>... classesLog) {
        return new LogCaptor(classesLog);
    }

    public LogCaptorVerifier verify(Class<?> classLog, Level level) {
        Objects.requireNonNull(classLog);
        final LogCaptorVerifier logCaptorVerifier = new LogCaptorVerifier();
        logCaptorVerifier.level = level;
        logCaptorVerifier.logAppender = this.logsAppender.get(classLog);
        Objects.requireNonNull(logCaptorVerifier.logAppender);
        return logCaptorVerifier;
    }

    @Override
    public void close() {
        this.logsAppender.forEach((k, v) -> {
            ch.qos.logback.classic.Logger classLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(k);
            classLogger.detachAppender(v);
        });
    }

    public static final class LogCaptorVerifier {
        private Level level;
        private LogAppender logAppender;

        private List<ILoggingEvent> getLoggingEvents(Level level) {
            return Optional.ofNullable(this.logAppender.levelEvents.get(level)).orElse(List.of());
        }

        public boolean contains(final String value) {
            return this.getLoggingEvents(level).stream().anyMatch((loggingEvent -> loggingEvent.getMessage().contains(value)));
        }

        public boolean equals(final String value) {
            return this.getLoggingEvents(level).stream().anyMatch((loggingEvent -> loggingEvent.getMessage().equals(value)));
        }
    }

    private static final class LogAppender extends AppenderBase<ILoggingEvent> {

        private final Map<Level, List<ILoggingEvent>> levelEvents = new HashMap<>();

        @Override
        protected void append(ILoggingEvent loggingEvent) {
            final List<ILoggingEvent> events = levelEvents.computeIfAbsent(loggingEvent.getLevel(), k -> new ArrayList<>());
            events.add(loggingEvent);
        }

    }

}
