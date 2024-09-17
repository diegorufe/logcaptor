package com.example.demo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LogCaptor implements AutoCloseable {

    private final LogAppender logAppender = new LogAppender();
    private final Class classLog;

    private LogCaptor(Class classLog) {
        this.classLog = classLog;
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        this.logAppender.setContext(loggerContext);
        this.logAppender.start();

        ch.qos.logback.classic.Logger classLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.classLog);
        classLogger.addAppender(this.logAppender);
        classLogger.setLevel(Level.TRACE);
    }

    public static LogCaptor capture(Class<?> classLog) {
        return new LogCaptor(classLog);
    }

    public LogCaptorVerifier verify(Level level) {
        final LogCaptorVerifier logCaptorVerifier = new LogCaptorVerifier();
        logCaptorVerifier.logCaptor = this;
        logCaptorVerifier.level = level;
        return logCaptorVerifier;
    }

    @Override
    public void close() {
        ch.qos.logback.classic.Logger classLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.classLog);
        classLogger.detachAppender(this.logAppender);
    }

    public static final class LogCaptorVerifier {
        private LogCaptor logCaptor;
        private Level level;

        private List<ILoggingEvent> getLoggingEvents(Level level){
            return Optional.ofNullable(this.logCaptor.logAppender.levelEvents.get(level)).orElse(List.of());
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
