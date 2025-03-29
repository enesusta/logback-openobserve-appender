package com.github.enesusta.logback.openobserve.publisher;

import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperties;
import com.github.enesusta.logback.openobserve.domain.Property;
import com.github.enesusta.logback.openobserve.encoder.AbstractPropertyAndEncoder;
import com.github.enesusta.logback.openobserve.io.DefaultOpenObserveWriter;
import com.github.enesusta.logback.openobserve.io.OpenObserveWriter;
import com.github.enesusta.logback.openobserve.jackson.JacksonPropertySerializer;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOpenObserverPublisher<T> implements Runnable {

  private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
  public static final String THREAD_NAME_PREFIX = "openobserve-writer-";
  private static final ThreadLocal<DateFormat> DATE_FORMAT =
      new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
          return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
      };

  protected abstract void serializeCommonFields(JsonGenerator gen, T event) throws IOException;

  protected abstract AbstractPropertyAndEncoder<T> buildPropertyAndEncoder(
      Context context, Property property);

  private final List<AbstractPropertyAndEncoder<T>> propertyList;

  private final LogbackErrorReporter errorReporter;
  protected OpenObserveAppenderSettings openObserveAppenderSettings;

  private final JacksonPropertySerializer propertySerializer;
  private final List<T> events;

  private final OpenObserveWriter openObserveWriter;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  public AbstractOpenObserverPublisher(
      final Context context,
      final OpenObserveAppenderSettings openObserveAppenderSettings,
      final OpenObserveHttpRequestHeaders headers,
      final OpenObserveProperties properties,
      final LogbackErrorReporter logbackErrorReporter)
      throws IOException {

    this.errorReporter = logbackErrorReporter;
    this.openObserveAppenderSettings = openObserveAppenderSettings;

    this.events = new CopyOnWriteArrayList<T>();
    this.propertyList = generatePropertyList(context, properties);
    this.propertySerializer = new JacksonPropertySerializer();
    this.openObserveWriter =
        new DefaultOpenObserveWriter(logbackErrorReporter, openObserveAppenderSettings, headers);
  }

  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newScheduledThreadPool(1);

  private List<AbstractPropertyAndEncoder<T>> generatePropertyList(
      final Context context, final OpenObserveProperties properties) {
    final List<AbstractPropertyAndEncoder<T>> list = new ArrayList<AbstractPropertyAndEncoder<T>>();
    if (properties != null) {
      for (final Property property : properties.getProperties()) {
        list.add(buildPropertyAndEncoder(context, property));
      }
    }
    return list;
  }

  public void addEvent(final T event) {
    events.add(event);
  }

  private void writer() {
    try {
      LoggerFactory.getLogger(this.openObserveAppenderSettings.getLoggerName())
          .info("%t ITS WORKING");
      final JsonFactory jsonFactory = new JsonFactory();
      jsonFactory.setRootValueSeparator(null);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final JsonGenerator jsonGenerator = jsonFactory.createGenerator(out);

      jsonGenerator.writeStartArray();
      for (final T event : events) {
        jsonGenerator.writeRaw('\n');
        serializeEvent(jsonGenerator, event, propertyList);
        jsonGenerator.flush();
      }

      jsonGenerator.writeEndArray();
      jsonGenerator.close();

      if (events.size() >= 1) this.openObserveWriter.write(out);
      events.clear();
    } catch (final Throwable e) {
      LoggerFactory.getLogger(this.openObserveAppenderSettings.getErrorLoggerName())
          .error("Some error occurred in [wrtier]", e);
    }
  }

  @Override
  public void run() {
    try {
      LoggerFactory.getLogger(this.openObserveAppenderSettings.getLoggerName())
          .info("STARTED {}", new Date());
      final long initialDelay = this.openObserveAppenderSettings.getInitialDelay();
      final long delay = this.openObserveAppenderSettings.getDelay();
      final TimeUnit timeUnit = TimeUnit.valueOf(this.openObserveAppenderSettings.getTimeUnit());

      scheduledExecutorService.scheduleAtFixedRate(
          () -> {
            writer();
          },
          initialDelay,
          delay,
          timeUnit);
    } catch (Exception e) {
      LoggerFactory.getLogger(this.openObserveAppenderSettings.getErrorLoggerName())
          .error("Some error occurred in [DAEMON_THREAD]", e);
    }
  }

  private void serializeEvent(
      final JsonGenerator gen,
      final T event,
      final List<AbstractPropertyAndEncoder<T>> propertyList)
      throws IOException {
    gen.writeStartObject();
    serializeCommonFields(gen, event);

    for (final AbstractPropertyAndEncoder<T> propertyAndEncoder : propertyList)
      propertySerializer.serializeProperty(gen, event, propertyAndEncoder);

    gen.writeEndObject();
  }

  protected static String getTimestamp(final long timestamp) {
    return DATE_FORMAT.get().format(new Date(timestamp));
  }
}
