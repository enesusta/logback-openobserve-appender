package com.github.enesusta.logback.openobserve.io;

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

public class DefaultOpenObserveWriter implements OpenObserveWriter {

  private final LogbackErrorReporter errorReporter;
  private final Collection<OpenObserveHttpRequestHeader> headers;
  private final OpenObserveAppenderSettings settings;

  public DefaultOpenObserveWriter(
      final LogbackErrorReporter errorReporter,
      final OpenObserveAppenderSettings settings,
      final OpenObserveHttpRequestHeaders headers) {
    this.errorReporter = errorReporter;
    this.settings = settings;
    this.headers =
        headers != null && headers.getHeaders() != null
            ? headers.getHeaders()
            : Collections.emptyList();
  }

  @Override
  public void write(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    final HttpURLConnection urlConnection =
        (HttpURLConnection) settings.getUrl().openConnection();

    try {
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setReadTimeout(settings.getReadTimeout());
      urlConnection.setConnectTimeout(settings.getConnectTimeout());
      urlConnection.setRequestMethod("POST");

      for (OpenObserveHttpRequestHeader header : headers) {
        urlConnection.setRequestProperty(header.getName(), header.getValue());
      }

      OutputStream outputStream = urlConnection.getOutputStream();
      outputStream.write(byteArrayOutputStream.toByteArray());
      outputStream.flush();

      final int rc = urlConnection.getResponseCode();
      if (rc < 200 || rc >= 300) {
        errorReporter.logWarning("OpenObserve responded with HTTP " + rc + ", batch dropped");
      }
    } finally {
      urlConnection.disconnect();
    }
  }
}
