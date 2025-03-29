package com.github.enesusta.logback.openobserve.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * this holds the information from the appender/properties tag (in logback.xml)
 */
public class OpenObserveProperties {

  private List<Property> properties;

  public OpenObserveProperties() {
    this.properties = new ArrayList<Property>();
  }

  public List<Property> getProperties() {
    return properties;
  }

  /**
   * this is called by logback for each property tag contained in the properties
   * tag
   */
  public void addProperty(Property property) {
    properties.add(property);
  }

  /**
   * this is called by logback for each openObserveProperty tag contained in the
   * properties tag
   */
  public void addOpenObserveProperty(Property property) {
    properties.add(property);
  }
}
