package com.github.enesusta.logback.openobserve.domain;

import java.net.URL;

public class OpenObserveAppenderSettings {
  private String type;
  private URL url;

  private String loggerName;
  private String errorLoggerName;

  private int sleepTime = 250;
  private int maxRetries = 3;
  private int connectTimeout = 30000;
  private int readTimeout = 30000;

  private long initialDelay = 0;
  private long delay = 1;
  private String timeUnit = "SECONDS";

  private int maxQueueSize = 100 * 1024 * 1024;
  private int maxMessageSize = -1;

  private boolean logsToStderr;
  private boolean errorsToStderr;
  private boolean includeCallerData;
  private boolean includeMdc;
  private boolean rawJsonMessage;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public void setLoggerName(String loggerName) {
    this.loggerName = loggerName;
  }

  public String getErrorLoggerName() {
    return errorLoggerName;
  }

  public void setErrorLoggerName(String errorLoggerName) {
    this.errorLoggerName = errorLoggerName;
  }

  public int getSleepTime() {
    return sleepTime;
  }

  public void setSleepTime(int sleepTime) {
    this.sleepTime = sleepTime;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public boolean isLogsToStderr() {
    return logsToStderr;
  }

  public void setLogsToStderr(boolean logsToStderr) {
    this.logsToStderr = logsToStderr;
  }

  public boolean isErrorsToStderr() {
    return errorsToStderr;
  }

  public void setErrorsToStderr(boolean errorsToStderr) {
    this.errorsToStderr = errorsToStderr;
  }

  public boolean isIncludeCallerData() {
    return includeCallerData;
  }

  public void setIncludeCallerData(boolean includeCallerData) {
    this.includeCallerData = includeCallerData;
  }

  public boolean isIncludeMdc() {
    return includeMdc;
  }

  public void setIncludeMdc(boolean includeMdc) {
    this.includeMdc = includeMdc;
  }

  public boolean isRawJsonMessage() {
    return rawJsonMessage;
  }

  public void setRawJsonMessage(boolean rawJsonMessage) {
    this.rawJsonMessage = rawJsonMessage;
  }

  public int getMaxQueueSize() {
    return maxQueueSize;
  }

  public void setMaxQueueSize(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
  }

  public int getMaxMessageSize() {
    return maxMessageSize;
  }

  public void setMaxMessageSize(int maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  public long getInitialDelay() {
    return initialDelay;
  }

  public void setInitialDelay(long initialDelay) {
    this.initialDelay = initialDelay;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public String getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }
}
