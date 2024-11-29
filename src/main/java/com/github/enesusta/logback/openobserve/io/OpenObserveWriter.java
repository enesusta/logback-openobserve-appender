package com.github.enesusta.logback.openobserve.io;

import java.io.IOException;

public interface OpenObserveWriter {
  void write(char[] cbuf, int off, int len);

  void sendData() throws IOException;

  boolean hasPendingData();
}
