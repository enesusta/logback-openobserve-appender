package com.github.enesusta.logback.openobserve.io;

import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeader;
import com.github.enesusta.logback.openobserve.domain.OpenObserveHttpRequestHeaders;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;

public class DefaultOpenObserveWriter implements OpenObserveWriter {

  private final StringBuilder sendBuffer;
  private final LogbackErrorReporter errorReporter;
  private final Collection<OpenObserveHttpRequestHeader> headers;
  private final OpenObserveAppenderSettings settings;

  private boolean bufferExceeded;

  public DefaultOpenObserveWriter(
      final LogbackErrorReporter errorReporter,
      final OpenObserveAppenderSettings settings,
      final OpenObserveHttpRequestHeaders headers) {
    this.errorReporter = errorReporter;
    this.settings = settings;
    this.headers = headers != null && headers.getHeaders() != null
        ? headers.getHeaders()
        : Collections.<OpenObserveHttpRequestHeader>emptyList();
    this.sendBuffer = new StringBuilder();
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) {
    if (bufferExceeded) {
      return;
    }

    sendBuffer.append(cbuf, off, len);

    if (sendBuffer.length() >= settings.getMaxQueueSize()) {
      errorReporter.logWarning(
          "Send queue maximum size exceeded - log messages will be lost until the buffer is"
              + " cleared");
      bufferExceeded = true;
    }
  }

  @Override
  public void sendData() throws IOException {
    if (sendBuffer.length() <= 0) {
      return;
    }

    final HttpURLConnection urlConnection = (HttpURLConnection) (settings.getUrl().openConnection());
    try {
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setReadTimeout(settings.getReadTimeout());
      urlConnection.setConnectTimeout(settings.getConnectTimeout());
      urlConnection.setRequestMethod("POST");

      final String body = sendBuffer.toString();

      if (!headers.isEmpty()) {
        for (final OpenObserveHttpRequestHeader header : headers) {
          urlConnection.setRequestProperty(header.getName(), header.getValue());
        }
      }

      // if (settings.getAuthentication() != null) {
      // settings.getAuthentication().addAuth(urlConnection, body);
      // }

      final Writer writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
      writer.write(body);
      writer.flush();
      writer.close();

      final int rc = urlConnection.getResponseCode();
      if (rc != 200) {
        final String data = slurpErrors(urlConnection);
        throw new IOException("Got response code [" + rc + "] from server with data " + data);
      }
    } finally {
      urlConnection.disconnect();
    }

    sendBuffer.setLength(0);
    if (bufferExceeded) {
      errorReporter.logInfo("Send queue cleared - log messages will no longer be lost");
      bufferExceeded = false;
    }
  }

  @Override
  public boolean hasPendingData() {
    return sendBuffer.length() != 0;
  }

  private static String slurpErrors(final HttpURLConnection urlConnection) {
    try {
      final InputStream stream = urlConnection.getErrorStream();
      if (stream == null) {
        return "<no data>";
      }

      final StringBuilder builder = new StringBuilder();
      final InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
      final char[] buf = new char[2048];
      int numRead;
      while ((numRead = reader.read(buf)) > 0) {
        builder.append(buf, 0, numRead);
      }
      return builder.toString();
    } catch (final Exception e) {
      return "<error retrieving data: " + e.getMessage() + ">";
    }
  }
}
