package com.github.enesusta.logback.openobserve.publisher;

import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.enesusta.logback.openobserve.OpenObserveOutputAggregator;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperties;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperty;
import com.github.enesusta.logback.openobserve.encoder.AbstractPropertyAndEncoder;
import com.github.enesusta.logback.openobserve.jackson.JacksonPropertySerializer;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
  private OpenObserveOutputAggregator outputAggregator;
  private List<AbstractPropertyAndEncoder<T>> propertyList;

  private AbstractPropertyAndEncoder<T> indexPattern;
  private JsonFactory jf;
  private JsonGenerator jsonGenerator;

  private LogbackErrorReporter errorReporter;
  protected OpenObserveAppenderSettings openObserveAppenderSettings;

  private final Object lock;
  private volatile boolean working;

  private final JacksonPropertySerializer propertySerializer;

  public AbstractOpenObserverPublisher(
      Context context,
      OpenObserveAppenderSettings openObserveAppenderSettings,
      OpenObserveHttpRequestHeaders headers,
      OpenObserveProperties properties,
      LogbackErrorReporter logbackErrorReporter)
      throws IOException {

    this.errorReporter = logbackErrorReporter;
    this.events = new ArrayList<T>();
    this.lock = new Object();
    this.openObserveAppenderSettings = openObserveAppenderSettings;
    this.outputAggregator =
        openObserveAppenderSettings.populateAggregator(logbackErrorReporter, headers);

    this.jf = new JsonFactory();
    this.jf.setRootValueSeparator(null);
    this.jsonGenerator = jf.createGenerator(outputAggregator);

    this.indexPattern =
        buildPropertyAndEncoder(
            context,
            new OpenObserveProperty("<index>", openObserveAppenderSettings.getIndex(), false));
    this.propertyList = generatePropertyList(context, properties);
    this.propertySerializer = new JacksonPropertySerializer();
  }

  private List<AbstractPropertyAndEncoder<T>> generatePropertyList(
      Context context, OpenObserveProperties properties) {
    List<AbstractPropertyAndEncoder<T>> list = new ArrayList<AbstractPropertyAndEncoder<T>>();
    if (properties != null) {
      for (OpenObserveProperty property : properties.getProperties()) {
        list.add(buildPropertyAndEncoder(context, property));
      }
    }
    return list;
  }

  public void addEvent(T event) {
    System.out.println("addEvent -1");
    if (!outputAggregator.hasOutputs()) {
      return;
    }
    System.out.println("addEvent -2");

    synchronized (lock) {
      events.add(event);
      if (!working) {
        working = true;
        Thread thread = new Thread(this, THREAD_NAME_PREFIX + THREAD_COUNTER.getAndIncrement());
        thread.start();
      }
    }
  }

  public void run() {
    int currentTry = 1;
    int maxRetries = openObserveAppenderSettings.getMaxRetries();
    while (true) {
      try {
        Thread.sleep(openObserveAppenderSettings.getSleepTime());

        List<T> eventsCopy = null;
        synchronized (lock) {
          if (!events.isEmpty()) {
            eventsCopy = events;
            events = new ArrayList<T>();
            currentTry = 1;
          }

          if (eventsCopy == null) {
            if (!outputAggregator.hasPendingData()) {
              // all done
              working = false;
              return;
            } else {
              // Nothing new, must be a retry
              if (currentTry > maxRetries) {
                // Oh well, better luck next time
                working = false;
                return;
              }
            }
          }
        }

        if (eventsCopy != null) {
          serializeEvents(jsonGenerator, eventsCopy, propertyList);
        }

        if (!outputAggregator.sendData()) {
          currentTry++;
        }
      } catch (Exception e) {
        System.out.println("Internal error " + e);
        errorReporter.logError("Internal error handling log data: " + e.getMessage(), e);
        currentTry++;
      }
    }
  }

  private void serializeEvents(
      JsonGenerator gen, List<T> eventsCopy, List<AbstractPropertyAndEncoder<T>> propertyList)
      throws IOException {
    for (T event : eventsCopy) {
      // serializeIndexString(gen, event);
      // gen.writeRaw('\n');
      serializeEvent(gen, event, propertyList);
      gen.writeRaw(',');
    }
    gen.flush();
  }

  private void serializeIndexString(JsonGenerator gen, T event) throws IOException {
    gen.writeStartObject();
    gen.writeObjectFieldStart("index");
    gen.writeObjectField("_index", indexPattern.encode(event));
    String type = openObserveAppenderSettings.getType();
    if (type != null) {
      gen.writeObjectField("_type", type);
    }
    gen.writeEndObject();
    gen.writeEndObject();
  }

  private void serializeEvent(
      JsonGenerator gen, T event, List<AbstractPropertyAndEncoder<T>> propertyList)
      throws IOException {
    gen.writeStartObject();

    serializeCommonFields(gen, event);

    for (AbstractPropertyAndEncoder<T> pae : propertyList) {
      propertySerializer.serializeProperty(gen, event, pae);
    }

    gen.writeEndObject();
  }

  protected static String getTimestamp(long timestamp) {
    return DATE_FORMAT.get().format(new Date(timestamp));
  }
}
