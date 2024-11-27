package com.github.enesusta.logback.openobserve;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;

public class OpenObserveAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private OpenObserveAppenderSettings openObserveAppenderSettings;

  public OpenObserveAppender() {
    this.openObserveAppenderSettings = new OpenObserveAppenderSettings();
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    String targetLogger = eventObject.getLoggerName();
    try {
      var objectMapper = new ObjectMapper();
      System.out.println(targetLogger);
      System.out.println(objectMapper.writeValueAsString(openObserveAppenderSettings));
      System.out.println(objectMapper.writeValueAsString(eventObject));
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
