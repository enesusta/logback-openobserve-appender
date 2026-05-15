package com.github.enesusta.logback.openobserve;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.publisher.AbstractOpenObserverPublisher;
import com.github.enesusta.logback.openobserve.publisher.DefaultOpenObservePublisher;
import java.io.IOException;

public class OpenObserveAppender extends AbstractOpenObserveAppender<ILoggingEvent> {

  public OpenObserveAppender() {
  }

  public OpenObserveAppender(final OpenObserveAppenderSettings settings) {
    super(settings);
  }

  @Override
  protected void appendInternal(final ILoggingEvent eventObject) {
    final String targetLogger = eventObject.getLoggerName();
    final String loggerName = settings.getLoggerName();
    if (loggerName != null && loggerName.equals(targetLogger))
      return;

    final String errorLoggerName = settings.getErrorLoggerName();
    if (errorLoggerName != null && errorLoggerName.equals(targetLogger))
      return;

    eventObject.prepareForDeferredProcessing();
    if (settings.isIncludeCallerData())
      eventObject.getCallerData();

    publishEvent(eventObject);
  }

  @Override
  protected AbstractOpenObserverPublisher<ILoggingEvent> populatePublisher() throws IOException {
    return new DefaultOpenObservePublisher(
        getContext(), settings, headers, properties, logbackErrorReporter, this::stop);
  }
}
