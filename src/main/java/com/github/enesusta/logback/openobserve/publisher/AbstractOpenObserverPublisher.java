package com.github.enesusta.logback.openobserve.publisher;

import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperty;
import com.github.enesusta.logback.openobserve.encoder.AbstractPropertyAndEncoder;
import java.io.IOException;
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

  protected abstract void serializeCommonFields(JsonGenerator gen, T event) throws IOException;

  protected abstract AbstractPropertyAndEncoder<T> buildPropertyAndEncoder(
      Context context, OpenObserveProperty property);

  public static final String THREAD_NAME_PREFIX = "openobserve-writer-";

  private volatile List<T> events;
  private JsonFactory jf;
  private JsonGenerator jsonGenerator;

  protected OpenObserveAppenderSettings openObserveAppenderSettings;
}
