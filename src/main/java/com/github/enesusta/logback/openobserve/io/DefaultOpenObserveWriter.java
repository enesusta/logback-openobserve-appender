package com.github.enesusta.logback.openobserve.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeader;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultOpenObserveWriter implements OpenObserveWriter {

  private final LogbackErrorReporter errorReporter;
  private final Collection<OpenObserveHttpRequestHeader> headers;
  private final OpenObserveAppenderSettings settings;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
    OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  }

  public DefaultOpenObserveWriter(
      final LogbackErrorReporter errorReporter,
      final OpenObserveAppenderSettings settings,
      final OpenObserveHttpRequestHeaders headers) {
    this.errorReporter = errorReporter;
    this.settings = settings;
    this.headers =
        headers != null && headers.getHeaders() != null
            ? headers.getHeaders()
            : Collections.<OpenObserveHttpRequestHeader>emptyList();
  }

  @Override
  public void write(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    final HttpURLConnection urlConnection =
        (HttpURLConnection) (settings.getUrl().openConnection());

    try {
      final Map<String, String> mappedHeaders =
          this.headers.stream()
              .collect(
                  Collectors.toMap(OpenObserveHttpRequestHeader::getName, item -> item.getValue()));

      urlConnection.setRequestProperty("Authorization", mappedHeaders.get("Authorization"));
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setReadTimeout(settings.getReadTimeout());
      urlConnection.setConnectTimeout(settings.getConnectTimeout());
      urlConnection.setRequestMethod("POST");

      OutputStream outputStream = urlConnection.getOutputStream();
      outputStream.write(byteArrayOutputStream.toByteArray());
      outputStream.flush();

      final int rc = urlConnection.getResponseCode();
    } finally {
      urlConnection.disconnect();
    }
  }
}
