package com.github.enesusta.logback.openobserve.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface OpenObserveWriter {
  void write(ByteArrayOutputStream byteArrayOutputStream) throws IOException;
}
