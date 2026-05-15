package com.github.enesusta.logback.openobserve.publisher;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperties;
import com.github.enesusta.logback.openobserve.domain.Property;
import com.github.enesusta.logback.openobserve.encoder.AbstractPropertyAndEncoder;
import com.github.enesusta.logback.openobserve.encoder.ClassicPropertyAndEncoder;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.IOException;
import java.util.Map;

public class DefaultOpenObservePublisher extends AbstractOpenObserverPublisher<ILoggingEvent> {

  public DefaultOpenObservePublisher(
      Context context,
      OpenObserveAppenderSettings openObserveAppenderSettings,
      OpenObserveHttpRequestHeaders headers,
      OpenObserveProperties properties,
      LogbackErrorReporter logbackErrorReporter,
      Runnable stopCallback)
      throws IOException {
    super(context, openObserveAppenderSettings, headers, properties, logbackErrorReporter, stopCallback);
  }

  @Override
  protected void serializeCommonFields(JsonGenerator gen, ILoggingEvent event) throws IOException {
    gen.writeNumberField("_timestamp", event.getTimeStamp() * 1000L);

    if (openObserveAppenderSettings.isRawJsonMessage()) {
      gen.writeFieldName("message");
      gen.writeRawValue(event.getFormattedMessage());
    } else {
      gen.writeObjectField("message", event.getFormattedMessage());
    }

    if (openObserveAppenderSettings.isIncludeMdc()) {
      for (Map.Entry<String, String> entry : event.getMDCPropertyMap().entrySet()) {
        gen.writeObjectField(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  protected AbstractPropertyAndEncoder<ILoggingEvent> buildPropertyAndEncoder(
      Context context, Property property) {
    return new ClassicPropertyAndEncoder(property, context);
  }
}
