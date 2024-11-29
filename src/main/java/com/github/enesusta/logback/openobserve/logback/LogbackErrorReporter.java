package com.github.enesusta.logback.openobserve.logback;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import org.slf4j.LoggerFactory;

public class LogbackErrorReporter extends ContextAwareBase {

  private OpenObserveAppenderSettings settings;

  public LogbackErrorReporter(OpenObserveAppenderSettings settings, Context context) {
    setContext(context);
    this.settings = settings;
  }

  public void logError(String message, Throwable e) {
    String loggerName = settings.getErrorLoggerName();
    if (loggerName != null) {
      LoggerFactory.getLogger(loggerName).error(message, e);
    }
    addError(message, e);
  }

  public void logWarning(String message) {
    String loggerName = settings.getErrorLoggerName();
    if (loggerName != null) {
      LoggerFactory.getLogger(loggerName).warn(message);
    }
    addWarn(message);
  }

  public void logInfo(String message) {
    String loggerName = settings.getErrorLoggerName();
    if (loggerName != null) {
      LoggerFactory.getLogger(loggerName).info(message);
    }
    addInfo(message);
  }
}
