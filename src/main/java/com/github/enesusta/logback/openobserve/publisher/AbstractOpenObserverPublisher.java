package com.github.enesusta.logback.openobserve.publisher;

import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractOpenObserverPublisher<T> implements Runnable {

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
  private final Runnable stopCallback;

  private final JacksonPropertySerializer propertySerializer;
  private final LinkedBlockingQueue<T> events;

  private final OpenObserveWriter openObserveWriter;

  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newScheduledThreadPool(1);

  public AbstractOpenObserverPublisher(
      final Context context,
      final OpenObserveAppenderSettings openObserveAppenderSettings,
      final OpenObserveHttpRequestHeaders headers,
      final OpenObserveProperties properties,
      final LogbackErrorReporter logbackErrorReporter,
      final Runnable stopCallback)
      throws IOException {

    this.errorReporter = logbackErrorReporter;
    this.openObserveAppenderSettings = openObserveAppenderSettings;
    this.stopCallback = stopCallback;

    this.events = new LinkedBlockingQueue<>(openObserveAppenderSettings.getMaxQueueSize());
    this.propertyList = generatePropertyList(context, properties);
    this.propertySerializer = new JacksonPropertySerializer();
    this.openObserveWriter =
        new DefaultOpenObserveWriter(logbackErrorReporter, openObserveAppenderSettings, headers);
  }

  private List<AbstractPropertyAndEncoder<T>> generatePropertyList(
      final Context context, final OpenObserveProperties properties) {
    final List<AbstractPropertyAndEncoder<T>> list = new ArrayList<>();
    if (properties != null) {
      for (final Property property : properties.getProperties()) {
        list.add(buildPropertyAndEncoder(context, property));
      }
    }
    return list;
  }

  public void addEvent(final T event) {
    if (!events.offer(event)) {
      errorReporter.logWarning("OpenObserve queue is full, dropping log event");
    }
  }

  private void writer() {
    final List<T> batch = new ArrayList<>();
    events.drainTo(batch);
    if (batch.isEmpty()) return;

    try {
      final JsonFactory jsonFactory = new JsonFactory();
      jsonFactory.setRootValueSeparator(null);

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final JsonGenerator jsonGenerator = jsonFactory.createGenerator(out);

      jsonGenerator.writeStartArray();
      for (final T event : batch) {
        jsonGenerator.writeRaw('\n');
        serializeEvent(jsonGenerator, event, propertyList);
        jsonGenerator.flush();
      }
      jsonGenerator.writeEndArray();
      jsonGenerator.close();

      if (openObserveAppenderSettings.isLogsToStderr()) {
        System.err.println(out.toString(StandardCharsets.UTF_8));
      }

      this.openObserveWriter.write(out);
    } catch (final IOException e) {
      errorReporter.logError("Cannot reach OpenObserve instance, stopping appender", e);
      scheduledExecutorService.shutdown();
      stopCallback.run();
    } catch (final Throwable e) {
      errorReporter.logError("Error serializing log events", e);
    }
  }

  @Override
  public void run() {
    try {
      final long initialDelay = this.openObserveAppenderSettings.getInitialDelay();
      final long delay = this.openObserveAppenderSettings.getDelay();
      final TimeUnit timeUnit = TimeUnit.valueOf(this.openObserveAppenderSettings.getTimeUnit());
      scheduledExecutorService.scheduleAtFixedRate(this::writer, initialDelay, delay, timeUnit);
    } catch (Exception e) {
      errorReporter.logError("Failed to start OpenObserve scheduler, stopping appender", e);
      stopCallback.run();
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
