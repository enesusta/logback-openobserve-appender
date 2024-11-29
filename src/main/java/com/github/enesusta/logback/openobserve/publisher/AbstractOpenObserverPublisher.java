package com.github.enesusta.logback.openobserve.publisher;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractOpenObserverPublisher<T> implements Runnable {

  private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
  private static final ThreadLocal<DateFormat> DATE_FORMAT =
      new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
      };

  public static final String THREAD_NAME_PREFIX = "openobserve-writer-";

  private volatile List<T> events;
  private JsonFactory jf;
  private JsonGenerator jsonGenerator;

  protected OpenObserveAppenderSettings openObserveAppenderSettings;
}
