package com.github.enesusta.logback.openobserve.domain;

import java.util.ArrayList;
import java.util.List;

public class OpenObserveProperties {

  private List<OpenObserveProperty> properties;

  public OpenObserveProperties() {
    this.properties = new ArrayList<>();
  }

  public List<OpenObserveProperty> getProperties() {
    return properties;
  }

  public void addProperty(OpenObserveProperty openObserveProperty) {
    properties.add(openObserveProperty);
  }
}
