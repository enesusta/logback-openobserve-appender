package com.github.enesusta.logback.openobserve;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;

public abstract class AbstractOpenObserveAppender<T> extends UnsynchronizedAppenderBase<T> {

  private OpenObserveAppenderSettings openObserveAppenderSettings;

  public AbstractOpenObserveAppender() {
    this.openObserveAppenderSettings = new OpenObserveAppenderSettings();
  }

  protected abstract void appendInternal(T eventObject);

  @Override
  protected void append(T eventObject) {
    appendInternal(eventObject);
  }

  @Override
  public void start() {
    super.start();
  }
}
