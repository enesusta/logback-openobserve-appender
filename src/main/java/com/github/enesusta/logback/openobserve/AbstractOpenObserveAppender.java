package com.github.enesusta.logback.openobserve;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperties;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import com.github.enesusta.logback.openobserve.publisher.AbstractOpenObserverPublisher;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractOpenObserveAppender<T> extends UnsynchronizedAppenderBase<T> {

  protected OpenObserveAppenderSettings settings;
  protected OpenObserveHttpRequestHeaders headers;
  protected OpenObserveProperties properties;

  protected LogbackErrorReporter logbackErrorReporter;
  protected AbstractOpenObserverPublisher<T> publisher;

  public AbstractOpenObserveAppender() {
    this.settings = new OpenObserveAppenderSettings();
    this.headers = new OpenObserveHttpRequestHeaders();
  }

  public AbstractOpenObserveAppender(OpenObserveAppenderSettings openObserveAppenderSettings) {
    this.settings = openObserveAppenderSettings;
  }

  protected abstract void appendInternal(T eventObject);

  protected abstract AbstractOpenObserverPublisher<T> populatePublisher() throws IOException;

  @Override
  protected void append(T eventObject) {
    appendInternal(eventObject);
  }

  protected void publishEvent(T eventObject) {
    publisher.addEvent(eventObject);
  }

  // VisibleForTesting
  protected LogbackErrorReporter getErrorReporter() {
    return new LogbackErrorReporter(settings, context);
  }

  @Override
  public void start() {
    super.start();
    this.logbackErrorReporter = getErrorReporter();
    try {
      this.publisher = populatePublisher();
      Thread t = new Thread(this.publisher);
      t.setDaemon(true);
      t.start();
    } catch (IOException e) {
    }
  }

  public void setProperties(OpenObserveProperties openObserveProperties) {
    this.properties = openObserveProperties;
  }

  public void setInitialDelay(long initialDelay) {
    settings.setInitialDelay(initialDelay);
  }

  public void setDelay(long delay) {
    settings.setDelay(delay);
  }

  public void setTimeUnit(String timeUnit) {
    settings.setTimeUnit(timeUnit);
  }

  public void setSleepTime(int sleepTime) {
    settings.setSleepTime(sleepTime);
  }

  public void setMaxRetries(int maxRetries) {
    settings.setMaxRetries(maxRetries);
  }

  public void setConnectTimeout(int connectTimeout) {
    settings.setConnectTimeout(connectTimeout);
  }

  public void setReadTimeout(int readTimeout) {
    settings.setReadTimeout(readTimeout);
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    settings.setIncludeCallerData(includeCallerData);
  }

  public void setErrorsToStderr(boolean errorsToStderr) {
    settings.setErrorsToStderr(errorsToStderr);
  }

  public void setLogsToStderr(boolean logsToStderr) {
    settings.setLogsToStderr(logsToStderr);
  }

  public void setMaxQueueSize(int maxQueueSize) {
    settings.setMaxQueueSize(maxQueueSize);
  }

  public void setType(String type) {
    settings.setType(type);
  }

  public void setUrl(String url) throws MalformedURLException {
    settings.setUrl(new URL(url));
  }

  public void setLoggerName(String logger) {
    settings.setLoggerName(logger);
  }

  public void setErrorLoggerName(String logger) {
    settings.setErrorLoggerName(logger);
  }

  public void setHeaders(OpenObserveHttpRequestHeaders openObserveHttpRequestHeaders) {
    this.headers = openObserveHttpRequestHeaders;
  }

  public void setRawJsonMessage(boolean rawJsonMessage) {
    settings.setRawJsonMessage(rawJsonMessage);
  }

  public void setIncludeMdc(boolean includeMdc) {
    settings.setIncludeMdc(includeMdc);
  }

  public void setMaxMessageSize(int maxMessageSize) {
    settings.setMaxMessageSize(maxMessageSize);
  }
}
