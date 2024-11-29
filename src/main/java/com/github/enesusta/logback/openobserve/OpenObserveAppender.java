package com.github.enesusta.logback.openobserve;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;

public class OpenObserveAppender extends AbstractOpenObserveAppender<ILoggingEvent> {

  private final OpenObserveAppenderSettings openObserveAppenderSettings;

  public OpenObserveAppender() {
    this.openObserveAppenderSettings = new OpenObserveAppenderSettings();
  }

  @Override
  protected void appendInternal(final ILoggingEvent eventObject) {
    try {
      final String targetLogger = eventObject.getLoggerName();
      final String loggerName = openObserveAppenderSettings.getLoggerName();
      if (loggerName != null && loggerName.equals(targetLogger)) {
        return;
      }

      final String errorLoggerName = openObserveAppenderSettings.getErrorLoggerName();
      if (errorLoggerName != null && errorLoggerName.equals(targetLogger)) {
        return;
      }

      eventObject.prepareForDeferredProcessing();

      final var objectMapper = new ObjectMapper();
      System.out.println(targetLogger);
      System.out.println(objectMapper.writeValueAsString(openObserveAppenderSettings));
      System.out.println(objectMapper.writeValueAsString(eventObject));
    } catch (final JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
