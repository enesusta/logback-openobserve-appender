package com.github.enesusta.logback.openobserve;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.publisher.AbstractOpenObserverPublisher;
import com.github.enesusta.logback.openobserve.publisher.DefaultOpenObservePublisher;
import java.io.IOException;

public class OpenObserveAppender extends AbstractOpenObserveAppender<ILoggingEvent> {

  public OpenObserveAppender() {}

  public OpenObserveAppender(OpenObserveAppenderSettings settings) {
    super(settings);
  }

  @Override
  protected void appendInternal(final ILoggingEvent eventObject) {
    String targetLogger = eventObject.getLoggerName();
    String loggerName = settings.getLoggerName();
    if (loggerName != null && loggerName.equals(targetLogger)) {
      return;
    }

    String errorLoggerName = settings.getErrorLoggerName();
    if (errorLoggerName != null && errorLoggerName.equals(targetLogger)) {
      return;
    }

    eventObject.prepareForDeferredProcessing();
    if (settings.isIncludeCallerData()) {
      eventObject.getCallerData();
    }

    publishEvent(eventObject);
  }

  @Override
  protected AbstractOpenObserverPublisher<ILoggingEvent> populatePublisher() throws IOException {
    return new DefaultOpenObservePublisher(
        getContext(), settings, headers, properties, logbackErrorReporter);
  }
}

// String targetLogger = eventObject.getLoggerName();
// try {
// var objectMapper = new ObjectMapper();
// System.out.println(targetLogger);
// System.out.println(objectMapper.writeValueAsString(settings));
// System.out.println(objectMapper.writeValueAsString(eventObject));
// } catch (JsonProcessingException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
