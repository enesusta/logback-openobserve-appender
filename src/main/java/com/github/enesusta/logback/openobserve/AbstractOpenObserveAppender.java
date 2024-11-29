package com.github.enesusta.logback.openobserve;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperties;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;

public abstract class AbstractOpenObserveAppender<T> extends UnsynchronizedAppenderBase<T> {

  protected OpenObserveAppenderSettings openObserveAppenderSettings;
  protected OpenObserveHttpRequestHeaders headers;
  protected OpenObserveProperties properties;
  protected LogbackErrorReporter logbackErrorReporter;

  public AbstractOpenObserveAppender() {
    this.openObserveAppenderSettings = new OpenObserveAppenderSettings();
    this.headers = new OpenObserveHttpRequestHeaders();
    this.logbackErrorReporter = new LogbackErrorReporter(this.openObserveAppenderSettings, getContext());
  }

  public AbstractOpenObserveAppender(OpenObserveAppenderSettings openObserveAppenderSettings) {
    this.openObserveAppenderSettings = openObserveAppenderSettings;
  }

  protected abstract void appendInternal(T eventObject);

  @Override
  protected void append(T eventObject) {
    appendInternal(eventObject);
  }

  // VisibleForTesting
  protected LogbackErrorReporter getErrorReporter() {
    return this.logbackErrorReporter;
  }

  @Override
  public void start() {
    super.start();
  }
}
