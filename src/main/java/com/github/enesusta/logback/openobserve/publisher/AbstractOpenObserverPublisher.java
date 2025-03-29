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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

  private List<AbstractPropertyAndEncoder<T>> propertyList;

  private LogbackErrorReporter errorReporter;
  protected OpenObserveAppenderSettings openObserveAppenderSettings;

  private final JacksonPropertySerializer propertySerializer;
  private final Queue<T> events = new ArrayBlockingQueue<>(100);

  private final OpenObserveWriter openObserveWriter;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  public AbstractOpenObserverPublisher(
      Context context,
      OpenObserveAppenderSettings openObserveAppenderSettings,
      OpenObserveHttpRequestHeaders headers,
      OpenObserveProperties properties,
      LogbackErrorReporter logbackErrorReporter)
      throws IOException {

    this.errorReporter = logbackErrorReporter;
    this.openObserveAppenderSettings = openObserveAppenderSettings;
    this.propertyList = generatePropertyList(context, properties);
    this.propertySerializer = new JacksonPropertySerializer();
    this.openObserveWriter =
        new DefaultOpenObserveWriter(logbackErrorReporter, openObserveAppenderSettings, headers);
  }

  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newScheduledThreadPool(1);

  private List<AbstractPropertyAndEncoder<T>> generatePropertyList(
      Context context, OpenObserveProperties properties) {
    List<AbstractPropertyAndEncoder<T>> list = new ArrayList<AbstractPropertyAndEncoder<T>>();
    if (properties != null) {
      for (Property property : properties.getProperties()) {
        list.add(buildPropertyAndEncoder(context, property));
      }
    }
    return list;
  }

  public void addEvent(T event) {
    events.add(event);
  }

  public void callback() {
    try {
      final JsonFactory jsonFactory = new JsonFactory();
      jsonFactory.setRootValueSeparator(null);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final JsonGenerator jsonGenerator = jsonFactory.createGenerator(out);

      jsonGenerator.writeStartArray();
      for (T event : events) {
        jsonGenerator.writeRaw('\n');
        serializeEvent(jsonGenerator, event, propertyList);
        jsonGenerator.flush();
      }

      jsonGenerator.writeEndArray();
      jsonGenerator.close();

      if (events.size() >= 1) {
        this.openObserveWriter.write(out);
      }

      events.clear();
    } catch (final Throwable e) {
    }
  }

  @Override
  public void run() {
    try {
      scheduledExecutorService.scheduleAtFixedRate(
          () -> {
            callback();
          },
          2,
          1,
          TimeUnit.SECONDS);

    } catch (final Exception e) {
    }
  }

  private void serializeEvent(
      JsonGenerator gen, T event, List<AbstractPropertyAndEncoder<T>> propertyList)
      throws IOException {
    gen.writeStartObject();
    serializeCommonFields(gen, event);

    for (AbstractPropertyAndEncoder<T> propertyAndEncoder : propertyList)
      propertySerializer.serializeProperty(gen, event, propertyAndEncoder);

    gen.writeEndObject();
  }

  protected static String getTimestamp(long timestamp) {
    return DATE_FORMAT.get().format(new Date(timestamp));
  }
}
