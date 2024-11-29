package com.github.enesusta.logback.openobserve.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackOpenObserveWriter implements OpenObserveWriter {

  private String loggerName;

  private Logger logger;

  public LogbackOpenObserveWriter(String loggerName) {
    this.loggerName = loggerName;
  }

  public void write(char[] cbuf, int off, int len) {
    if (logger == null) {
      logger = LoggerFactory.getLogger(loggerName);
    }
    logger.info(new String(cbuf, 0, len));
  }

  public void sendData() {
    // No-op
  }

  public boolean hasPendingData() {
    return false;
  }
}
