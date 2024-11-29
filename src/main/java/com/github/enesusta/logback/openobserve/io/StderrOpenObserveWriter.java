package com.github.enesusta.logback.openobserve.io;

import java.io.IOException;

public class StderrOpenObserveWriter implements OpenObserveWriter {

  @Override
  public void write(char[] cbuf, int off, int len) {
    System.err.println(new String(cbuf, 0, len));
  }

  @Override
  public void sendData() throws IOException {}

  @Override
  public boolean hasPendingData() {
    return false;
  }
}
