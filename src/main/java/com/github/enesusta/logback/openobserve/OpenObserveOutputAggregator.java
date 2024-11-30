package com.github.enesusta.logback.openobserve;

import com.github.enesusta.logback.openobserve.domain.OpenObserveAppenderSettings;
import com.github.enesusta.logback.openobserve.io.OpenObserveWriter;
import com.github.enesusta.logback.openobserve.logback.LogbackErrorReporter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpenObserveOutputAggregator extends Writer {

  private LogbackErrorReporter errorReporter;
  private List<OpenObserveWriter> writers;
  private OpenObserveAppenderSettings settings;

  public OpenObserveOutputAggregator(
      final OpenObserveAppenderSettings settings, final LogbackErrorReporter errorReporter) {
    this.writers = new ArrayList<>();
    this.settings = settings;
    this.errorReporter = errorReporter;
  }

  public void addWriter(OpenObserveWriter writer) {
    writers.add(writer);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (OpenObserveWriter writer : writers) {
      writer.write(cbuf, off, len);
    }
  }

  public boolean sendData() {
    boolean success = true;
    for (OpenObserveWriter writer : writers) {
      try {
        writer.sendData();
      } catch (IOException e) {
        success = false;
        errorReporter.logWarning("Failed to send events to : OpenObserve" + e.getMessage());
        if (settings.isErrorsToStderr()) {
          System.err.println(
              "["
                  + new Date().toString()
                  + "] Failed to send events to : OpenObserve "
                  + e.getMessage());
        }
      }
    }
    return success;
  }

  @Override
  public void flush() throws IOException {}

  @Override
  public void close() throws IOException {}

  public boolean hasPendingData() {
    for (OpenObserveWriter writer : writers) {
      if (writer.hasPendingData()) {
        return true;
      }
    }
    return false;
  }

  public boolean hasOutputs() {
    return !writers.isEmpty();
  }
}
