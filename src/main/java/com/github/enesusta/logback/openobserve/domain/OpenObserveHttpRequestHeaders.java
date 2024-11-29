package com.github.enesusta.logback.openobserve.domain;

import java.util.LinkedList;
import java.util.List;

public class OpenObserveHttpRequestHeaders {

  private List<OpenObserveHttpRequestHeader> headers =
      new LinkedList<OpenObserveHttpRequestHeader>();

  public List<OpenObserveHttpRequestHeader> getHeaders() {
    return headers;
  }

  public void addHeader(OpenObserveHttpRequestHeader header) {
    headers.add(header);
  }
}
